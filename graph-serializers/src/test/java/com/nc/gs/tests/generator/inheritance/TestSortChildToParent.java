package com.nc.gs.tests.generator.inheritance;

import static java.util.Arrays.binarySearch;

import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.interpreter.Hierarchy;
import com.nc.gs.tests.generator.inheritance.Root.A;
import com.nc.gs.tests.generator.inheritance.Root.A_0;
import com.nc.gs.tests.generator.inheritance.Root.A_0_0;
import com.nc.gs.tests.generator.inheritance.Root.B;
import com.nc.gs.tests.generator.inheritance.Root.B_0;
import com.nc.gs.tests.generator.inheritance.Root.B_1;
import com.nc.gs.tests.generator.inheritance.Root.C;
import com.nc.gs.tests.generator.inheritance.Root.C_0;
import com.nc.gs.tests.generator.inheritance.Root.C_0_0;
import com.nc.gs.tests.generator.inheritance.Root.C_0_0_0;
import com.nc.gs.tests.generator.inheritance.Root.D;

public class TestSortChildToParent {

	@Test
	public void run() {

		Class<?>[] types = { Root.class, A.class, B.class, C.class, D.class, A_0.class, A_0_0.class, B_0.class, B_1.class, C_0.class, C_0_0.class, C_0_0_0.class };

		Hierarchy h = new Hierarchy(Root.class, types.clone(), true);

		Class<?>[] sorted = h.runtimeTypes();

		testHierarchy(h.cmp(), sorted, A.class, A_0.class, A_0_0.class);
		testHierarchy(h.cmp(), sorted, B.class, B_0.class, B_1.class);
		testHierarchy(h.cmp(), sorted, C.class, C_0.class, C_0_0_0.class);
	}

	private void testHierarchy(Comparator<Class<?>> cmp, Class<?>[] sorted, Class<?> top, Class<?>... child) {
		int l = binarySearch(sorted, Root.class, cmp);
		int p = binarySearch(sorted, top, cmp);

		Assert.assertTrue(p >= 0);
		Assert.assertTrue(l >= p);

		for (Class<?> c : child) {
			int s;
			Assert.assertTrue(((s = binarySearch(sorted, c, cmp)) >= 0) && (s < p));
		}

	}

}