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
package org.openj9.test.floatsanity.conversions;

import java.util.ArrayList;

import org.openj9.test.floatsanity.D;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

@Test(groups = { "level.sanity" })
public class CheckConversionsFromDouble {

	public static final byte pBMAX = (byte)0x7F;
	public static final byte nBMAX = (byte)0x80;
	public static final short pSMAX = (short)0x7FFF;
	public static final short nSMAX = (short)0x8000;
	public static final int pIMAX = 0x7FFFFFFF;
	public static final int nIMAX = 0x80000000;
	public static final long pLMAX = 0x7FFFFFFFFFFFFFFFL;
	public static final long nLMAX = 0x8000000000000000L;

	class ConversionPair<T> {
		public double from;
		public T to;

		public ConversionPair(double from, T to) {
			this.from = from;
			this.to = to;
		}
	}

	public static Logger logger = Logger.getLogger(CheckConversionsFromDouble.class);

	@BeforeClass
	public void groupName() {
		logger.debug("Check conversions from double");
	}

	/* Actually, the java language spec contradicts the VM spec
	   and describes semantics for narrowing conversions of float
	   to integral types which exactly match sequences such as
	   f2i,i2b (i.e. it describes exactly the results for inf/max/min below).
	*/

	public void double_to_byte() {
		ArrayList<ConversionPair<Byte>> testCases = new ArrayList<>();
		
		testCases.add(new ConversionPair<Byte>(D.PZERO,	 (byte)0));
		testCases.add(new ConversionPair<Byte>(D.NZERO,	 (byte)0));
		testCases.add(new ConversionPair<Byte>(D.pOne,	 (byte)1));
		testCases.add(new ConversionPair<Byte>(D.nOne,	 (byte)-1));
		testCases.add(new ConversionPair<Byte>(D.pBMAX,	 (byte)pBMAX));
		testCases.add(new ConversionPair<Byte>(D.nBMAX,	 (byte)nBMAX));
		testCases.add(new ConversionPair<Byte>(D.PINF,	 (byte)0xFF));
		testCases.add(new ConversionPair<Byte>(D.NINF,	 (byte)0x00));
		testCases.add(new ConversionPair<Byte>(D.PMAX,	 (byte)0xFF));
		testCases.add(new ConversionPair<Byte>(D.NMAX,	 (byte)0x00));
		testCases.add(new ConversionPair<Byte>(D.PMIN,	 (byte)0));
		testCases.add(new ConversionPair<Byte>(D.NMIN,	 (byte)0));

		for (ConversionPair<Byte> pair : testCases) {
			String operation = "testing conversion: convert double " + pair.from + " to byte";
			logger.debug(operation);
			Assert.assertEquals((byte)pair.from, (byte)pair.to, operation);
		}
	}

	public void double_to_char() {
		ArrayList<ConversionPair<Character>> testCases = new ArrayList<>();

 		testCases.add(new ConversionPair<Character>(D.PZERO,	 (char)0));
		testCases.add(new ConversionPair<Character>(D.NZERO,	 (char)0));
		testCases.add(new ConversionPair<Character>(D.pOne,		 (char)1));
		testCases.add(new ConversionPair<Character>(D.pBMAX,	 (char)pBMAX));
		testCases.add(new ConversionPair<Character>(D.pSMAX,	 (char)pSMAX));
		testCases.add(new ConversionPair<Character>(D.PINF,		 (char)0xFFFF));
		testCases.add(new ConversionPair<Character>(D.NINF,		 (char)0x0000));
		testCases.add(new ConversionPair<Character>(D.PMAX,		 (char)0xFFFF));
		testCases.add(new ConversionPair<Character>(D.NMAX,		 (char)0x0000));
		testCases.add(new ConversionPair<Character>(D.PMIN,		 (char)0));
		testCases.add(new ConversionPair<Character>(D.NMIN,		 (char)0));

		for (ConversionPair<Character> pair : testCases) {
			String operation = "testing conversion: convert double " + pair.from + " to char";
			logger.debug(operation);
			Assert.assertEquals((char)pair.from, (char)pair.to, operation);
		}
	}

