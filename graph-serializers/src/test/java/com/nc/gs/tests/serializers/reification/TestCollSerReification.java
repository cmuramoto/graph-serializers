package com.nc.gs.tests.serializers.reification;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

import org.junit.Assert;
import org.junit.Test;

import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.log.Log;
import com.nc.gs.serializers.java.util.CollectionSerializer;
import com.nc.gs.tests.AbstractRoundTripTests;

public class TestCollSerReification extends AbstractRoundTripTests {

	class CyclicCmp implements Comparator<Object> {

		Object state;

		@Override
		public int compare(Object l, Object r) {
			if (state == null) {
				throw new IllegalStateException();
			}

			if (l instanceof String) {
				if (r instanceof String) {
					return ((String) l).compareTo((String) r);
				} else {
					return -1;
				}
			} else if (r instanceof String) {
				return 1;
			} else {
				return ((BigInteger) l).compareTo((BigInteger) r);
			}
		}
	}

	static Comparator<Object> acyclic = new Comparator<Object>() {

		@Override
		public int compare(Object l, Object r) {
			if (l instanceof String) {
				if (r instanceof String) {
					return ((String) l).compareTo((String) r);
				} else {
					return -1;
				}
			} else if (r instanceof String) {
				return 1;
			} else {
				return ((BigInteger) l).compareTo((BigInteger) r);
			}
		}
	};;

	@SuppressWarnings("unchecked")
	private Collection<Object> makeBigIntegerList(Class<?> c, int sz, Comparator<Object> cmp) {
		Collection<Object> rv = (Collection<Object>) (//
		c == null ? //
		cmp == null ? //
		new ArrayList<>()
			: //
			new PriorityQueue<>(sz, cmp)
			: cmp == null ? //
			SerializerFactory.instantiatorOf(c).allocate(sz)
				: //
				SerializerFactory.instantiatorOf(c).allocate(sz, cmp));

		if (cmp instanceof CyclicCmp) {
			((CyclicCmp) cmp).state = rv;
		}

		Random r = new Random();

		for (int i = 0; i < sz; i++) {
			rv.add(new BigInteger(String.format("%d%d%d%d", r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE))));
		}

		return rv;
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> makeMixedList(Class<?> c, int sz, Comparator<Object> cmp) {
		Collection<Object> rv = (Collection<Object>) (//
		c == null ? //
		cmp == null ? //
		new ArrayList<>()
			: //
			new PriorityQueue<>(sz, cmp)
			: cmp == null ? //
			SerializerFactory.instantiatorOf(c).allocate(sz)
				: //
				SerializerFactory.instantiatorOf(c).allocate(sz, cmp));

		Log.info("Creating mixed list for %s", rv.getClass().getSimpleName());

		if (cmp instanceof CyclicCmp) {
			((CyclicCmp) cmp).state = rv;
		}

		Random r = new Random();

		for (int i = 0; i < sz; i++) {
			rv.add((i & 1) == 0 ? String.valueOf(i) : new BigInteger(String.format("%d%d%d%d", r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE), r.nextInt(Integer.MAX_VALUE))));
		}

		return rv;
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> makeStringList(Class<?> c, int sz, Comparator<Object> cmp) {
		Collection<Object> rv = (Collection<Object>) (//
		c == null ? //
		cmp == null ? //
		new ArrayList<>()
			: //
			new PriorityQueue<>(sz, cmp)
			: cmp == null ? //
			SerializerFactory.instantiatorOf(c).allocate(sz)
				: //
				SerializerFactory.instantiatorOf(c).allocate(sz, cmp));

		if (cmp instanceof CyclicCmp) {
			((CyclicCmp) cmp).state = rv;
		}

		for (int i = 0; i < sz; i++) {
			rv.add(String.valueOf(i));
		}

		return rv;
	}

	@Test
	public void PriorityQueue_Poly_WithAcyclicCmp() throws Exception {

		Class<?>[] types = { String.class, BigInteger.class };

		for (Class<? extends Collection<?>> c : newCollTypeArray(null, PriorityQueue.class, PriorityBlockingQueue.class)) {
			for (boolean x : new boolean[]{ true, false }) {
				for (boolean y : new boolean[]{ true, false }) {
					GraphSerializer gs = rawForCollection(c, types, x, y, true);

					CollectionSerializer cs = new CollectionSerializer(c, null, x, y);

					Collection<Object> coll = makeMixedList(c, 3, acyclic);

					Assert.assertArrayEquals(coll.toArray(), probeNoValidate(gs, coll).toArray());

					Assert.assertArrayEquals(probeNoValidate(cs, coll).toArray(), probeNoValidate(gs, coll).toArray());

				}
			}
		}
	}

