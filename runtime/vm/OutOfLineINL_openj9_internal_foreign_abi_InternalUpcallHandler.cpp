/*******************************************************************************
 * Copyright IBM Corp. and others 2021
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
 * [2] http://openopenj9.java.net/legal/assembly-exception.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0-only WITH Classpath-exception-2.0 OR GPL-2.0-only WITH OpenJDK-assembly-exception-1.0
 *******************************************************************************/

#include "j9protos.h"
#include "j9vmnls.h"
#include "BytecodeAction.hpp"
#include "LayoutFFITypeHelpers.hpp"
#include "OutOfLineINL.hpp"
#include "VMHelpers.hpp"
#include "UnsafeAPI.hpp"

extern "C" {

#if JAVA_SPEC_VERSION >= 16
/**
 * openj9.internal.foreign.abi.InternalUpcallHandler: private native long allocateUpcallStub(UpcallMHMetaData mhMetaData, String[] cSignatureStrs);
 *
 * @brief Request the JIT to generate an upcall thunk of the specified java method
 *
 * @param mhMetaData[in] a data object that consists of the upcall handler and the metadata for MH resolution
 * @param cSignatureStrs[in] An array of the native signature string for the requested java method during the upcall
 * @return the address of the native thunk generated by JIT.
 */
VM_BytecodeAction
OutOfLineINL_openj9_internal_foreign_abi_InternalUpcallHandler_allocateUpcallStub(J9VMThread *currentThread, J9Method *method)
{
	VM_BytecodeAction rc = EXECUTE_BYTECODE;
	J9JavaVM *vm = currentThread->javaVM;
	J9InternalVMFunctions *vmFuncs = vm->internalVMFunctions;
	J9UpcallMetaData *upcallMetaData = NULL;
	j9object_t mhMetaData = NULL;
	J9UpcallNativeSignature *nativeSig = NULL;
	J9UpcallSigType *sigArray = NULL;
	void *thunkAddr = NULL;
	PORT_ACCESS_FROM_JAVAVM(vm);

	j9object_t cSigStrs = (j9object_t)currentThread->sp[0];
	/* the last element of the array is the signature of return type */
	U_32 sigCount  = J9INDEXABLEOBJECT_SIZE(currentThread, cSigStrs);

	/* Note: the J9UpcallMetaData pointer will be stored in the generated thunk as data
	 * in which case it is released only when the generated thunk memory is released
	 */
	upcallMetaData = (J9UpcallMetaData *)j9mem_allocate_memory(sizeof(J9UpcallMetaData), J9MEM_CATEGORY_VM_FFI);
	if (NULL == upcallMetaData) {
		rc = GOTO_THROW_CURRENT_EXCEPTION;
		setNativeOutOfMemoryError(currentThread, 0, 0);
		goto done;
	}

	nativeSig = (J9UpcallNativeSignature *)j9mem_allocate_memory(sizeof(J9UpcallNativeSignature), J9MEM_CATEGORY_VM_FFI);
	if (NULL == nativeSig) {
		rc = GOTO_THROW_CURRENT_EXCEPTION;
		setNativeOutOfMemoryError(currentThread, 0, 0);
		goto freeAllMemoryThenExit;
	}

	sigArray = (J9UpcallSigType *)j9mem_allocate_memory(sizeof(J9UpcallSigType) * sigCount, J9MEM_CATEGORY_VM_FFI);
	if (NULL == sigArray) {
		rc = GOTO_THROW_CURRENT_EXCEPTION;
		setNativeOutOfMemoryError(currentThread, 0, 0);
		goto freeAllMemoryThenExit;
	}

	for (U_32 sigIndex = 0; sigIndex < sigCount; sigIndex++) {
		j9object_t sigStrObject = J9JAVAARRAYOFOBJECT_LOAD(currentThread, cSigStrs, sigIndex);
		char sigBuffer[J9VM_NATIVE_SIGNATURE_STRING_LENGTH] = {0};
		/* The simplified signature string in cSig for parameter/return type is converted at java level.
		 * e.g.
		 * "4#I" represents a 4-byte integer
		 * "20#[5:I]" represents a 20-byte struct for {int a[5]}
		 * "16#[I(4)J]" represents a 16-byte struct for {int, padding(4 bytes), long}
		 *
		 * Note: the last element is the signature string for the return type.
		 */
		char *cSig = copyStringToUTF8WithMemAlloc(currentThread, sigStrObject,
				J9_STR_NULL_TERMINATE_RESULT, "", 0, sigBuffer, sizeof(sigBuffer), NULL);
		if (NULL == cSig) {
			rc = GOTO_THROW_CURRENT_EXCEPTION;
			setNativeOutOfMemoryError(currentThread, 0, 0);
			goto freeAllMemoryThenExit;
		}
		LayoutFFITypeHelpers::encodeUpcallSignature(cSig, &sigArray[sigIndex]);
		if (cSig != sigBuffer) {
			j9mem_free_memory(cSig);
		}
	}
	nativeSig->numSigs = sigCount;
	nativeSig->sigArray  = sigArray;

	/* Set the fields of the J9UpcallMetaData struct for the thunk generation */
	upcallMetaData->vm = vm;
	upcallMetaData->downCallThread = currentThread;
	upcallMetaData->nativeFuncSignature = nativeSig;
	mhMetaData = (j9object_t)currentThread->sp[1];
	upcallMetaData->mhMetaData = j9jni_createGlobalRef((JNIEnv*)currentThread, mhMetaData, false);
	if (NULL == upcallMetaData->mhMetaData) {
		rc = GOTO_THROW_CURRENT_EXCEPTION;
		setNativeOutOfMemoryError(currentThread, 0, 0);
		goto freeAllMemoryThenExit;
	}

	/* The resolution is performed by MethodHandleResolver.upcallLinkCallerMethod()
	 * to obtain a MemberName object plus appendix in a two element object array.
	 */
	VM_OutOfLineINL_Helpers::buildInternalNativeStackFrame(currentThread, method);
	resolveUpcallInvokeHandle(currentThread, upcallMetaData);
	if (VM_VMHelpers::exceptionPending(currentThread)) {
		rc = GOTO_THROW_CURRENT_EXCEPTION;
		goto freeAllMemoryThenExit;
	}
	VM_OutOfLineINL_Helpers::restoreInternalNativeStackFrame(currentThread);

	thunkAddr = vmFuncs->createUpcallThunk(upcallMetaData);
	if (NULL == thunkAddr) {
		rc = GOTO_THROW_CURRENT_EXCEPTION;
		setNativeOutOfMemoryError(currentThread, 0, 0);
		goto freeAllMemoryThenExit;
	}

done:
	VM_OutOfLineINL_Helpers::returnDouble(currentThread, (I_64)(intptr_t)thunkAddr, 3);
	return rc;

freeAllMemoryThenExit:
	j9mem_free_memory(upcallMetaData);
	upcallMetaData = NULL;

	j9mem_free_memory(nativeSig);
	nativeSig = NULL;

	j9mem_free_memory(sigArray);
	sigArray = NULL;

	goto done;
}
#endif /* JAVA_SPEC_VERSION >= 16 */

} /* extern "C" */
