package com.nc.gs.tests.generator.offline;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import com.nc.gs.generator.ext.OfflineClassWriter;
import com.nc.gs.log.Log;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestCommonSuperClassAlg extends AbstractRoundTripTests {

	/**
	 * Just delegate to the default impl, which calls
	 * {@link Class#forName(String, boolean, ClassLoader)} to calculate common
	 * super class.
	 * 
	 * @author cmuramoto
	 * 
	 */
	class DummyCW extends ClassWriter {

		public DummyCW(ClassReader classReader, int flags) {
			super(classReader, flags);
		}

		public DummyCW(int flags) {
			super(flags);
		}

		@Override
		public String getCommonSuperClass(String type1, String type2) {
			return super.getCommonSuperClass(type1, type2);
		}

	}

	@Test
	public void run() throws Exception {
		OfflineClassWriter ocw = new OfflineClassWriter(0);
		DummyCW dcw = new DummyCW(0);

		String[] lefts = { "java/util/Collection", "java/util/TreeSet",
				"java/util/regex/Pattern" };
		String[] rights = { "java/util/List",
				"java/util/concurrent/ConcurrentSkipListSet",
				"java/lang/String" };

		for (int i = 0; i < lefts.length; i++) {
			String left = lefts[i];
			String right = rights[i];

			String csc = ocw.getCommonSuperClass(left, right);
			String asmCsc = dcw.getCommonSuperClass(left, right);

			Log.info("CSC of : [%s,%s] ==> %s [no-runtime]", left, right, csc);

			Log.info("CSC of : [%s,%s] ==> %s [asm-default]", left, right, csc);

			Assert.assertEquals(asmCsc, csc);

		}

	}
}
