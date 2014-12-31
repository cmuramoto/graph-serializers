package com.nc.gs.core;

import static symbols.io.abstraction._Tags.Serializer.ID_BASE;
import static symbols.io.abstraction._Tags.Serializer.NULL;
import static symbols.io.abstraction._Tags.Serializer.REF;
import static symbols.io.abstraction._Tags.Serializer.TYPE_ID;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

import com.nc.gs.ds.ClassTable;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.log.Log;
import com.nc.gs.util.Bits;
import com.nc.gs.util.Utils;

@SuppressWarnings("restriction")
public final class Context implements AutoCloseable {

	static final class Chunk {

		byte[] hb;
		Source src;
		Sink dst;

		public Chunk() {
			grow(4096);
		}

		public void grow(int moreBytes) {
			if (hb == null) {
				hb = new byte[moreBytes];
			} else {
				hb = Arrays.copyOf(hb, hb.length + moreBytes);
			}
		}

		@SuppressWarnings("unchecked")
		public <T> T read(Context c, DataInput input) throws IOException {
			int id = input.readInt();
			Class<?> type;

			if (id > 0) {
				type = ct.type(id);
			} else {
				type = Utils.forName(input.readUTF());
			}

			int len = input.readInt();
			if (len > hb.length) {
				grow(len);
			}
			input.readFully(hb, 0, len);

			src.refill(hb);

			GraphSerializer gs = c.forType(type);
			return (T) gs.read(c, src);
		}

		public <T> void write(Context c, DataOutput output, T obj) throws IOException {
			dst.clear();

			GraphSerializer gs = c.forType(obj.getClass());

			gs.write(c, dst, obj);

			int id = ct.id(obj.getClass());
			output.writeInt(id);

			if (id <= 0) {
				output.writeUTF(obj.getClass().getName());
			}

			int sz = dst.position();
			output.writeInt(sz);
			dst.flushTo(output);
		}
	}

	private static Context borrow() {
		Context rv = CONTEXTS.poll();

		if (rv == null) {
			rv = new Context();
		}

		return rv;
	}

	public static ClassTable getClassTableImpl() {

		return ct;
	}

	public static Class<?> lookupOrCreate(Object type) {
		Class<?> rv = P_TYPES.get(type);

		if (rv == null) {
			if (type instanceof String) {
				rv = Utils.forName(((String) type).intern());
			} else {
				rv = Proxy.getProxyClass(Thread.currentThread().getContextClassLoader(), (Class<?>[]) type);
			}
			Class<?> old = P_TYPES.putIfAbsent(type, rv);

			if (old != null) {
				rv = old;
			}
		}

		return rv;
	}

	public static Class<?> rawReadType(Source src) {
		int tId = src.readIntP();

		Class<?> rv;

		if (tId == 0) {
			int amount = src.readIntP();

			if (amount == 1) {
				rv = lookupOrCreate(src.readUTF());
			} else {
				Class<?>[] types = new Class<?>[amount - 1];

				for (int i = 0; i < types.length; i++) {
					types[i] = rawReadType(src);
				}

				rv = lookupOrCreate(types);
			}
		} else {
			rv = type(tId);
		}

		return rv;
	}

	public static void rawWriteReplacing(Sink dst, Class<?> type) {
		Class<?> toWrite = R_TYPES.get(type);

		if (toWrite != null) {
			type = toWrite;
		}

		rawWriteType(dst, type);
	}

	public static void rawWriteType(Sink dst, Class<?> type) {
		int tId = typeId(type);

		if (tId == 0) {
			dst.write((byte) 0);

			Object overlaid = unwrap(type);

			if (overlaid == type) {
				dst.write(TYPE_ID);
				dst.writeUTF(type.getName());
			} else {
				Class<?>[] types = (Class<?>[]) overlaid;
				dst.writeIntP(types.length + 1);
				for (Class<?> t : types) {
					rawWriteType(dst, t);
				}
			}
		} else {
			dst.writeIntP(tId);
		}
	}

	public static Context reading() {
		Context c = borrow();
		c.write = false;
		return c;
	}

	public static Class<?> type(int tId) {
		return ct.type(tId);
	}

	public static int typeId(Class<?> c) {
		return ct.id(c);
	}

	static Object unwrap(Class<?> t) {
		ConcurrentHashMap<Class<?>, Object> common = C_TYPES;

		Object rv = common.get(t);

		if (rv == null) {
			if (Proxy.isProxyClass(t)) {
				rv = t.getInterfaces();
				Arrays.sort((Class<?>[]) rv, NAME_CMP);
			} else {
				rv = t;
			}
			if (common.putIfAbsent(t, rv) == null) {
				Log.warn("Registered %s for compatibility.", t.getName());
			}
		}

		return rv;
	}

	public static Context writing() {
		Context c = borrow();
		c.write = true;
		return c;
	}

