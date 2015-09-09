package com.nc.gs.tests.generator.interned;

import java.math.BigInteger;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.core.Context;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestInternedBean extends AbstractRoundTripTests {

	@Test
	public void run() {

		final InternedBeanHolder h = new InternedBeanHolder();
		h.left = new InternedBean(1, new String("new_str"));
		h.right = new InternedBean(1, new String("new_str"));
		h.leftTag = new String("tag");
		h.rightTag = new String("tag");
		h.leftVal = new BigInteger("1");
		h.rightVal = new BigInteger("1");

		final InternedBeanHolder rec = roundTrip(h);
		Assert.assertSame(rec.left, rec.right);
		Assert.assertSame(rec.leftTag, rec.rightTag);
		Assert.assertSame(rec.leftVal, rec.rightVal);
	}

	@Test
	public void runMultiple() {
		final Sink dst = new Sink(1024 * 1024);

		try (Context c = Context.writing()) {
			for (int i = 0; i < 100; i++) {
				final InternedBeanHolder h = new InternedBeanHolder();
				h.left = new InternedBean(1, new String("new_str"));
				h.right = new InternedBean(1, new String("new_str"));

				c.writeRoot(dst, h);
			}
		}

		Source src = dst.mirror();

		final Set<InternedBean> set = Collections.newSetFromMap(new IdentityHashMap<>());
		try (Context c = Context.reading()) {
			for (int i = 0; i < 100; i++) {
				final InternedBeanHolder h = c.readRoot(src, InternedBeanHolder.class);
				set.add(h.left);
				set.add(h.right);
			}
		}

		Assert.assertEquals(1, set.size());

		Assert.assertEquals(1, set.iterator().next().i);
		Assert.assertEquals("new_str", set.iterator().next().j);
	}
}
