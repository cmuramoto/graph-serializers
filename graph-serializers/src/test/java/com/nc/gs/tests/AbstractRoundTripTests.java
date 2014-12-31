package com.nc.gs.tests;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import symbols.io.abstraction._Tags.ObjectShape;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.interpreter.Hierarchy;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.interpreter.StreamShape;
import com.nc.gs.io.Sink;

public abstract class AbstractRoundTripTests {

	public static void gc() {

		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
	}

	@SuppressWarnings({ "unchecked" })
	public static Class<? extends Map<?, ?>>[] newMapTypeArray(Class<?>... types) {
		for (Class<?> type : types) {
			if (type != null && !Map.class.isAssignableFrom(type)) {
				throw new IllegalArgumentException(type.getName() + " is not a Map");
			}
		}

		return (Class<? extends Map<?, ?>>[]) types;
	}

	public static <T> T probe(GraphSerializer gs, T o) {
		return probe(gs, o, true);
	}

	@SuppressWarnings("unchecked")
	public static <T> T probe(GraphSerializer gs, T o, boolean validate) {
		Sink dst = new Sink(1);

		try (Context c = Context.writing()) {
			gs.writeRoot(c, dst, o);
		}

		T rv;
		try (Context c = Context.reading()) {
			rv = (T) gs.readRoot(c, dst.mirror());
		}

		if (validate) {
			Assert.assertEquals(o, rv);
		}

		return rv;
	}

	@Deprecated
	public static <T> T probe(T o) {
		return probe(o, true);
	}

	@Deprecated
	public static <T> T probe(T o, boolean validate) {
		return probe(SerializerFactory.serializer(o.getClass()), o, validate);
	}

	@Deprecated
	public static <T> T probeNoValidate(GraphSerializer gs, T o) {
		return probe(gs, o, false);
	}

	// probe without overhead of TL-lookups
	@SuppressWarnings("unchecked")
	public static <T> T probeNoValidate(GraphSerializer gs, T o, Sink bb) {
		bb.clear();

		try (Context c = Context.writing()) {
			gs.writeRoot(c, bb, o);
		}

		try (Context c = Context.reading()) {
			return (T) gs.readRoot(c, bb.mirror());
		}

	}

	public static <T> T probeNoValidate(T o) {
		return probe(o, false);
	}

	public static GraphSerializer rawForArray(Class<?> type, boolean nullabeEls, boolean onlyPayload, boolean optimize) {
		return rawForArray(new Class<?>[]{ type }, nullabeEls, onlyPayload, optimize);
	}

	public static GraphSerializer rawForArray(Class<?>[] types, boolean nullabeEls, boolean onlyPayload, boolean optimize) {
		Shape shape = new Shape(null, ObjectShape.ARRAY | (nullabeEls ? ObjectShape.NULLABLE : 0) | (onlyPayload ? ObjectShape.ONLY_PAYLOAD : 0));

		Hierarchy h = Hierarchy.from(types);
		h.complete = true;
		shape.state = h;

		StreamShape ss = new StreamShape(null, (Class<?>) null, shape, false, optimize);

		return SerializerFactory.forArray(ss);
	}

	public static GraphSerializer rawForCollection(@SuppressWarnings("rawtypes") Class<? extends Collection> colType, Class<?> type, boolean nullabeEls, boolean onlyPayload, boolean optimize) {
		return rawForCollection(colType, new Class<?>[]{ type }, nullabeEls, onlyPayload, optimize);
	}

	@SuppressWarnings("unchecked")
	public static GraphSerializer rawForCollection(@SuppressWarnings("rawtypes") Class<? extends Collection> colType, Class<?>[] types, boolean nullabeEls, boolean onlyPayload, boolean optimize) {
		int k = Shape.of((Class<? extends Collection<?>>) colType);

		Shape shape = new Shape(null, k | (nullabeEls ? ObjectShape.NULLABLE : 0) | (onlyPayload ? ObjectShape.ONLY_PAYLOAD : 0) | (optimize ? ObjectShape.OPTIMIZE : 0));

		Hierarchy h = Hierarchy.from(types);
		h.complete = true;
		shape.state = h;

		StreamShape ss = new StreamShape(null, colType, shape, false, optimize);

		return SerializerFactory.forCollection(ss);
	}

	@SuppressWarnings("unchecked")
	public static <T> T roundTrip(GraphSerializer gs, T root) {
		Assert.assertNotNull("Root object cannot be null.", root);

		Object rec = probe(gs, root, false);

		Assert.assertNotSame(rec, root);

		if (root.getClass().isArray()) {
			Assert.assertTrue(Arrays.deepEquals((Object[]) root, (Object[]) rec));
		} else {
			Assert.assertEquals(root, rec);
		}

		return (T) rec;
	}

	@SuppressWarnings("unchecked")
	public static <T> T roundTrip(GraphSerializer gs, T root, Sink dst) {
		Assert.assertNotNull("Root object cannot be null.", root);

		Object rec = probeNoValidate(gs, root, dst);

		Assert.assertNotSame(rec, root);

		if (root.getClass().isArray()) {
			Assert.assertTrue(Arrays.deepEquals((Object[]) root, (Object[]) rec));
		} else {
			Assert.assertEquals(root, rec);
		}

		return (T) rec;
	}

	public static <T> T roundTrip(T root) {
		Assert.assertNotNull("Root object cannot be null.", root);
		return roundTrip(SerializerFactory.serializer(root.getClass()), root);
	}

	public static Shape COLLECTION = Shape.stateless(ObjectShape.COLLECTION);

	public static Shape SET = Shape.stateless(ObjectShape.SET);

	public static Shape ARRAY = Shape.stateless(ObjectShape.ARRAY);

	public static ThreadLocal<ByteBuffer> PROBES = new ThreadLocal<ByteBuffer>() {

		@Override
		protected ByteBuffer initialValue() {
			return ByteBuffer.allocateDirect(16 * 1024 * 1024);
		}
	};

	public static boolean[] flags = new boolean[]{ false, true };

	public static long MAX_BUFFER = 64 * 1024 * 1024;

	@SuppressWarnings({ "unchecked" })
	public Class<? extends Collection<?>>[] newCollTypeArray(Class<?>... types) {
		for (Class<?> type : types) {
			if (type != null && !Collection.class.isAssignableFrom(type)) {
				throw new IllegalArgumentException(type.getName() + " is not a Collection");
			}
		}

		return (Class<? extends Collection<?>>[]) types;
	}

	@SuppressWarnings({ "unchecked" })
	public Class<? extends Set<?>>[] newSetTypeArray(Class<?>... types) {
		for (Class<?> type : types) {
			if (type != null && !Set.class.isAssignableFrom(type)) {
				throw new IllegalArgumentException(type.getName() + " is not a Collection");
			}
		}

		return (Class<? extends Set<?>>[]) types;
	}

}
