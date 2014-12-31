package com.nc.gs.tests.serializers.java.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.PriorityBlockingQueue;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.io.Sink;
import com.nc.gs.serializers.java.lang.StringSerializer;
import com.nc.gs.serializers.java.util.CollectionSerializer;
import com.nc.gs.serializers.java.util.SetSerializer;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestSortedCollections extends AbstractRoundTripTests {

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

	@BeforeClass
	public static void before() {
		SerializerFactory.register(String.class, new StringSerializer());
	}

	@SuppressWarnings("unchecked")
	static Class<? extends Set<?>> cast(Class<?> c) {
		return (Class<? extends Set<?>>) c;
	}

	Sink dst = new Sink(1024);

	@SuppressWarnings("unchecked")
	@Test
	public void PriorityQueue_Informed_WithCyclicCmp() {

		CyclicCmp cmp = new CyclicCmp();
		List<Collection<String>> samples = Arrays.<Collection<String>> asList(new PriorityQueue<String>(10, cmp), new PriorityBlockingQueue<String>(10, cmp));

		Class<?>[] types = { null, String.class };

		for (Class<?> type : types) {

			for (Collection<String> set : samples) {
				cmp.cyclic = set;

				set.addAll(Arrays.asList("foo", "bar", "roo"));

				for (boolean x : flags) {
					Class<? extends Collection<?>> ct = (Class<? extends Collection<?>>) set.getClass();
					CollectionSerializer cs = new CollectionSerializer(ct, type, x, false);

					Collection<String> rec = probeNoValidate(cs, set, dst);

					Assert.assertArrayEquals(set.toArray(), rec.toArray());

					Object state = Shape.of(rec).state;
					Assert.assertSame(cmp.getClass(), state.getClass());

					Assert.assertSame(rec, ((CyclicCmp) state).cyclic);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void PriorityQueue_Informed_WithNullCmp() {

		List<Collection<String>> samples = Arrays.<Collection<String>> asList(new PriorityQueue<String>(), new PriorityBlockingQueue<String>());

		Class<?>[] types = { null, String.class };

		for (Collection<String> set : samples) {
			set.addAll(Arrays.asList("foo", "bar", "roo"));

			for (Class<?> type : types) {
				for (boolean x : flags) {
					for (boolean y : flags) {
						Class<? extends Collection<?>> ct = (Class<? extends Collection<?>>) set.getClass();
						CollectionSerializer cs = new CollectionSerializer(ct, type, x, y);

						Collection<String> rec = probeNoValidate(cs, set, dst);

						Assert.assertArrayEquals(set.toArray(), rec.toArray());
					}
				}
			}
		}

	}

	@Test
	public void PriorityQueue_NonInformed_WithAcyclicCmp() {

		SimpleCmp cmp = new SimpleCmp();
		List<Collection<String>> samples = Arrays.<Collection<String>> asList(new PriorityQueue<String>(10, cmp), new PriorityBlockingQueue<String>(10, cmp));

		Class<?>[] types = { null, String.class };

		for (Class<?> type : types) {

			for (Collection<String> set : samples) {

				set.addAll(Arrays.asList("foo", "bar", "roo"));

				for (boolean x : flags) {
					for (boolean y : flags) {
						CollectionSerializer cs = new CollectionSerializer(null, type, x, y);

						Collection<String> rec = probeNoValidate(cs, set, dst);

						Assert.assertArrayEquals(set.toArray(), rec.toArray());

						Object state = Shape.of(rec).state;
						Assert.assertSame(cmp.getClass(), state.getClass());
					}
				}
			}
		}
	}

	@Test
	public void PriorityQueue_NonInformed_WithCyclicCmp() {

		CyclicCmp cmp = new CyclicCmp();
		List<Collection<String>> samples = Arrays.<Collection<String>> asList(new PriorityQueue<String>(10, cmp), new PriorityBlockingQueue<String>(10, cmp));

		Class<?>[] types = { null, String.class };

		for (Class<?> type : types) {

			for (Collection<String> set : samples) {
				cmp.cyclic = set;

				set.addAll(Arrays.asList("foo", "bar", "roo"));

				for (boolean x : flags) {
					CollectionSerializer cs = new CollectionSerializer(null, type, x, false);

					Collection<String> rec = probeNoValidate(cs, set, dst);

					Assert.assertArrayEquals(set.toArray(), rec.toArray());

					Object state = Shape.of(rec).state;
					Assert.assertSame(cmp.getClass(), state.getClass());

					Assert.assertSame(rec, ((CyclicCmp) state).cyclic);
				}
			}
		}
	}

	@Test
	public void PriorityQueue_NonInformed_WithNullCmp() {

		List<Collection<String>> samples = Arrays.<Collection<String>> asList(new PriorityQueue<String>(), new PriorityBlockingQueue<String>());

		Class<?>[] types = { null, String.class };

		for (Class<?> type : types) {

			for (Collection<String> set : samples) {

				set.addAll(Arrays.asList("foo", "bar", "roo"));

				for (boolean x : flags) {
					for (boolean y : flags) {
						CollectionSerializer cs = new CollectionSerializer(null, type, x, y);

						Collection<String> rec = probeNoValidate(cs, set, dst);

						Assert.assertArrayEquals(set.toArray(), rec.toArray());
					}
				}
			}
		}
	}

	@Test
	public void Set_Informed_WithAcyclicCmp() {

		SimpleCmp cmp = new SimpleCmp();
		TreeSet<String> set = new TreeSet<>(cmp);

		set.addAll(Arrays.asList("foo", "bar", "roo"));

		Class<? extends Set<?>> setType = cast(TreeSet.class);
		Class<? extends Collection<?>> colType = cast(TreeSet.class);

		for (boolean x : flags) {
			for (boolean y : flags) {
				SetSerializer ss = new SetSerializer(setType, String.class, x, y);
				CollectionSerializer cs = new CollectionSerializer(colType, String.class, x, y);

				SortedSet<String> rec = probe(ss, set);

				Assert.assertNotNull(rec.comparator());

				rec = probe(cs, set);

				Assert.assertNotNull(rec.comparator());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void Set_Informed_WithCyclicCmp() {

		Class<? extends Set<?>>[] types = newSetTypeArray(TreeSet.class, ConcurrentSkipListSet.class);

		for (boolean x : flags) {
			for (Class<? extends Set<?>> type : types) {
				SetSerializer ss = new SetSerializer(type, String.class, x, false);
				CollectionSerializer cs = new CollectionSerializer(cast(TreeSet.class), String.class, x, false);

				CyclicCmp cmp = new CyclicCmp();
				SortedSet<String> set = (SortedSet<String>) SerializerFactory.instantiatorOf(type).allocate(cmp);
				cmp.cyclic = set;

				set.addAll(Arrays.asList("foo", "bar", "roo"));

				SortedSet<String> rec = probe(ss, set);

				Assert.assertNotNull(rec.comparator());
				Assert.assertSame(((CyclicCmp) rec.comparator()).cyclic, rec);

				rec = probe(cs, set);

				Assert.assertNotNull(rec.comparator());
				Assert.assertSame(((CyclicCmp) rec.comparator()).cyclic, rec);
			}
		}

	}

	@Test
	public void Set_Informed_WithNullCmp() {

		TreeSet<String> set = new TreeSet<>();

		set.addAll(Arrays.asList("foo", "bar", "roo"));

		Class<?>[] types = { null, String.class };

		for (Class<?> type : types) {
			for (boolean x : flags) {
				for (boolean y : flags) {
					SetSerializer ss = new SetSerializer(cast(TreeSet.class), type, x, y);

					CollectionSerializer cs = new CollectionSerializer(cast(TreeSet.class), type, x, y);

					TreeSet<String> rec = probe(ss, set);

					Assert.assertEquals(set, rec);

					rec = probe(cs, set);

					Assert.assertEquals(set, rec);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void Set_NonInformed_WithCyclicCmp() {

		Class<? extends Collection<?>>[] types = newCollTypeArray(TreeSet.class, ConcurrentSkipListSet.class);

		for (Class<?> type : types) {
			for (boolean x : flags) {
				CyclicCmp cmp = new CyclicCmp();

				SetSerializer ss = new SetSerializer(null, String.class, x, false);
				CollectionSerializer cs = new CollectionSerializer(null, String.class, x, false);

				SortedSet<String> set = (SortedSet<String>) SerializerFactory.instantiatorOf(type).allocate(cmp);

				cmp.cyclic = set;

				set.addAll(Arrays.asList("foo", "bar", "roo"));

				SortedSet<String> rec = probe(ss, set);

				Assert.assertNotNull(rec.comparator());

				Assert.assertSame(((CyclicCmp) rec.comparator()).cyclic, rec);

				rec = probe(cs, set);

				Assert.assertNotNull(rec.comparator());

				Assert.assertSame(((CyclicCmp) rec.comparator()).cyclic, rec);

			}
		}
	}

	@Test
	public void Set_NonInformed_WithNullCmp() {

		List<SortedSet<String>> samples = Arrays.<SortedSet<String>> asList(new TreeSet<String>(), new ConcurrentSkipListSet<String>());

		for (SortedSet<String> set : samples) {

			set.addAll(Arrays.asList("foo", "bar", "roo"));

			for (boolean x : flags) {
				for (boolean y : flags) {
					SetSerializer ss = new SetSerializer(null, String.class, x, y);
					CollectionSerializer cs = new CollectionSerializer(null, String.class, x, y);

					probe(ss, set);

					probe(cs, set);
				}
			}
		}
	}

}