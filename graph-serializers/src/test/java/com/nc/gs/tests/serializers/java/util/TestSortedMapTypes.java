package com.nc.gs.tests.serializers.java.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nc.gs.core.Context;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.log.Log;
import com.nc.gs.serializers.java.lang.StringSerializer;
import com.nc.gs.serializers.java.util.MapSerializer;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestSortedMapTypes extends AbstractRoundTripTests {

	static class CyclicCmp implements Comparator<String> {

		Object cyclic;

		@Override
		public int compare(String l, String r) {
			if (cyclic == null) {
				throw new IllegalStateException();
			}
			return l.compareTo(r);
		}
	}

	static class SimpleCmp implements Comparator<String> {

		@Override
		public int compare(String l, String r) {
			return l.compareTo(r);
		}

	}

	@AfterClass
	public static void after() {
		Log.info("Acyclic Asserts: %d", ACYCLIC_ASSERTS);
		Log.info("Cyclic Asserts: %d", CYCLIC_ASSERTS);
		Log.info("No Cmp Asserts: %d", NO_CMP_ASSERTS);
	}

	@BeforeClass
	public static void before() {
		SerializerFactory.register(String.class, new StringSerializer());
	}

	static int CYCLIC_ASSERTS;

	static int ACYCLIC_ASSERTS;

	static int NO_CMP_ASSERTS;

	void populateMap(Map<String, String> map) {
		for (int i = 0; i < 100; i++) {
			map.put(Integer.toString(i), Integer.toBinaryString(i));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInformedMapWithAcyclicCmp() {

		SimpleCmp cmp = new SimpleCmp();
		TreeMap<String, String> map = new TreeMap<>(cmp);

		populateMap(map);

		Sink bb = new Sink();

		for (boolean x : new boolean[]{ false, true }) {
			for (boolean y : new boolean[]{ false, true }) {
				for (boolean z : new boolean[]{ false, true }) {
					for (boolean w : new boolean[]{ false, true }) {

						MapSerializer ms = new MapSerializer(TreeMap.class, String.class, String.class, x, y, z, w);

						bb.clear();

						try (Context c = Context.writing()) {
							ms.writeRoot(c, bb, map);
						}

						bb.clear();

						SortedMap<String, String> rec;

						try (Context c = Context.reading()) {
							rec = (SortedMap<String, String>) ms.readRoot(c, bb.mirror());
						} catch (Exception e) {
							e.printStackTrace();
							throw e;
						}

						Assert.assertNotNull(rec.comparator());
						Assert.assertEquals(map, rec);
						ACYCLIC_ASSERTS++;
					}
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testInformedMapWithCyclicCmp() {

		List<Class<?>> types = Arrays.<Class<?>> asList(TreeMap.class, ConcurrentSkipListMap.class);

		for (Class<?> type : types) {

			MapSerializer ms = new MapSerializer((Class<? extends Map>) type, String.class, String.class, true, false, true, false);

			CyclicCmp cmp = new CyclicCmp();
			SortedMap<String, String> map = (SortedMap<String, String>) SerializerFactory.instantiatorOf(type).allocate(cmp);
			cmp.cyclic = map;

			populateMap(map);

			Sink bb = new Sink(1024 * 1024);

			try (Context c = Context.writing()) {
				ms.writeRoot(c, bb, map);
			}

			bb.clear();

			SortedMap<String, String> rec;

			try (Context c = Context.reading()) {
				rec = (SortedMap<String, String>) ms.readRoot(c, bb.mirror());
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}

			Assert.assertNotNull(rec.comparator());
			Assert.assertEquals(map, rec);

			Assert.assertSame(((CyclicCmp) rec.comparator()).cyclic, rec);
			CYCLIC_ASSERTS++;
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInformedMapWithNullCmp() {

		SortedMap<String, String> map = new TreeMap<>();

		populateMap(map);

		Sink bb = new Sink(1024 * 1024);

		for (boolean x : new boolean[]{ false, true }) {
			for (boolean y : new boolean[]{ false, true }) {
				for (boolean z : new boolean[]{ false, true }) {
					for (boolean w : new boolean[]{ false, true }) {

						MapSerializer ms = new MapSerializer(TreeMap.class, String.class, String.class, x, y, z, w);

						bb.clear();

						try (Context c = Context.writing()) {
							ms.writeRoot(c, bb, map);
						}

						bb.clear();

						SortedMap<String, String> rec;

						try (Context c = Context.reading()) {
							rec = (SortedMap<String, String>) ms.readRoot(c, bb.mirror());
						} catch (Exception e) {
							e.printStackTrace();
							throw e;
						}

						Assert.assertEquals(map, rec);
						NO_CMP_ASSERTS++;

					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNonInformedMapWithAcyclicCmp() {

		SimpleCmp cmp = new SimpleCmp();
		SortedMap<String, String> map = new TreeMap<>(cmp);

		populateMap(map);

		Sink bb = new Sink(1024 * 1024);

		for (boolean x : new boolean[]{ false, true }) {
			for (boolean y : new boolean[]{ false, true }) {
				for (boolean z : new boolean[]{ false, true }) {
					for (boolean w : new boolean[]{ false, true }) {

						MapSerializer ms = new MapSerializer(TreeMap.class, String.class, String.class, x, y, z, w);

						bb.clear();

						try (Context c = Context.writing()) {
							ms.writeRoot(c, bb, map);
						}

						bb.clear();

						SortedMap<String, String> rec;

						try (Context c = Context.reading()) {
							rec = (SortedMap<String, String>) ms.readRoot(c, bb.mirror());
						} catch (Exception e) {
							e.printStackTrace();
							throw e;
						}

						Assert.assertEquals(map, rec);
						ACYCLIC_ASSERTS++;
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNonInformedMapWithCyclicCmp() {

		List<Class<?>> types = Arrays.<Class<?>> asList(TreeMap.class, ConcurrentSkipListMap.class);

		for (Class<?> type : types) {
			for (boolean x : new boolean[]{ false, true }) {
				for (boolean y : new boolean[]{ false, true }) {
					for (boolean z : new boolean[]{ false, true }) {
						for (boolean w : new boolean[]{ false, true }) {

							MapSerializer ss = new MapSerializer(null, String.class, String.class, x, y, z, w);
							CyclicCmp cmp = new CyclicCmp();

							SortedMap<String, String> map = (SortedMap<String, String>) SerializerFactory.instantiatorOf(type).allocate(cmp);

							cmp.cyclic = map;

							populateMap(map);

							Sink bb = new Sink(1024 * 1024);

							try (Context c = Context.writing()) {
								ss.writeRoot(c, bb, map);
							}

							bb.clear();

							SortedMap<String, String> rec;

							try (Context c = Context.reading()) {
								rec = (SortedMap<String, String>) ss.readRoot(c, bb.mirror());
							} catch (Exception e) {
								e.printStackTrace();
								throw e;
							}

							Assert.assertNotNull(rec.comparator());
							Assert.assertEquals(map, rec);

							Assert.assertSame(((CyclicCmp) rec.comparator()).cyclic, rec);
							ACYCLIC_ASSERTS++;
						}
					}
				}
			}
		}
	}

}