	public static final ConcurrentHashMap<Class<?>, Object> C_TYPES;

	private static final ConcurrentLinkedQueue<Context> CONTEXTS;

	static final ClassTable ct;

	public static final Comparator<Class<?>> NAME_CMP = (l, r) -> l.getName().compareTo(r.getName());

	public static final ConcurrentSkipListMap<Object, Class<?>> P_TYPES;

	public static final ConcurrentHashMap<Class<?>, Class<?>> R_TYPES;

	static {
		Comparator<Object> A_NAME_CMP = (left, right) -> {
			int rv = 0;

			if (left instanceof String) {
				if (right instanceof String) {
					if (left != right) {
						rv = ((String) left).compareTo((String) right);
					}
				} else {
					rv = -1;
				}
			} else if (right instanceof String) {
				rv = 1;
			} else {
				Class<?>[] l = (Class<?>[]) left;
				Class<?>[] r = (Class<?>[]) right;
				if (l != r) {
					int ll = l.length;
					int rl = r.length;

					if (ll > rl) {
						rv = -1;
					} else if (rl > ll) {
						rv = 1;
					} else {
						for (int i = 0; i < ll; i++) {
							if ((rv = l[i].getName().compareTo(r[i].getName())) != 0) {
								break;
							}
						}
					}
				}
			}
			return rv;
		};
		CONTEXTS = new ConcurrentLinkedQueue<>();
		C_TYPES = new ConcurrentHashMap<>();
		R_TYPES = new ConcurrentHashMap<>();
		P_TYPES = new ConcurrentSkipListMap<>(A_NAME_CMP);

		Utils.U.ensureClassInitialized(Genesis.class);
		ct = Genesis.getClassTableImpl();
	}

	Chunk chunk;

	GraphSerializer[] gsCache = new GraphSerializer[32];

	Instantiator[] instCache = new Instantiator[4];

	TObjectIntCustomHashMap<Object> m = new TObjectIntCustomHashMap<>(IdentityHashingStrategy.INSTANCE, 256, .75f, -1);

	Object[] refs = new Object[256];

	boolean write;

	private Context() {
	}

	@Override
	public void close() {
		if (write) {
			m.clear();
		} else {
			Bits.clearFast(refs);
		}

		CONTEXTS.offer(this);
	}

	public GraphSerializer forNested(Sink dst, Object o) {
		int next = m.size();
		int id = m.putIfAbsent(o, next);

		if (id == -1) {
			dst.write(TYPE_ID);
			Class<? extends Object> type = o.getClass();
			writeType(dst, type);
			dst.writeIntP(next + ID_BASE);
			return forType(type);
		} else {
			dst.writeIntP(id + ID_BASE);
			return null;
		}
	}

	public final GraphSerializer forType(Class<?> c) {
		int tId = ct.id(c);

		return tId == 0 ? SerializerFactory.serializer(c) : forTypeId(tId);
	}

	public final GraphSerializer forTypeId(int id) {
		GraphSerializer rv;
		GraphSerializer[] lc = gsCache;

		if (id >= lc.length) {
			lc = gsCache = Arrays.copyOf(lc, id + 64);
		}

		rv = lc[id];

		if (rv == null) {
			rv = lc[id] = SerializerFactory.serializer(ct.type(id));
		}

		return rv;
	}

	public final GraphSerializer forTypeId(Source src, int tId) {

		return tId == 0 ? SerializerFactory.serializer(resolveType(src)) : forTypeId(tId);
	}

	public final Object from(int id) {
		return refs[id - ID_BASE];
	}

	public Chunk getChunk() {
		Chunk c = chunk;

		if (c == null) {
			c = chunk = new Chunk();
		}

		return c;
	}

	public final int id(Object o) {
		return m.get(o);
	}

	public Instantiator instantiatorOf(Class<?> type) {
		Instantiator rv;

		int tId = typeId(type);

		if (tId == 0) {
			rv = SerializerFactory.instantiatorOf(type);
		} else {
			Instantiator[] cache = instCache;
			int length = cache.length;

			if (tId < length) {
				rv = cache[tId];
				if (rv == null) {
					rv = SerializerFactory.instantiatorOf(type);
					cache[tId] = rv;
				}
			} else {
				rv = SerializerFactory.instantiatorOf(type);
				cache = instCache = Arrays.copyOf(cache, tId << 1);
				cache[tId] = rv;
			}
		}

		return rv;
	}

	public boolean isReferenced(Object o) {
		return m.containsKey(o);
	}

	public final void mark(Object o) {
		m.putIfAbsent(o, m.size());
	}

	public final void mark(Object o, int id) {
		Object[] refs = this.refs;

		if (id > refs.length) {
			refs = this.refs = Arrays.copyOf(refs, Math.max(refs.length * 2, id + 64));
		}

		refs[id - ID_BASE] = o;
	}

