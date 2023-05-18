/*******************************************************************************
 * Copyright IBM Corp. and others 2014
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

/**
 * @file testlibA.c
 * @brief File is to be compiled into a shared library (testlibA).  All the symbols should
 * be exported and visible for external invocations.
 */

#include <stdio.h>
#include "jni.h"

/**
 * @brief Function indicates to the runtime that the library testlibA has been
 * linked into the executable.
 */
jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
	fprintf(stdout, "[MSG] Reached OnLoad (testlibA): JNI_OnLoad [dynamically]\n");
	fflush(stdout);
	return JNI_VERSION_1_8;
}

/**
 * @brief Function indicates an alternative to the traditional unload routine to
 * the runtime specifically targeting the library testlibA.
 */
void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved)
{
	fprintf(stdout, "[MSG] Reached OnUnload (testlibA): JNI_OnUnload [dynamically]\n");
	fflush(stdout);
	/* Nothing much to cleanup here. */
	return;
}

/**
 * @brief Provide an implementation for the class Hello's native instance method 
 * fooImpl.
 * Package:	  com.ibm.j9.tests.jeptests
 * Class:     StaticLinking
 * Method:    fooImpl
 * @param[in] env The JNI env.
 * @param[in] instance The this pointer.
 */
void JNICALL
Java_com_ibm_j9_tests_jeptests_StaticLinking_fooImpl(JNIEnv *env, jobject this)
{
	fprintf(stdout, "[MSG] Reached native fooImpl() [dynamically]\n");
	fflush(stdout);
}
