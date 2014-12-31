package com.nc.gs.tests.serializers.graphs.cols;

import static com.nc.gs.tests.serializers.graphs.cols.TestCollectionDeclarations.randomIntArray;
import static com.nc.gs.tests.serializers.graphs.cols.TestCollectionDeclarations.randomStringArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.core.SerializerFactory;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestReplacingBean extends AbstractRoundTripTests {

	Comparator<String> cmp = new Comparator<String>() {

		@Override
		public int compare(String l, String r) {
			return l.compareTo(r);
		}
	};

	@SuppressWarnings("unchecked")
	private ReplacingCollectionBean randomReplacingCollectionBean(
			Class<?> listType, Class<?> setType, Comparator<String> cmp,
			int sz, boolean wrapInSynchronized) {
		ReplacingCollectionBean bean = new ReplacingCollectionBean();

		String[] keys = randomStringArray(sz);

		String[] vals = randomStringArray(sz);

		List<String> m = (List<String>) SerializerFactory.instantiatorOf(
				listType).allocate(sz);

		for (String k : keys) {
			m.add(k);
		}

		SortedSet<String> ss = (SortedSet<String>) SerializerFactory
				.instantiatorOf(setType).allocate(cmp);

		for (String v : vals) {
			ss.add(v);
		}

		m = wrapInSynchronized ? Collections.synchronizedList(m) : m;

		bean.setOptList(m);

		ss = wrapInSynchronized ? Collections.synchronizedSortedSet(ss) : ss;

		bean.setOptPolySet(ss);

		return bean;
	}

	@SuppressWarnings("unchecked")
	ReplacingMapBean randomReplacingMapBean(Class<?> mapKind,
			Class<?> smapKind, Comparator<String> c, int sz) {

		ReplacingMapBean bean = new ReplacingMapBean();

		String[] keys = randomStringArray(sz);

		String[] vals = randomStringArray(sz);

		Map<String, String> m = (Map<String, String>) SerializerFactory
				.instantiatorOf(mapKind).allocate(sz);

		for (int i = 0; i < keys.length; i++) {
			m.put(keys[i], vals[i]);
		}

		keys = randomStringArray(sz);
		String[] lpvs = randomStringArray(sz / 2);

		SortedMap<String, Object> smap = (SortedMap<String, Object>) SerializerFactory
				.instantiatorOf(smapKind).allocate(c);

		int[] rpvs = randomIntArray(sz / 2);

		int j = 0;

		for (; j < lpvs.length; j++) {
			smap.put(keys[j], lpvs[j]);
		}

		for (int i = 0; i < rpvs.length; i++) {
			smap.put(keys[j++], rpvs[i]);
		}

		bean.setOptMap(m);

		bean.setOptPolyMap(smap);

		return bean;
	}

	@Test
	public void testCollectionReplacements() {

		ReplacingCollectionBean bean = randomReplacingCollectionBean(
				ArrayList.class, ConcurrentSkipListSet.class, cmp, 200, true);

		Assert.assertSame(Collections.synchronizedList(Collections.emptyList()).getClass(), bean.getOptList().getClass());
		Assert.assertSame(Collections.synchronizedSortedSet(new ConcurrentSkipListSet<>()).getClass(), bean.getOptPolySet()
				.getClass());

		ReplacingCollectionBean rec = probeNoValidate(bean);

		Assert.assertSame(LinkedList.class, rec.getOptList().getClass());
		Assert.assertSame(TreeSet.class, rec.getOptPolySet().getClass());

		Assert.assertEquals(new TreeSet<>(bean.getOptList()),
				new TreeSet<>(rec.getOptList()));

		Assert.assertEquals(bean.getOptPolySet(), rec.getOptPolySet());
	}

	@Test
	public void testMapReplacements() {

		ReplacingMapBean bean = randomReplacingMapBean(LinkedHashMap.class,
				ConcurrentSkipListMap.class, cmp, 200);

		Assert.assertSame(LinkedHashMap.class, bean.getOptMap().getClass());
		Assert.assertSame(ConcurrentSkipListMap.class, bean.getOptPolyMap()
				.getClass());

		ReplacingMapBean rec = probeNoValidate(bean);

		Assert.assertSame(HashMap.class, rec.getOptMap().getClass());
		Assert.assertSame(TreeMap.class, rec.getOptPolyMap().getClass());

		Assert.assertEquals(new TreeMap<>(bean.getOptMap()),
				new TreeMap<>(rec.getOptMap()));

		Assert.assertEquals(bean.getOptPolyMap(), rec.getOptPolyMap());

	}

}