	public boolean nullSafeVisited(Sink dst, Object o) {
		int next = m.size();
		int id = m.putIfAbsent(o, next);

		if (id == -1) {
			dst.writeIntP(next + ID_BASE);
			return false;
		} else {
			dst.writeIntP(id + ID_BASE);
			return true;
		}
	}

	<T> T read(DataInput in) throws IOException {
		return getChunk().read(this, in);
	}

	public Object readRefAndData(Source src) {
		int id = src.readIntP();

		Object rv = from(id);

		if (rv == null) {
			int tId = src.readIntP() - TYPE_ID;

			GraphSerializer gs;

			if (tId == 0) {
				gs = forType(resolveType(src));
			} else {
				gs = forTypeId(tId);
			}
			rv = gs.instantiate(src);
			mark(rv, id);
			gs.inflateData(this, src, rv);
		}

		return rv;
	}

	@SuppressWarnings("unchecked")
	public <T> T readRoot(Source src, Class<T> type) {
		return (T) SerializerFactory.serializer(type).readRoot(this, src);
	}

	public Class<?> readType(Source src) {
		int tId = src.readIntP() - TYPE_ID;

		if (tId == 0) {
			return resolveType(src);
		} else {
			return ct.type(tId);
		}
	}

	public Object readTypeAndData(Source src) {
		int tId = src.readIntP() - TYPE_ID;

		GraphSerializer gs = tId == 0 ? SerializerFactory.serializer(resolveType(src)) : forTypeId(tId);

		return gs.readData(this, src);
	}

	public Class<?> readTypeOrNull(Source src) {
		int tId = src.readIntP() - TYPE_ID;

		if (tId == -1) {
			return null;
		} else if (tId == 0) {
			return resolveType(src);
		} else {
			return ct.type(tId);
		}

	}

	private Class<?> resolveType(Source src) {
		int id = src.readIntP();
		Class<?> rv = (Class<?>) from(id);
		if (rv == null) {
			int amount = src.readIntP();
			rv = resolveType(src, amount);
			mark(rv, id);
		}
		return rv;
	}

	private Class<?> resolveType(Source src, int amount) {
		Class<?> rv;
		if (amount == 1) {
			rv = lookupOrCreate(src.readUTF());
		} else {
			Class<?>[] types = new Class<?>[amount - 1];
			for (int i = 0; i < types.length; i++) {
				types[i] = readType(src);
			}
			rv = lookupOrCreate(types);
		}
		return rv;
	}

	public boolean visited(Sink dst, Object o) {
		int next = m.size();
		int id = m.putIfAbsent(o, next);

		if (id == -1) {
			dst.write(REF);
			dst.writeIntP(next + ID_BASE);
			return false;
		} else {
			dst.writeIntP(id + ID_BASE);
			return true;
		}
	}

	<T> void write(DataOutput out, T obj) throws IOException {
		getChunk().write(this, out, obj);
	}

	private void writeForCompatibility(Sink dst, Class<?> type) {
		if (!nullSafeVisited(dst, type)) {

			Object overlaid = unwrap(type);

			if (overlaid == type) {
				dst.write(TYPE_ID);
				dst.writeUTF(type.getName());
			} else {
				Class<?>[] types = (Class<?>[]) overlaid;
				dst.writeIntP(types.length + 1);

				for (Class<?> t : types) {
					writeType(dst, t);
				}
			}
		}
	}

	public void writeRefAndData(Sink dst, Object o) {
		if (!nullSafeVisited(dst, o)) {
			writeTypeAndData(dst, o);
		}
	}

	public <T> void writeRoot(Sink dst, T root) {
		SerializerFactory.serializer(root.getClass()).writeRoot(this, dst, root);
	}

	public void writeType(Sink dst, Class<?> type) {
		int tId = typeId(type);

		dst.writeIntP(TYPE_ID + tId);

		if (tId == 0) {
			writeForCompatibility(dst, type);
		}
	}

	public Class<?> writeType(Sink dst, Object o) {
		Class<?> rv = o.getClass();
		writeType(dst, rv);
		return rv;
	}

	public void writeTypeAndData(Sink dst, Object o) {
		Class<?> type = o.getClass();
		int tId = typeId(type);

		dst.writeIntP(TYPE_ID + tId);

		GraphSerializer gs;
		if (tId == 0) {
			writeForCompatibility(dst, type);
			gs = forType(type);
		} else {
			gs = forTypeId(tId);
		}

		gs.writeData(this, dst, o);
	}

	public Class<?> writeTypeIdOrNull(Sink dst, Object o) {
		if (o == null) {
			dst.write(NULL);
			return null;
		} else {
			Class<? extends Object> type = o.getClass();
			writeType(dst, type);

			return type;
		}
	}
}