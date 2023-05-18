/*[INCLUDE-IF Sidecar18-SE]*/
/*******************************************************************************
 * Copyright IBM Corp. and others 2004
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution and is available at https://www.eclipse.org/legal/epl-2.0/
 * or the Apache License, Version 2.0 which accompanies this distribution and
 * is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following
 * Secondary Licenses when the conditions for such availability set
 * forth in the Eclipse Public License, v. 2.0 are satisfied: GNU
 * General Public License, version 2 with the GNU Classpath
 * Exception [1] and GNU General Public License, version 2 with the
 * OpenJDK Assembly Exception [2].
 *
 * [1] https://www.gnu.org/software/classpath/license.html
 * [2] https://openjdk.org/legal/assembly-exception.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0-only WITH Classpath-exception-2.0 OR GPL-2.0-only WITH OpenJDK-assembly-exception-1.0
 *******************************************************************************/
package com.ibm.dtfj.java.j9;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.dtfj.image.j9.CorruptData;
import com.ibm.dtfj.image.CorruptDataException;
import com.ibm.dtfj.image.ImagePointer;
import com.ibm.dtfj.java.JavaClass;
import com.ibm.dtfj.java.JavaObject;

/**
 * @author jmdisher
 *
 */
public class JavaClassLoader implements com.ibm.dtfj.java.JavaClassLoader
{
	private JavaRuntime _javaVM;
	private ImagePointer _id;
	private ImagePointer _objectID;
	private List _classIDs = new ArrayList();
	private JavaObject _object = null;
	private List   _defined = null;
	private List   _cached = null;
	
	public JavaClassLoader(JavaRuntime runtime, ImagePointer id, ImagePointer obj)
	{
		if (null == runtime) {
			throw new IllegalArgumentException("Java VM for class loader must not be null");
		}
		if (null == id) {
			throw new IllegalArgumentException("Class loader id must not be null");
		}
		_javaVM = runtime;
		_id = id;
		_objectID = obj;
	}

	/* (non-Javadoc)
	 * @see com.ibm.dtfj.java.JavaClassLoader#getDefinedClasses()
	 */
	public Iterator getDefinedClasses()
	{
		if (null == _defined) {
		// Loop through the list of all classes building a list of this loader's defined classes.
		// A defined class is a class whose class loader is this loader
		Iterator cached  = _javaVM.getClasses();
		long     thisID  = _id.getAddress();
			_defined = new ArrayList();

		while (cached.hasNext()) {
			Object next = cached.next();
			// If we find a CorruptData in the cached classes iterator we can't check its loader ID
			// so we don't include it in the list of defined classes for this loader. Note that we 
			// are not completely hiding the corruption - the DTFJ application can find out there are 
			// corrupt entries by looking at the cached classes via getCachedClasses()
			if (next instanceof CorruptData) {
				continue;
			}
			
			// Extract the current class
			JavaClass currentClass = (JavaClass)next;
			
			try {
				// Extract the current class's class loader
				JavaClassLoader currentClassLoader = (JavaClassLoader)currentClass.getClassLoader();
				
				// Test whether the current class belongs to this class loader.
				// NB : Tolerate null class loader pointers resulting from zip files generated by
				//      old versions of jextract
				if ((currentClassLoader != null) && (currentClassLoader._id.getAddress() == thisID)) {
						_defined.add(currentClass);
				}
			} catch (CorruptDataException e) {
				// Ignore this one
			}
		}
		}		
		return _defined.iterator();
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.dtfj.java.JavaClassLoader#getCachedClasses()
	 */
	public Iterator getCachedClasses()
	{
		if (null == _cached) {
		//a cached class is any class known to the loader
		Iterator ids = _classIDs.iterator();
			_cached = new ArrayList();
		
		while (ids.hasNext()) {
			long oneID = ((Long)ids.next()).longValue();
			JavaClass oneClass = _javaVM.getClassForID(oneID);
			if (null == oneClass) {
					_cached.add(new CorruptData("Cache reference to unknown class " + oneID, null));
			} else {
					_cached.add(oneClass);
				}
			}
		}
		return _cached.iterator();
	}

	/* (non-Javadoc)
	 * @see com.ibm.dtfj.java.JavaClassLoader#findClass(java.lang.String)
	 */
	public JavaClass findClass(String name) throws CorruptDataException
	{
		Iterator classes = getDefinedClasses();
		JavaClass found = null;
		
		while (classes.hasNext() && (null == found)) {
			JavaClass one = (JavaClass) classes.next();
			if (one.getName().equals(name)) {
				found = one;
			}
		}
		return found;
	}

	/* (non-Javadoc)
	 * @see com.ibm.dtfj.java.JavaClassLoader#getObject()
	 */
	public JavaObject getObject() throws CorruptDataException
	{
		if (null == _object) {
			try {
				_object = _javaVM.getObjectAtAddress(_objectID);
			} catch (IllegalArgumentException e) {
				// getObjectAtAddress can throw an IllegalArgumentException if the address is not aligned
				throw new CorruptDataException(new CorruptData(e.getMessage(),_objectID));
			}
		}
		return _object;
	}

	public long getID()
	{
		return _id.getAddress();
	}
	
	public void addClassID(long id)
	{
		_classIDs.add(Long.valueOf(id));
	}

	public boolean equals(Object obj)
	{
		boolean isEqual = false;
		
		if (obj instanceof JavaClassLoader) {
			JavaClassLoader local = (JavaClassLoader) obj;
			isEqual = ((_javaVM.equals(local._javaVM)) && (_id.equals(local._id)));
		}
		return isEqual;
	}

	public int hashCode()
	{
		return _javaVM.hashCode() ^ _id.hashCode();
	}
}
