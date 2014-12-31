package com.nc.gs.tests.serializers.reification;

import gnu.trove.map.hash.THashMap;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import symbols.io.abstraction._Tags.ObjectShape;

import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.generator.opt.MultiMSOptimizer;
import com.nc.gs.generator.opt.SimpleMSOptmizer;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.serializers.java.math.BigIntegerSerializer;
import com.nc.gs.serializers.java.util.MapSerializer;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestMapReification extends AbstractRoundTripTests {

	static class PolyComparator implements Comparator<Object> {

		@Override
		public int compare(Object l, Object r) {
			int rv;

			if (l instanceof Byte) {
				if (r instanceof Byte) {
					rv = ((Byte) l).compareTo((Byte) r);
				} else {
					rv = -1;
				}
			} else if (r instanceof Byte) {
				rv = 1;
			} else {
				rv = ((String) l).compareTo((String) r);
			}

			return rv;
		}
	}

	static Object getStaticField(Object o, String n) throws ReflectiveOperationException {
		Field field = o.getClass().getDeclaredField(n);
		field.setAccessible(true);

		return field.get(null);
	}

	Random r = new Random();

	@Before
	public void beforeMethod() {
		org.junit.Assume.assumeTrue(!SerializerFactory.serializer(BigInteger.class).getClass().isSynthetic());
	}

	@SuppressWarnings("unchecked")
	private Map<Object, Object> createMixed(Class<? extends Map<?, ?>> mapType, int sz) {

		Map<Object, Object> rv;

		if (mapType == null) {
			rv = new THashMap<>();
		} else {
			if (SortedMap.class.isAssignableFrom(mapType)) {
				rv = (Map<Object, Object>) SerializerFactory.instantiatorOf(mapType).allocate(sz, new PolyComparator());
			} else {

				rv = (Map<Object, Object>) SerializerFactory.instantiatorOf(mapType).allocate(sz);
			}
		}

		Random r = this.r;

		for (int i = 0; i < sz; i++) {
			Object key = (r.nextInt(Integer.MAX_VALUE) & 1) == 0 ? String.valueOf(i) : new Byte((byte) r.nextInt());

			Object val = (r.nextInt(Integer.MAX_VALUE) & 1) == 0 ? new Long(r.nextLong()) : new BigInteger(String.format("%d%d%d%d", r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE)));

			rv.put(key, val);
		}

		return rv;
	}

	@Test
	public void testMultiple() throws Exception {

		boolean[] ops = new boolean[]{ true, false };

		Class<? extends Map<?, ?>>[] mapTypes = newMapTypeArray(null, HashMap.class, TreeMap.class);

		Shape ks = Shape.stateless(ObjectShape.MAP);
		Shape vs = Shape.stateless(ObjectShape.MAP);

		Class<?>[] keyTypes = { String.class, Byte.class };

		Class<?>[] valTypes = { Long.class, BigInteger.class };

		for (Class<? extends Map<?, ?>> mapType : mapTypes) {
			Map<Object, Object> map = createMixed(mapType, 100 + r.nextInt(200));

			for (boolean x : ops) {
				for (boolean y : ops) {
					for (boolean z : ops) {
						for (boolean w : ops) {

							GraphSerializer opt = MultiMSOptimizer.rawOptimized(mapType, keyTypes, valTypes, ks.with(x, y), vs.with(z, w), true);

							MapSerializer ms = new MapSerializer(mapType, null, null, x, y, z, w);

							roundTrip(ms, map);

							roundTrip(opt, map);

						}
					}
				}
			}
		}
	}

	@Test
	public void testSimple() throws Exception {

		Class<? extends Map<?, ?>>[] mapTypes = newMapTypeArray(TreeMap.class, null, HashMap.class);

		boolean[] ops = new boolean[]{ true, false };

		Shape ks = Shape.stateless(ObjectShape.MAP);
		Shape vs = Shape.stateless(ObjectShape.MAP);

		for (Class<? extends Map<?, ?>> mapType : mapTypes) {

			for (boolean x : ops) {
				for (boolean y : ops) {
					for (boolean z : ops) {
						for (boolean w : ops) {

							GraphSerializer left = SimpleMSOptmizer.rawOptimized(mapType, String.class, ks.with(x, y), BigInteger.class, vs.with(z, w));

							// Non-reified are embeded for delegation
							Assert.assertTrue(getStaticField(left, "vs").getClass() == BigIntegerSerializer.class);

							GraphSerializer right = SimpleMSOptmizer.rawOptimized(mapType, BigInteger.class, ks.with(x, y), String.class, vs.with(z, w));

							Assert.assertTrue(getStaticField(right, "ks").getClass() == BigIntegerSerializer.class);

						}
					}
				}
			}
		}
	}

}