	public void double_to_short() {
		ArrayList<ConversionPair<Short>> testCases = new ArrayList<>();

		testCases.add(new ConversionPair<Short>(D.PZERO,	 (short)0));
		testCases.add(new ConversionPair<Short>(D.NZERO,	 (short)0));
		testCases.add(new ConversionPair<Short>(D.pOne,		 (short)1));
		testCases.add(new ConversionPair<Short>(D.nOne,		 (short)-1));
		testCases.add(new ConversionPair<Short>(D.pBMAX,	 (short)pBMAX));
		testCases.add(new ConversionPair<Short>(D.nBMAX,	 (short)nBMAX));
		testCases.add(new ConversionPair<Short>(D.pSMAX,	 (short)pSMAX));
		testCases.add(new ConversionPair<Short>(D.nSMAX,	 (short)nSMAX));
		testCases.add(new ConversionPair<Short>(D.PINF,		 (short)0xFFFF));
		testCases.add(new ConversionPair<Short>(D.NINF,		 (short)0x0000));
		testCases.add(new ConversionPair<Short>(D.PMAX,		 (short)0xFFFF));
		testCases.add(new ConversionPair<Short>(D.NMAX,		 (short)0x0000));
		testCases.add(new ConversionPair<Short>(D.PMIN,		 (short)0));
		testCases.add(new ConversionPair<Short>(D.NMIN,		 (short)0));

		for (ConversionPair<Short> pair : testCases) {
			String operation = "testing conversion: convert double " + pair.from + " to short";
			logger.debug(operation);
			Assert.assertEquals((short)pair.from, (short)pair.to, operation);
		}
	}

	public void double_to_int() {
		ArrayList<ConversionPair<Integer>> testCases = new ArrayList<>();

		testCases.add(new ConversionPair<Integer>(D.PZERO,	0));
		testCases.add(new ConversionPair<Integer>(D.NZERO,	0));
		testCases.add(new ConversionPair<Integer>(D.pOne,	1));
		testCases.add(new ConversionPair<Integer>(D.nOne,	-1));
		testCases.add(new ConversionPair<Integer>(D.pBMAX,	(int)pBMAX));
		testCases.add(new ConversionPair<Integer>(D.nBMAX,	(int)nBMAX));
		testCases.add(new ConversionPair<Integer>(D.pSMAX,	(int)pSMAX));
		testCases.add(new ConversionPair<Integer>(D.nSMAX,	(int)nSMAX));
		testCases.add(new ConversionPair<Integer>(D.nIMAX,	(int)nIMAX));
		testCases.add(new ConversionPair<Integer>(D.PINF,	(int)pIMAX));
		testCases.add(new ConversionPair<Integer>(D.NINF,	(int)nIMAX));
		testCases.add(new ConversionPair<Integer>(D.PMAX,	(int)pIMAX));
		testCases.add(new ConversionPair<Integer>(D.NMAX,	(int)nIMAX));
		testCases.add(new ConversionPair<Integer>(D.PMIN,	0));
		testCases.add(new ConversionPair<Integer>(D.NMIN,	0));

		for (ConversionPair<Integer> pair : testCases) {
			String operation = "testing conversion: convert double " + pair.from + " to byintte";
			logger.debug(operation);
			Assert.assertEquals((int)pair.from, (int)pair.to, operation);
		}
	}

	public void double_to_long() {
		ArrayList<ConversionPair<Long>> testCases = new ArrayList<>();

		testCases.add(new ConversionPair<Long>(D.PZERO,	0L));
		testCases.add(new ConversionPair<Long>(D.NZERO,	0L));
		testCases.add(new ConversionPair<Long>(D.pOne,	1L));
		testCases.add(new ConversionPair<Long>(D.nOne,	-1L));
		testCases.add(new ConversionPair<Long>(D.pBMAX,	(long)pBMAX));
		testCases.add(new ConversionPair<Long>(D.nBMAX,	(long)nBMAX));
		testCases.add(new ConversionPair<Long>(D.pSMAX,	(long)pSMAX));
		testCases.add(new ConversionPair<Long>(D.nSMAX,	(long)nSMAX));
		testCases.add(new ConversionPair<Long>(D.nIMAX,	(long)nIMAX));
		testCases.add(new ConversionPair<Long>(D.nLMAX,	(long)nLMAX));
		testCases.add(new ConversionPair<Long>(D.PINF,	(long)pLMAX));
		testCases.add(new ConversionPair<Long>(D.NINF,	(long)nLMAX));
		testCases.add(new ConversionPair<Long>(D.PMAX,	(long)pLMAX));
		testCases.add(new ConversionPair<Long>(D.NMAX,	(long)nLMAX));
		testCases.add(new ConversionPair<Long>(D.PMIN,	0L));
		testCases.add(new ConversionPair<Long>(D.NMIN,	0L));

		for (ConversionPair<Long> pair : testCases) {
			String operation = "testing conversion: convert double " + pair.from + " to long";
			logger.debug(operation);
			Assert.assertEquals((long)pair.from, (long)pair.to, operation);
		}
	}
}
