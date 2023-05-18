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
package j9vm.test.softmx;

import org.testng.annotations.Test;
import org.testng.log4testng.Logger;
import org.testng.AssertJUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import com.ibm.lang.management.MemoryMXBean;

@SuppressWarnings({ "nls", "restriction" })
public class SoftmxRASTest1 {

	public static final Logger logger = Logger.getLogger(SoftmxRASTest1.class);

	private final MemoryMXBean ibmMemoryMBean = (MemoryMXBean) ManagementFactory.getMemoryMXBean();

	/**
	 * RAS Test case 1 : Start JVM with -Xsoftmx****, trigger OOM exception,
	 * verify that softmx value is reported in the javacore.
	 */
	@Test(groups = { "level.extended" })
	public void testSoftmx_RAS_Test_1() {
		try {
			final int MegaByte = 1024 * 1024;
			long initialXmxValue = ibmMemoryMBean.getMaxHeapSize() / MegaByte; // get the value in MB
			long initialSoftmxValue = initialXmxValue / 2;

			logger.info("Starting RAS testcase 1 : We will initially set -Xmx = " + initialXmxValue
					+ "M and -Xsoftmx = " + initialSoftmxValue + "M");

			int exitValueOfSecondJVM = startSecondJVM(initialXmxValue, initialSoftmxValue,
					OOMGenerator_RAS_Test1.class);

			logger.info("After expected crash, exit value : " + exitValueOfSecondJVM);

			logger.debug("Analyzing javacore..");

			long softmxValue_from_javacore = extractSoftmxVal_From_Javacore() / MegaByte;

			AssertJUnit.assertTrue("Softmx value is not reported on Javacore after it was set via -Xsoftmx on command line",
					softmxValue_from_javacore != -1);

			logger.debug("Softmx value found in javacore : " + softmxValue_from_javacore + "M");
			logger.debug("Softmx value set via -Xsoftmx on command line : " + initialSoftmxValue + "M");

			AssertJUnit.assertTrue("Softmx value found in javacore is bigger than  the value set via -Xsoftmx on command line",
					softmxValue_from_javacore <= initialSoftmxValue);

		} catch (InterruptedException e) {
			logger.error("Unexpected exception occured" + e.getMessage(), e);
		} catch (IOException e) {
			logger.error("Unexpected exception occured" + e.getMessage(), e);
		}
	}

	public static long extractSoftmxVal_From_Javacore() {
		File currentDir = new File(".");
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().startsWith("javacore");
			}
		};
		File javacoreFileToParse = null;
		long newestUpdateTime = 0;

		/* We may find more than one javacore - one generated by first run where
		 * we set the -Xsoftmx on the command line, and another on the second run
		 * when we set softmx using JMX, we have to be careful when picking the
		 * core file during the second JVM run - we must pick the latest one.
		 * Among files with the same modification time (which may occur because
		 * we're *only* requesting javacore dumps), we choose the one with a larger
		 * sequence number in its name.
		 */
		for (File file : currentDir.listFiles(filter)) {
			long updateTime = file.lastModified();

			if (javacoreFileToParse != null) {
				if (updateTime < newestUpdateTime) {
					// skip this older file
					continue;
				} else if (updateTime == newestUpdateTime
						&& file.getName().compareTo(javacoreFileToParse.getName()) < 0) {
					// skip this file with the same modification time but a smaller sequence number
					continue;
				}
			}

			javacoreFileToParse = file;
			newestUpdateTime = updateTime;
		}

		if (javacoreFileToParse == null) {
			logger.error("Can not find javacore file");
		} else {
			try {
				BufferedReader br = new BufferedReader(new FileReader(javacoreFileToParse));
				try {

					while (true) {
						String aLine = br.readLine();
						if (aLine == null) {
							break;
						} else if (aLine.startsWith("1STHEAPTARGET")) {
							logger.debug("1STHEAPTARGET present in javacore ");
							logger.debug("Found entry for softmx : " + aLine);
							logger.debug("Extracting softmx value...");

							Pattern pattern = Pattern.compile("1STHEAPTARGET\\s+Target memory:\\s+(\\d+).*");
							Matcher matcher = pattern.matcher(aLine);

							if (!matcher.matches()) {
								throw new Exception();
							}

							String softmxVal = matcher.group(1);

							return Long.parseLong(softmxVal);
						}
					}
				} catch (Exception e) {
					logger.error("Error parsing javacore " + e.getMessage(), e);
				} finally {
					br.close();
				}
			} catch (IOException e) {
				logger.error("Unexpected exception occured " + e.getMessage(), e);
			}
		}

		return -1;
	}

	/**
	 * Starts a JVM in a subprocess
	 * @param xmxVal : -Xmx value to use in the command line of the JVM to be spawned
	 * @param softmxVal : -Xsoftmx value to use in the command line of the JVM to be spawned
	 * @param classToRun : The class that should be run using java
	 * @return : return code of the sub-process which runs the JVM
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static int startSecondJVM(long xmxVal, long softmxVal, Class<?> classToRun)
			throws IOException, InterruptedException {
		List<String> arguments = new ArrayList<String>();

		/* pass parent JVM options to the child JVMs. */
		List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();

		// Include -X, but not -Xdump, arguments from parent first to allow for later overrides.
		for (String arg : inputArgs) {
			if (arg.startsWith("-X") && !arg.startsWith("-Xdump")) {
				arguments.add(arg);
			}
		}

		// We're only interested in generating javacore files: disable other dumps.
		// first disable all dump agents
		arguments.add("-Xdump:none");

		// now enable javacore files for systhrow of OutOfMemoryError
		arguments.add("-Xdump:java:events=systhrow,filter=java/lang/OutOfMemoryError");

		String classpath = System.getProperty("java.class.path");

		arguments.add("-cp");
		arguments.add(classpath);

		arguments.add("-Xmx" + xmxVal + "M");

		if (softmxVal != -1) {
			arguments.add("-Xsoftmx" + softmxVal + "M");
		}

		arguments.add("-Xmn1M");

		arguments.add(classToRun.getCanonicalName());

		StringBuilder cmdLineBuffer = new StringBuilder();
		String javaPath = System.getProperty("java.home") + "/bin/java";

		cmdLineBuffer.append(javaPath);

		for (String argument : arguments) {
			cmdLineBuffer.append(' ').append(argument);
		}

		String cmdLineStr = cmdLineBuffer.toString();

		logger.debug("Executing cmd: " + cmdLineStr);

		CommandLine cmdLine = CommandLine.parse(cmdLineStr);
		DefaultExecutor executor = new DefaultExecutor();
		PumpStreamHandler strmHndlr = new PumpStreamHandler(System.out);
		executor.setWorkingDirectory(new File("."));
		executor.setStreamHandler(strmHndlr);

		int exitValue;

		try {
			exitValue = executor.execute(cmdLine);
		} catch (ExecuteException e) {
			exitValue = -1;
		}

		return exitValue;
	}

}
