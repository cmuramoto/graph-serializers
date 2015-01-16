package com.nc.gs.tests.serializers.reification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.log.Log;
import com.nc.gs.serializers.java.util.CollectionSerializer;
import com.nc.gs.tests.AbstractRoundTripTests;
import com.nc.gs.tests.StopWatch;

public class BenchColSerReification extends AbstractRoundTripTests {

	Random r = new Random();

	Sink s = new Sink(1024 * 1024);

	private void compare(Collection<Object> coll, CollectionSerializer cs, GraphSerializer gs) {

		StopWatch std = new StopWatch("std");

		StopWatch opt = new StopWatch("opt");

		for (int i = 0; i < 10; i++) {

			doProbe(coll, gs, opt, i);
			doProbe(coll, cs, std, i);
		}

		Log.info(opt.compareFastest(std));

	}

	private void doProbe(Collection<Object> coll, GraphSerializer gs, StopWatch sw, int n) {

		sw.start(gs.getClass().getSimpleName() + "#" + n);

		for (int i = 0; i < 10000; i++) {
			probeNoValidate(gs, coll, s);
		}

		sw.stop();
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> makeMixedList(Class<?> c, int sz) {
		Collection<Object> rv = c == null ? new ArrayList<>() : (Collection<Object>) SerializerFactory.instantiatorOf(c).allocate(sz);

		for (int i = 0; i < sz; i++) {
			Object o;

			long l = r.nextLong();

			if (l % 3 == 0) {
				o = l;
			} else if (l % 5 == 0) {
				o = Integer.valueOf((int) l);
			} else {
				o = String.valueOf(i);
			}

			rv.add(o);
		}

		return rv;
	}

	@Test
	public void testMulti() throws Exception {

		Class<?>[] types = { String.class, Long.class, Integer.class };

		for (Class<? extends Collection<?>> c : newCollTypeArray(null, ArrayList.class, LinkedList.class)) {
			for (boolean x : new boolean[]{ true, false }) {
				for (boolean y : new boolean[]{ true, false }) {
					GraphSerializer gs = rawForCollection(c, types, x, y, true);

					CollectionSerializer cs = new CollectionSerializer(c, null, x, y);

					Collection<Object> coll = makeMixedList(c, 300);

					roundTrip(gs, coll, s);

					roundTrip(cs, coll, s);

					Assert.assertEquals(probeNoValidate(cs, coll,s), probeNoValidate(gs, coll, s));

					compare(coll, cs, gs);

				}
			}
		}
	}

}
