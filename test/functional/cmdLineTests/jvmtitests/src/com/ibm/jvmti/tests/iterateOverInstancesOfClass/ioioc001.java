/*******************************************************************************
 * Copyright IBM Corp. and others 2001
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
package com.ibm.jvmti.tests.iterateOverInstancesOfClass;


public class ioioc001 
{
	ioioc001Class klass = new ioioc001Class();
	ioioc001SubClass subKlass = new ioioc001SubClass();
		
	
	/**
	 * Check the IterateOverInstancesOfClass API for correct traversal of a class and its subclasses.
	 * Added as a unit test for CMVC 111021 
	 * 
	 * @return true on pass
	 */
	public boolean testCheckSubclasses() 
	{		
		Class clazz = ioioc001Class.class;
				
		boolean rc = checkSubclasses(clazz, klass, subKlass, klass.getName(), subKlass.getName());
								
		return rc;
	}
	
	public String helpCheckSubclasses()
	{
		return "Tests IterateOverInstancesOfClass API for correct traversal of a class and its subclasses. Added as a unit test for CMVC 111021.";
	}
	
			
	public static native boolean 
	checkSubclasses(Class klass, Object klassObject, Object subKlassObject,	char[] className, char[] subClassName);
	
}
