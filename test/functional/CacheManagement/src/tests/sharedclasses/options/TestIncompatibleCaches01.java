/*******************************************************************************
 * Copyright IBM Corp. and others 2010
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

package tests.sharedclasses.options;

import tests.sharedclasses.TestUtils;

/*
 * Create some incompatible caches, check they appear in listAllCaches in the appropriate
 * section.
 */
public class TestIncompatibleCaches01 extends TestUtils {
  public static void main(String[] args) {
    runDestroyAllCaches();
	createIncompatibleCache("Foo5");
	createIncompatibleCache("Foo");
	createIncompatibleCache("Bar");

	listAllCaches();
	checkOutputForIncompatibleCache("Foo5",true);
	checkOutputForIncompatibleCache("Foo",true);
	checkOutputForIncompatibleCache("Bar",true);

	runDestroyAllCaches();
	checkOutputContains("\"Bar\".*is destroyed", "Did not see expected message about cache Bar being destroyed");
	checkOutputContains("\"Foo\".*is destroyed", "Did not see expected message about cache Foo being destroyed");
	checkOutputContains("\"Foo5\".*is destroyed", "Did not see expected message about cache Foo5 being destroyed");
	
  }
}
