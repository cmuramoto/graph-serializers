package com.nc.gs.tests.serializers.java.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.core.Context;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.log.Log;
import com.nc.gs.serializers.java.util.MapSerializer;

public class MapVariants /* extends FastBootstrapWithCompatibleFallbackTests */{

	@AfterClass
	public static void log() {
		Set<Entry<Integer, Object[]>> set = SZ_TO_STATE.entrySet();

		for (Entry<Integer, Object[]> e : set) {
			Log.info("Size: %d. Kind: %s", e.getKey(), Arrays.toString(e.getValue()));
		}
	}

	static final int MAX_ENTRIES = 20000;

	static TreeMap<Integer, Object[]> SZ_TO_STATE = new TreeMap<>();

	final Sink bb = new Sink();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, BigInteger> create(Class<? extends Map> mapType) {
		Map<String, BigInteger> map = (Map<String, BigInteger>) SerializerFactory.instantiatorOf(mapType).allocate(2);

		Random r = new Random();
		long mask = ~(1L << 63);

		for (int i = 0; i < MAX_ENTRIES; i++) {
			String n = String.valueOf(r.nextLong() & mask) + String.valueOf(r.nextLong() & mask) + String.valueOf(r.nextLong() & mask);

			map.put("#" + i, new BigInteger(n));
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	private void mapRoundTrip(MapSerializer gs, Map<String, BigInteger> sample, Object[] stateVars) {
		bb.clear();

		try (Context c = Context.writing()) {
			gs.writeRoot(c, bb, sample);
		}

		bb.clear();

		Map<String, BigInteger> rec;

		try (Context c = Context.reading()) {
			rec = (Map<String, BigInteger>) gs.readRoot(c, bb.mirror());
		}

		try {
			Assert.assertEquals(sample, rec);
		} catch (Exception e) {
			Assert.assertEquals(new TreeMap<>(sample), new TreeMap<>(rec));
		}

		SZ_TO_STATE.put(bb.position(), stateVars);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void run() {

		boolean[] state = { true, false };

		List<Class<? extends Map>> maps = new ArrayList<>();

		maps.add(HashMap.class);
		maps.add(LinkedHashMap.class);
		maps.add(Hashtable.class);
		maps.add(TreeMap.class);
		maps.add(ConcurrentHashMap.class);
		maps.add(ConcurrentSkipListMap.class);

		Class<?>[] declaredKeys = { null, String.class };
		Class<?>[] declaredVals = { null, BigInteger.class };

		for (Class<? extends Map> mapType : maps) {
			Map<String, BigInteger> sample = create(mapType);

			for (boolean nK : state) {
				for (boolean opK : state) {
					for (boolean nV : state) {
						for (boolean opV : state) {

							MapSerializer gs;

							for (Class<?> kt : declaredKeys) {
								for (Class<?> vt : declaredVals) {

									Object[] stateVars = { mapType, kt, vt, nK, opK, nV, opV };

									gs = new MapSerializer(mapType, kt, vt, nK, opK, nV, opV);

									mapRoundTrip(gs, sample, stateVars);

									stateVars = stateVars.clone();
									stateVars[0] = null;

									gs = new MapSerializer(null, kt, vt, nK, opK, nV, opV);

									mapRoundTrip(gs, sample, stateVars);

								}
							}
						}
					}
				}
			}
		}
	}
}
