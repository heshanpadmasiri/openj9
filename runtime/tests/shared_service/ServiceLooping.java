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
//
// This class is suggested for use by the Windows Shared Classes Service
// test as the class to be run by the Windows service.
// 
// It is a long running class that also provides a method to stop the run.
// This stop method is used by the service to end the Java processing
// 

public class ServiceLooping {

        private static boolean continueLooping = true;

	public ServiceLooping() {
	}

        public static void stopLooping() {
                continueLooping = false;
        }

	public static void main( String[] args ) {

                while ( continueLooping ) { 
                    try {
                        Thread.sleep(1000);
                    }
                    catch(Exception e) {
                    }
                }

	}

}

