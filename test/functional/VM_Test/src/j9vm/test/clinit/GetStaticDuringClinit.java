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
package j9vm.test.clinit;

// Only methods named jit* need to be JIT-compiled for the test to fail on bad VMs,
// i.e. "-Xjit:count=0,limit={*.jit*}"

class GetStaticTestHelper {
	public static String i;

	static {
		GetStaticDuringClinit.jitWrite("value");
		GetStaticDuringClinit.jitRead();
		Object lock = GetStaticDuringClinit.lock;
		synchronized(lock) {
			GetStaticDuringClinit.runBlocker = true;
			lock.notifyAll();
		}
		for (int i = 0; i < 100000; ++i) {
			try {
				Thread.sleep(1000000);
			} catch(InterruptedException e) {
				GetStaticDuringClinit.interrupted = true;
				break;
			}
		}
	}
}

public class GetStaticDuringClinit {
	public static Object lock = new Object();
	public static boolean runBlocker = false;
	public static boolean threadsReady = false;
	public static volatile boolean passed = true;
	public static volatile boolean interrupted = false;

	public static String jitRead() {
		return GetStaticTestHelper.i;
	}

	public static void jitWrite(String x) {
		// https://github.com/eclipse-openj9/openj9/pull/2794
		// The write to the static field causes jitResolveClassFromStaticField to be called on
		// certain GC policies. On older VMs, this caused the CP entry to be resolved, meaning
		// the call to jitResolveStaticField succeeds without the class init check.
		GetStaticTestHelper.i = x;
	}

	public static void test() {
		Thread initializer = new Thread() {
			public void run() {
				// Initialize helper class
				try {
					Class.forName("j9vm.test.clinit.GetStaticTestHelper");
				} catch(ClassNotFoundException t) {
					passed = false;
					throw new RuntimeException(t);
				}
			}
		};
		Thread blocker = new Thread() {
			public void run() {
				synchronized(lock) {
					while (!runBlocker) {
						try {
							lock.wait();
						} catch(InterruptedException e) {
						}
					}
					threadsReady = true;
					lock.notifyAll();
				}
				String value = jitRead();
				if (!interrupted) {
					// <clinit> for GetStaticTestHelper never returns, so if execution reaches
					// here, the VM has allowed an invalid getstatic.
					passed = false;
				}
			}
		};
		initializer.start();
		blocker.start();
		// Wait until the blocker is about to attempt the getstatic
		synchronized(lock) {
			while (!threadsReady) {
				try {
					lock.wait();
				} catch(InterruptedException e) {
				}
			}
		}
		// 5 seconds should be enough time for the blocker to perform the getstatic
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		initializer.interrupt();
		blocker.interrupt();
		try {
			initializer.join(); 
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		try {
			blocker.join(); 
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (!passed) {
			throw new RuntimeException("TEST FAILED");
		}
	}

	public static void main(String[] args) {
		GetStaticDuringClinit.test();
	}
}
