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
package jit.test.jitt.invoke;

import org.testng.annotations.Test;
import org.testng.Assert;

public class InvokeStatic extends jit.test.jitt.Test {

	public static int tstStaticMethod(int numToReturn) {
		return numToReturn;
	}

	public int tstSendingStaticMethod(boolean run, int numToReturn) {
		if(run) {
			return tstStaticMethod(numToReturn);
		} else {
			return 0;
		}
	}

	@Test(groups = { "level.sanity","component.jit" })
	public void testInvokeStatic() {
		for (int j = 0; j <= sJitThreshold ; j++) {
			tstSendingStaticMethod(false, 0);
		}
		if(5 != tstSendingStaticMethod(true, 5)) {
			Assert.fail("did not get back what was passed");
		}
	}
}