	@Test
	public void PriorityQueue_Poly_WithCyclicCmp() throws Exception {

		Class<?>[] types = { String.class, BigInteger.class };

		for (Class<? extends Collection<?>> c : newCollTypeArray(null, PriorityQueue.class, PriorityBlockingQueue.class)) {
			for (boolean x : new boolean[]{ true, false }) {
				GraphSerializer gs = rawForCollection(c, types, x, false, true);

				CollectionSerializer cs = new CollectionSerializer(c, null, x, false);

				CyclicCmp cmp = new CyclicCmp();
				Collection<Object> coll = makeMixedList(c, 3, cmp);

				Collection<Object> rec = probeNoValidate(gs, coll);

				Assert.assertArrayEquals(coll.toArray(), rec.toArray());

				Assert.assertArrayEquals(probeNoValidate(cs, coll).toArray(), rec.toArray());

				CyclicCmp recCmp = (CyclicCmp) rec.getClass().getDeclaredMethod("comparator").invoke(rec);

				Assert.assertSame(recCmp.state, rec);

			}
		}
	}

	@Test
	public void PriorityQueue_WithAcyclicCmp() throws Exception {

		for (Class<? extends Collection<?>> c : newCollTypeArray(null, PriorityQueue.class, PriorityBlockingQueue.class)) {
			for (Class<?> t : new Class<?>[]{ String.class, BigInteger.class }) {
				for (boolean x : new boolean[]{ true, false }) {
					for (boolean y : new boolean[]{ true, false }) {
						GraphSerializer gs = rawForCollection(c, t, x, y, true);

						Collection<Object> l = t == String.class ? makeStringList(c, 300, acyclic) : makeBigIntegerList(c, 300, acyclic);

						Assert.assertArrayEquals(l.toArray(), probeNoValidate(gs, l).toArray());
					}
				}
			}
		}
	}

	@Test
	public void PriorityQueue_WithCyclicCmp() throws Exception {

		for (Class<? extends Collection<?>> c : newCollTypeArray(null, PriorityQueue.class, PriorityBlockingQueue.class)) {
			for (Class<?> t : new Class<?>[]{ String.class, BigInteger.class }) {
				for (boolean x : flags) {
					GraphSerializer gs = rawForCollection(c, t, x, false, true);

					CyclicCmp cmp = new CyclicCmp();

					Collection<Object> l = t == String.class ? makeStringList(c, 300, cmp) : makeBigIntegerList(c, 300, cmp);

					Collection<Object> rec = probeNoValidate(gs, l);
					Assert.assertArrayEquals(l.toArray(), rec.toArray());

					CyclicCmp recCmp = (CyclicCmp) l.getClass().getDeclaredMethod("comparator").invoke(rec);

					Assert.assertSame(recCmp.state, rec);

				}
			}
		}
	}

	@Test
	public void testMulti() throws Exception {

		Class<?>[] types = { String.class, BigInteger.class };

		for (Class<? extends Collection<?>> c : newCollTypeArray(null, ArrayList.class, LinkedList.class)) {
			for (boolean x : new boolean[]{ true, false }) {
				for (boolean y : new boolean[]{ true, false }) {
					GraphSerializer gs = rawForCollection(c, types, x, y, true);

					CollectionSerializer cs = new CollectionSerializer(c, null, x, y);

					Collection<Object> coll = makeMixedList(c, 3, null);

					Assert.assertArrayEquals(coll.toArray(), probeNoValidate(gs, coll).toArray());

					Assert.assertArrayEquals(probeNoValidate(cs, coll).toArray(), probeNoValidate(gs, coll).toArray());

				}
			}
		}
	}

	@Test
	public void testSimple() throws Exception {

		for (Class<? extends Collection<?>> c : newCollTypeArray(null, ArrayList.class, LinkedList.class, PriorityBlockingQueue.class, PriorityQueue.class)) {
			for (Class<?> t : new Class<?>[]{ String.class, BigInteger.class }) {
				for (boolean x : new boolean[]{ true, false }) {
					for (boolean y : new boolean[]{ true, false }) {
						GraphSerializer gs = rawForCollection(c, t, x, y, true);

						Collection<Object> l = t == String.class ? makeStringList(c, 300, null) : makeBigIntegerList(c, 300, null);

						Assert.assertArrayEquals(l.toArray(), probeNoValidate(gs, l).toArray());
					}
				}
			}
		}
	}
}