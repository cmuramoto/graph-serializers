package com.nc.gs.tests.serializers.graphs.cols;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nc.gs.core.SerializerFactory;
import com.nc.gs.serializers.java.lang.OpaqueSerializer;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestCollectionDeclarations extends AbstractRoundTripTests {

	static Random r = new Random();

	@SuppressWarnings("rawtypes")
	@BeforeClass
	public static void init() throws Exception {
		Class<? extends Set> sfm = Collections.newSetFromMap(Collections.<String, Boolean> emptyMap()).getClass();
		SerializerFactory.register(sfm, new OpaqueSerializer(sfm, null));

		Class<? extends Map> syncMap = Collections.synchronizedMap(Collections.emptyMap()).getClass();
		SerializerFactory.register(syncMap, new OpaqueSerializer(syncMap, null));

	}

	public static int[] randomIntArray(int sz) {
		int[] rv = new int[sz];

		for (int i = 0; i < rv.length; i++) {
			rv[i] = r.nextInt();
		}

		return rv;
	}

	static String[] randomStringArray(int sz) {

		String[] rv = new String[sz];

		for (int j = 0; j < sz; j++) {
			rv[j] = String.valueOf(r.nextInt());
		}

		return rv;
	}

	private Object randomArrayBean() {
		ArrayBean bean = new ArrayBean();

		bean.setHashes(randomIntArray(r.nextInt(10000)));
		bean.setKeys(randomIntArray(r.nextInt(10000)));
		bean.setVals(randomStringArray(10000));
		bean.setHashStrings(randomStringArray(10000));
		bean.setPolyType(randomStringArray(10000));
		return bean;
	}

	private Object randomMapBean(boolean reallyOpaque) {
		MapBean bean = new MapBean();

		bean.setHashes(randomIntArray(r.nextInt(10000)));
		bean.setKeys(randomIntArray(r.nextInt(10000)));
		bean.setVals(randomStringArray(10000));
		bean.setHashStrings(randomStringArray(10000));
		bean.setPolyType(randomStringArray(10000));
		bean.setOpaqueVals(randomStringArray(10000), reallyOpaque);

		return bean;
	}

	private Object randomSetBean(boolean reallyOpaque) {
		SetBean bean = new SetBean();

		bean.setHashes(randomIntArray(r.nextInt(10000)));
		bean.setKeys(randomIntArray(r.nextInt(10000)));
		bean.setVals(randomStringArray(10000));
		bean.setHashStrings(randomStringArray(10000));
		bean.setPolyType(randomStringArray(10000));
		bean.setOpaqueVals(randomStringArray(10000), reallyOpaque);

		return bean;
	}

	@Test
	public void testArray() {
		roundTrip(randomArrayBean());
	}

	@Test
	public void testBeanWithNonOpaqueSet() {
		roundTrip(randomSetBean(false));
	}

	@Test
	public void testBeanWithOpaqueSet() {
		roundTrip(randomSetBean(true));
	}

	@Test
	public void testMapBeanWithNonOpaqueSet() {
		roundTrip(randomMapBean(false));
	}

	@Test
	public void testMapBeanWithOpaqueSet() {
		roundTrip(randomMapBean(true));
	}

}