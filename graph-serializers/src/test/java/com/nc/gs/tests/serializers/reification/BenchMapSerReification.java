package com.nc.gs.tests.serializers.reification;

import gnu.trove.map.hash.THashMap;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import symbols.io.abstraction._Tags.ObjectShape;

import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.generator.opt.MultiMSOptimizer;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.io.Sink;
import com.nc.gs.log.Log;
import com.nc.gs.serializers.java.util.MapSerializer;
import com.nc.gs.tests.AbstractRoundTripTests;
import com.nc.gs.tests.StopWatch;

public class BenchMapSerReification extends AbstractRoundTripTests {

	public static class PolyCmp implements Comparator<Object> {

		@Override
		public int compare(Object l, Object r) {
			int rv;
			if (l instanceof Number) {
				if (r instanceof Number) {
					rv = Long.compare(((Number) l).intValue(), ((Number) r).intValue());
				} else {
					rv = -1;
				}
			} else if (r instanceof Number) {
				rv = 1;
			} else {
				rv = ((String) l).compareTo((String) r);
			}

			return rv;
		}

	}

	Sink s = new Sink();

	private static final int MAX_PROBE_LOOPS = 1000;

	Random r = new Random();

	private void compare(Object graph, GraphSerializer optSer, GraphSerializer stdSer) {

		StopWatch std = new StopWatch("std");

		StopWatch opt = new StopWatch("opt");

		for (int i = 0; i < 10; i++) {
			doProbe(graph, optSer, opt, i);
			doProbe(graph, stdSer, std, i);
		}

		Log.info(opt.compareFastest(std));

	}

	private void doProbe(Object graph, GraphSerializer gs, StopWatch sw, int n) {
		sw.start(gs.getClass().getSimpleName() + "#" + n);

		for (int i = 0; i < MAX_PROBE_LOOPS; i++) {
			probeNoValidate(gs, graph, s);
		}

		sw.stop();
	}

	private Object[] makeKeys(int sz) {
		Object[] rv = new Object[sz];

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

			rv[i] = o;
		}

		return rv;
	}

	@SuppressWarnings("unchecked")
	private Map<Object, Object> makeMap(Class<?> kind, int sz) {
		Object[] keys = makeKeys(sz);
		Object[] vals = makeVals(sz);

		Map<Object, Object> m;

		if (kind == null) {
			m = new THashMap<>(sz);
		} else {
			Instantiator ctor = SerializerFactory.instantiatorOf(kind);

			if (SortedMap.class.isAssignableFrom(kind)) {
				m = (Map<Object, Object>) ctor.allocate(sz, new PolyCmp());
			} else {
				m = (Map<Object, Object>) ctor.allocate(sz);
			}
		}

		for (int i = 0; i < keys.length; i++) {
			m.put(keys[i], vals[i]);
		}

		return m;
	}

	private Object[] makeVals(int sz) {
		Object[] rv = new Object[sz];

		for (int i = 0; i < sz; i++) {
			Object o;

			long l = r.nextLong();

			if (l % 3 == 0) {
				o = r.nextDouble();
			} else if (l % 5 == 0) {
				o = Short.valueOf((short) l);
			} else {
				o = new BigInteger(String.format("%d%d%d", r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE)));
			}

			rv[i] = o;
		}

		return rv;
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testMulti() throws Exception {

		Shape ks = Shape.stateless(ObjectShape.MAP);
		Shape vs = Shape.stateless(ObjectShape.MAP);

		Class<?>[] keyTypes = { String.class, Long.class, Integer.class };

		Class<?>[] valTypes = { Short.class, BigInteger.class, Double.class };

		for (Class<? extends Map> c : newMapTypeArray(null, HashMap.class, TreeMap.class)) {
			boolean[] state = new boolean[]{ true, false };
			for (boolean x : state) {
				for (boolean y : state) {
					for (boolean z : state) {
						for (boolean w : state) {
							GraphSerializer ms = MultiMSOptimizer.rawOptimized(c, keyTypes, valTypes, ks.with(x, y), vs.with(z, w), true);

							MapSerializer gs = new MapSerializer(c, null, null, x, y, z, w);

							Map<Object, Object> map = makeMap(c, 300);

							roundTrip(gs, map, s);

							roundTrip(ms, map, s);

							Assert.assertEquals(probeNoValidate(gs, map, s), probeNoValidate(ms, map, s));

							compare(map, ms, gs);
						}
					}
				}
			}
		}
	}
}
