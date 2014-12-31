package com.nc.gs.tests.serializers.reification;

import java.math.BigInteger;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.serializers.java.lang.ArraySerializer;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestArraySerReification extends AbstractRoundTripTests {

	Sink dst = new Sink();

	private Object[] makeMixedArray(int sz) {
		Object[] rv = new Object[sz];

		Random r = new Random();

		for (int i = 0; i < sz; i++) {

			rv[i] = (i & 1) == 0 ? String.valueOf(i) : new BigInteger(String.format("%d%d%d%d", r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE)));
		}

		return rv;
	}

	@Test
	public void testMulti() throws Exception {

		Class<?>[] types = { String.class, BigInteger.class };

		for (boolean x : new boolean[]{ true, false }) {
			for (boolean y : new boolean[]{ true, false }) {
				GraphSerializer gs = rawForArray(types, x, y, true);

				ArraySerializer as = ArraySerializer.NULL_WITH_REFS;

				Object[] coll = makeMixedArray(300);

				roundTrip(gs, coll);

				roundTrip(as, coll);

				Assert.assertArrayEquals(probeNoValidate(as, coll, dst), probeNoValidate(gs, coll, dst));
			}
		}
	}

	@Test
	public void testSimple() throws Exception {

		for (Class<?> t : new Class<?>[]{ BigInteger.class, String.class }) {
			for (boolean x : new boolean[]{ true, false }) {
				for (boolean y : new boolean[]{ true, false }) {
					rawForArray(t, x, y, true);
				}
			}
		}
	}
}