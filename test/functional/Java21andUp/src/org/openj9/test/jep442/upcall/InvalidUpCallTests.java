/*******************************************************************************
 * Copyright IBM Corp. and others 2023
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
package org.openj9.test.jep442.upcall;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.AssertJUnit;
import static org.testng.Assert.fail;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import static java.lang.foreign.ValueLayout.*;

/**
 * Test cases for JEP 442: Foreign Linker API (Third Preview) in upcall,
 * which verify the illegal cases including the returned segment, etc.
 */
@Test(groups = { "level.sanity" })
public class InvalidUpCallTests {
	private static Linker linker = Linker.nativeLinker();

	static {
		System.loadLibrary("clinkerffitests");
	}
	private static final SymbolLookup nativeLibLookup = SymbolLookup.loaderLookup();

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "An exception is thrown from the upcall method")
	public void test_throwExceptionFromUpcallMethod() throws Throwable {
		GroupLayout structLayout = MemoryLayout.structLayout(JAVA_INT.withName("elem1"), JAVA_INT.withName("elem2"));
		VarHandle intHandle1 = structLayout.varHandle(PathElement.groupElement("elem1"));
		VarHandle intHandle2 = structLayout.varHandle(PathElement.groupElement("elem2"));

		FunctionDescriptor fd = FunctionDescriptor.of(structLayout, structLayout, structLayout, ADDRESS);
		MemorySegment functionSymbol = nativeLibLookup.find("add2IntStructs_returnStructByUpcallMH").get();
		MethodHandle mh = linker.downcallHandle(functionSymbol, fd);

		try (Arena arena = Arena.openConfined()) {
			MemorySegment upcallFuncAddr = linker.upcallStub(UpcallMethodHandles.MH_add2IntStructs_returnStruct_throwException,
					FunctionDescriptor.of(structLayout, structLayout, structLayout), arena.scope());
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(arena.scope());
			MemorySegment structSegmt1 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt1, 11223344);
			intHandle2.set(structSegmt1, 55667788);
			MemorySegment structSegmt2 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt2, 99001122);
			intHandle2.set(structSegmt2, 33445566);

			MemorySegment resultSegmt = (MemorySegment)mh.invoke(allocator, structSegmt1, structSegmt2, upcallFuncAddr);
			fail("Failed to throw out IllegalArgumentException from the the upcall method");
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "An exception is thrown from the upcall method")
	public void test_nestedUpcall_throwExceptionFromUpcallMethod() throws Throwable {
		GroupLayout structLayout = MemoryLayout.structLayout(JAVA_INT.withName("elem1"), JAVA_INT.withName("elem2"));
		VarHandle intHandle1 = structLayout.varHandle(PathElement.groupElement("elem1"));
		VarHandle intHandle2 = structLayout.varHandle(PathElement.groupElement("elem2"));

		FunctionDescriptor fd = FunctionDescriptor.of(structLayout, structLayout, structLayout, ADDRESS);
		MemorySegment functionSymbol = nativeLibLookup.find("add2IntStructs_returnStructByUpcallMH").get();
		MethodHandle mh = linker.downcallHandle(functionSymbol, fd);

		try (Arena arena = Arena.openConfined()) {
			MemorySegment upcallFuncAddr = linker.upcallStub(UpcallMethodHandles.MH_add2IntStructs_returnStruct_nestedUpcall,
					FunctionDescriptor.of(structLayout, structLayout, structLayout), arena.scope());
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(arena.scope());
			MemorySegment structSegmt1 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt1, 11223344);
			intHandle2.set(structSegmt1, 55667788);
			MemorySegment structSegmt2 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt2, 99001122);
			intHandle2.set(structSegmt2, 33445566);

			MemorySegment resultSegmt = (MemorySegment)mh.invoke(allocator, structSegmt1, structSegmt2, upcallFuncAddr);
			fail("Failed to throw out IllegalArgumentException from the nested upcall");
		}
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void test_nullValueForReturnPtr() throws Throwable {
		GroupLayout structLayout = MemoryLayout.structLayout(JAVA_INT.withName("elem1"), JAVA_INT.withName("elem2"));
		VarHandle intHandle1 = structLayout.varHandle(PathElement.groupElement("elem1"));
		VarHandle intHandle2 = structLayout.varHandle(PathElement.groupElement("elem2"));

		FunctionDescriptor fd = FunctionDescriptor.of(ADDRESS, ADDRESS, structLayout, ADDRESS);
		MemorySegment functionSymbol = nativeLibLookup.find("add2IntStructs_returnStructPointerByUpcallMH").get();
		MethodHandle mh = linker.downcallHandle(functionSymbol, fd);

		try (Arena arena = Arena.openConfined()) {
			MemorySegment upcallFuncAddr = linker.upcallStub(UpcallMethodHandles.MH_add2IntStructs_returnStructPointer_nullValue,
					FunctionDescriptor.of(ADDRESS, ADDRESS, structLayout), arena.scope());
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(arena.scope());
			MemorySegment structSegmt1 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt1, 11223344);
			intHandle2.set(structSegmt1, 55667788);
			MemorySegment structSegmt2 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt2, 99001122);
			intHandle2.set(structSegmt2, 33445566);

			MemorySegment resultAddr = (MemorySegment)mh.invoke(structSegmt1, structSegmt2, upcallFuncAddr);
			fail("Failed to throw out NullPointerException in the case of the null value upon return");
		}
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void test_nullValueForReturnStruct() throws Throwable {
		GroupLayout structLayout = MemoryLayout.structLayout(JAVA_INT.withName("elem1"), JAVA_INT.withName("elem2"));
		VarHandle intHandle1 = structLayout.varHandle(PathElement.groupElement("elem1"));
		VarHandle intHandle2 = structLayout.varHandle(PathElement.groupElement("elem2"));

		FunctionDescriptor fd = FunctionDescriptor.of(structLayout, structLayout, structLayout, ADDRESS);
		MemorySegment functionSymbol = nativeLibLookup.find("add2IntStructs_returnStructByUpcallMH").get();
		MethodHandle mh = linker.downcallHandle(functionSymbol, fd);

		try (Arena arena = Arena.openConfined()) {
			MemorySegment upcallFuncAddr = linker.upcallStub(UpcallMethodHandles.MH_add2IntStructs_returnStruct_nullValue,
					FunctionDescriptor.of(structLayout, structLayout, structLayout), arena.scope());
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(arena.scope());
			MemorySegment structSegmt1 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt1, 11223344);
			intHandle2.set(structSegmt1, 55667788);
			MemorySegment structSegmt2 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt2, 99001122);
			intHandle2.set(structSegmt2, 33445566);

			MemorySegment resultSegmt = (MemorySegment)mh.invoke(allocator, structSegmt1, structSegmt2, upcallFuncAddr);
			fail("Failed to throw out NullPointerException in the case of the null value upon return");
		}
	}

	public void test_nullSegmentForReturnPtr() throws Throwable {
		GroupLayout structLayout = MemoryLayout.structLayout(JAVA_INT.withName("elem1"), JAVA_INT.withName("elem2"));
		VarHandle intHandle1 = structLayout.varHandle(PathElement.groupElement("elem1"));
		VarHandle intHandle2 = structLayout.varHandle(PathElement.groupElement("elem2"));

		FunctionDescriptor fd = FunctionDescriptor.of(ADDRESS, ADDRESS, structLayout, ADDRESS);
		MemorySegment functionSymbol = nativeLibLookup.find("validateReturnNullAddrByUpcallMH").get();
		MethodHandle mh = linker.downcallHandle(functionSymbol, fd);

		try (Arena arena = Arena.openConfined()) {
			MemorySegment upcallFuncAddr = linker.upcallStub(UpcallMethodHandles.MH_add2IntStructs_returnStructPointer_nullSegmt,
					FunctionDescriptor.of(ADDRESS, ADDRESS, structLayout), arena.scope());
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(arena.scope());
			MemorySegment structSegmt1 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt1, 11223344);
			intHandle2.set(structSegmt1, 55667788);
			MemorySegment structSegmt2 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt2, 99001122);
			intHandle2.set(structSegmt2, 33445566);

			MemorySegment resultAddr = (MemorySegment)mh.invoke(structSegmt1, structSegmt2, upcallFuncAddr);
			MemorySegment resultSegmt = MemorySegment.ofAddress(resultAddr.address(), structLayout.byteSize(), arena.scope());
			Assert.assertEquals(resultSegmt.get(JAVA_INT, 0), 11223344);
			Assert.assertEquals(resultSegmt.get(JAVA_INT, 4), 55667788);
		}
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void test_nullSegmentForReturnStruct() throws Throwable {
		GroupLayout structLayout = MemoryLayout.structLayout(JAVA_INT.withName("elem1"), JAVA_INT.withName("elem2"));
		VarHandle intHandle1 = structLayout.varHandle(PathElement.groupElement("elem1"));
		VarHandle intHandle2 = structLayout.varHandle(PathElement.groupElement("elem2"));

		FunctionDescriptor fd = FunctionDescriptor.of(structLayout, structLayout, structLayout, ADDRESS);
		MemorySegment functionSymbol = nativeLibLookup.find("add2IntStructs_returnStructByUpcallMH").get();
		MethodHandle mh = linker.downcallHandle(functionSymbol, fd);

		try (Arena arena = Arena.openConfined()) {
			MemorySegment upcallFuncAddr = linker.upcallStub(UpcallMethodHandles.MH_add2IntStructs_returnStruct_nullSegmt,
					FunctionDescriptor.of(structLayout, structLayout, structLayout), arena.scope());
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(arena.scope());
			MemorySegment structSegmt1 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt1, 11223344);
			intHandle2.set(structSegmt1, 55667788);
			MemorySegment structSegmt2 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt2, 99001122);
			intHandle2.set(structSegmt2, 33445566);

			MemorySegment resultSegmt = (MemorySegment)mh.invoke(allocator, structSegmt1, structSegmt2, upcallFuncAddr);
			fail("Failed to throw out NullPointerException in the case of the null segment upon return");
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Heap segment not allowed.*")
	public void test_heapSegmentForReturnPtr() throws Throwable {
		GroupLayout structLayout = MemoryLayout.structLayout(JAVA_INT.withName("elem1"), JAVA_INT.withName("elem2"));
		VarHandle intHandle1 = structLayout.varHandle(PathElement.groupElement("elem1"));
		VarHandle intHandle2 = structLayout.varHandle(PathElement.groupElement("elem2"));

		FunctionDescriptor fd = FunctionDescriptor.of(ADDRESS, ADDRESS, structLayout, ADDRESS);
		MemorySegment functionSymbol = nativeLibLookup.find("add2IntStructs_returnStructPointerByUpcallMH").get();
		MethodHandle mh = linker.downcallHandle(functionSymbol, fd);

		try (Arena arena = Arena.openConfined()) {
			MemorySegment upcallFuncAddr = linker.upcallStub(UpcallMethodHandles.MH_add2IntStructs_returnStructPointer_heapSegmt,
					FunctionDescriptor.of(ADDRESS, ADDRESS, structLayout), arena.scope());
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(arena.scope());
			MemorySegment structSegmt1 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt1, 11223344);
			intHandle2.set(structSegmt1, 55667788);
			MemorySegment structSegmt2 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt2, 99001122);
			intHandle2.set(structSegmt2, 33445566);

			MemorySegment resultAddr = (MemorySegment)mh.invoke(structSegmt1, structSegmt2, upcallFuncAddr);
			fail("Failed to throw out IllegalArgumentException in the case of the heap segment upon return");
		}
	}

	public void test_heapSegmentForReturnStruct() throws Throwable {
		GroupLayout structLayout = MemoryLayout.structLayout(JAVA_INT.withName("elem1"), JAVA_INT.withName("elem2"));
		VarHandle intHandle1 = structLayout.varHandle(PathElement.groupElement("elem1"));
		VarHandle intHandle2 = structLayout.varHandle(PathElement.groupElement("elem2"));

		FunctionDescriptor fd = FunctionDescriptor.of(structLayout, structLayout, structLayout, ADDRESS);
		MemorySegment functionSymbol = nativeLibLookup.find("add2IntStructs_returnStructByUpcallMH").get();
		MethodHandle mh = linker.downcallHandle(functionSymbol, fd);

		try (Arena arena = Arena.openConfined()) {
			MemorySegment upcallFuncAddr = linker.upcallStub(UpcallMethodHandles.MH_add2IntStructs_returnStruct_heapSegmt,
					FunctionDescriptor.of(structLayout, structLayout, structLayout), arena.scope());
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(arena.scope());
			MemorySegment structSegmt1 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt1, 11223344);
			intHandle2.set(structSegmt1, 55667788);
			MemorySegment structSegmt2 = allocator.allocate(structLayout);
			intHandle1.set(structSegmt2, 99001122);
			intHandle2.set(structSegmt2, 33445566);

			MemorySegment resultSegmt = (MemorySegment)mh.invoke(allocator, structSegmt1, structSegmt2, upcallFuncAddr);
			Assert.assertEquals(intHandle1.get(resultSegmt), 110224466);
			Assert.assertEquals(intHandle2.get(resultSegmt), 89113354);
		}
	}
}
