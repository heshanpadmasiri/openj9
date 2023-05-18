/*[INCLUDE-IF Sidecar18-SE]*/
/*******************************************************************************
 * Copyright IBM Corp. and others 2011
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
package com.ibm.dtfj.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

/**
 * File manger for gzip'd files. The gzip algorithm does not support a hierarchical archive
 * and only works on a single file.
 * 
 * @author adam
 *
 */
public class GZipFileManager extends CompressedFileManager {

	public GZipFileManager(File file) {
		super(file);
	}

	@Override
	public void extract(ManagedImageSource file, File todir) throws IOException {
		validateImageSource(file);		//make sure the source refers to this file
		extract(todir);					//extract as normal
	}

	//as gzip files only contain one file, the passed source must be equal to the gzip file itself
	@Override
	public ImageInputStream getStream(ManagedImageSource source) throws IOException {
		validateImageSource(source);
		return getStream();
	}

	private void validateImageSource(ManagedImageSource source) throws IOException {
		File archive = new File(source.getPath());
		if(!archive.equals(managedFile)) {
			throw new IOException("The specified Image Source : " + source.getArchive().getAbsolutePath() + " does not match the contents of " + managedFile.getAbsolutePath());
		}
	}

	@Override
	public ImageInputStream getStream() throws IOException {
		FileInputStream fis = new FileInputStream(managedFile);
		GZIPInputStream gis = new GZIPInputStream(fis);
		return new MemoryCacheImageInputStream(gis);
	}

	@Override
	public void extract(File todir) throws IOException {
		checkDirectoryToExtractTo(todir);
		FileInputStream fis = new FileInputStream(managedFile);
		GZIPInputStream gis = new GZIPInputStream(fis);
		File extractTo = new File(todir, managedFile.getName());
		extractTo.deleteOnExit();
		extractEntry(gis, extractTo);
	}
}
