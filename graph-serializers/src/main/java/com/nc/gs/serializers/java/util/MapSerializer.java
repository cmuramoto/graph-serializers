package com.nc.gs.serializers.java.util;

import static com.nc.gs.util.Utils.nullIfNotConcrete;
import static symbols.io.abstraction._Tags.Serializer.NULL;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import symbols.io.abstraction._Tags.ObjectShape;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class MapSerializer extends GraphSerializer {

	public static MapSerializer basic() {
		MapSerializer rv = BASIC;

		if (BASIC == null) {
			rv = BASIC = new MapSerializer(null, null, null, true, false, true, false);
		}
		return rv;
	}

	private static void nsPull(Context c, Source src, GraphSerializer gks, GraphSerializer gvs, Map<Object, Object> m, int nkAt, boolean opK, boolean opV, int sz) {

		Class<?> pcV = null;
		Class<?> pcK = null;

		GraphSerializer vps = null;
		GraphSerializer vks = null;

		GraphSerializer ks = gks;
		GraphSerializer vs = gvs;

		boolean rkt = ks == null;
		boolean rvt = vs == null;

		for (int i = 0; i < sz; i++) {

			Object k;

			if (i == nkAt) {
				k = null;
			} else {
				if (rkt) {
					Class<?> clazz = c.readType(src);
					if (clazz == pcK) {
						ks = vks;
					} else {
						pcK = clazz;
						vks = ks = c.forType(clazz);
					}
				}
				k = opK ? ks.readData(c, src) : ks.read(c, src);
			}

			Object v;

			if (rvt) {
				Class<?> clazz = c.readType(src);
				if (clazz == pcV) {
					vs = vps;
				} else {
					pcV = clazz;
					vps = vs = c.forType(clazz);
				}
			}
			v = opV ? vs.readData(c, src) : vs.read(c, src);

			m.put(k, v);
		}
	}

	private static void nsPullKsVs(Context c, Source src, GraphSerializer ks, GraphSerializer vs, Map<Object, Object> m, int nkAt, boolean opK, boolean opV, int sz) {

		for (int i = 0; i < sz; i++) {
			Object k;
			Object v;

			if (i == nkAt) {
				k = null;
			} else {
				k = opK ? ks.readData(c, src) : ks.read(c, src);
			}

			v = opV ? vs.readData(c, src) : vs.read(c, src);

			m.put(k, v);
		}

	}

	private static int nsPush(Context c, Sink dst, GraphSerializer gks, GraphSerializer gvs, Iterator<Entry<Object, Object>> left, boolean opK, boolean opV, int sz) {
		int n = -1;
		Class<?> pcV = null;
		Class<?> pcK = null;

		GraphSerializer vps = null;
		GraphSerializer vks = null;

		GraphSerializer ks = gks;
		GraphSerializer vs = gvs;

		boolean wkt = ks == null;
		boolean wvt = vs == null;

		for (int i = 0; i < sz; i++) {
			Entry<Object, Object> e = left.next();
			Object k = e.getKey();
			Object v = e.getValue();

			if (k == null) {
				n = i;
			} else {
				if (wkt) {
					Class<?> clazz = c.writeType(dst, k);

					if (clazz == pcK) {
						ks = vks;
					} else {
						pcK = clazz;
						ks = vks = c.forType(clazz);
					}
				}

				if (opK) {
					ks.writeData(c, dst, k);
				} else {
					ks.write(c, dst, k);
				}
			}

			if (wvt) {
				Class<?> clazz = c.writeType(dst, v);

				if (clazz == pcV) {
					vs = vps;
				} else {
					pcV = clazz;
					vs = vps = c.forType(clazz);
				}
			}

			if (opV) {
				vs.writeData(c, dst, v);
			} else {
				vs.write(c, dst, v);
			}
		}

		return n;
	}

	private static int nsPushKsVs(Context c, Sink dst, GraphSerializer ks, GraphSerializer vs, Iterator<Entry<Object, Object>> left, boolean opK, boolean opV, int sz) {
		int n = -1;

		for (int i = 0; i < sz; i++) {
			Entry<Object, Object> e = left.next();
			Object k = e.getKey();
			Object v = e.getValue();

			if (k == null) {
				n = i;
			} else {
				if (opK) {
					ks.writeData(c, dst, k);
				} else {
					ks.write(c, dst, k);
				}
			}

			if (opV) {
				vs.writeData(c, dst, v);
			} else {
				vs.write(c, dst, v);
			}
		}

		return n;
	}

	private static void pull(Context c, Source src, GraphSerializer gks, GraphSerializer gvs, Map<Object, Object> m, int nkAt, boolean opK, boolean opV, int sz) {

		Class<?> pcV = null;
		Class<?> pcK = null;

		GraphSerializer vps = null;
		GraphSerializer vks = null;

		GraphSerializer ks = gks;
		GraphSerializer vs = gvs;

		boolean rkt = ks == null;
		boolean rvt = vs == null;

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int base = i << 6;
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			for (int j = 0; j < max; j++) {

				Object k;

				if (base + j == nkAt) {
					k = null;
				} else {
					if (rkt) {
						Class<?> clazz = c.readType(src);
						if (clazz == pcK) {
							ks = vks;
						} else {
							pcK = clazz;
							vks = ks = c.forType(clazz);
						}
					}
					k = opK ? ks.readData(c, src) : ks.read(c, src);
				}

				Object v;

				if ((fl & 1L << j) == 0) {
					v = null;
				} else {
					if (rvt) {
						Class<?> clazz = c.readType(src);
						if (clazz == pcV) {
							vs = vps;
						} else {
							pcV = clazz;
							vps = vs = c.forType(clazz);
						}
					}
					v = opV ? vs.readData(c, src) : vs.read(c, src);
				}

				m.put(k, v);
			}
		}

	}

	public static void pullKsVs(Context c, Source src, GraphSerializer ks, GraphSerializer vs, Map<Object, Object> m, int nkAt, boolean opK, boolean opV, int sz) {

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int base = i << 6;
			int max = i != loops ? 64 : sz & 63;
			long fl = src.decodeMask(max);

			for (int j = 0; j < max; j++) {
				Object k = base + j == nkAt ? null : opK ? ks.readData(c, src) : ks.read(c, src);
				Object v = (fl & 1L << j) == 0 ? null : opV ? vs.readData(c, src) : vs.read(c, src);

				m.put(k, v);
			}
		}
	}

	public static int push(Context c, Sink dst, GraphSerializer gks, GraphSerializer gvs, Iterator<Entry<Object, Object>> left, boolean opK, boolean opV, int sz) {
		int n = -1;

		Class<?> pcV = null;
		Class<?> pcK = null;

		GraphSerializer vps = null;
		GraphSerializer vks = null;

		GraphSerializer ks = gks;
		GraphSerializer vs = gvs;

		boolean wkt = ks == null;
		boolean wvt = vs == null;

		int loops = sz >>> 6;
		int l = (sz & 63) == 0 ? loops : loops + 1;

		for (int i = 0; i < l; i++) {
			int max = i != loops ? 64 : sz & 63;
			int pos = dst.reserveMask(max);
			long fl = 0;

			for (int j = 0; j < max; j++) {
				Entry<Object, Object> next = left.next();
				Object k = next.getKey();
				Object v = next.getValue();

				if (k == null) {
					n = (i << 6) + j;
				} else {
					if (wkt) {
						Class<?> clazz = c.writeType(dst, k);

						if (clazz == pcK) {
							ks = vks;
						} else {
							pcK = clazz;
							ks = vks = c.forType(clazz);
						}
					}

					if (opK) {
						ks.writeData(c, dst, k);
					} else {
						ks.write(c, dst, k);
					}
				}

				if (v != null) {
					fl |= 1L << j;

					if (wvt) {
						Class<?> clazz = c.writeType(dst, v);

						if (clazz == pcV) {
							vs = vps;
						} else {
							pcV = clazz;
							vs = vps = c.forType(clazz);
						}
					}

					if (opV) {
						vs.writeData(c, dst, v);
					} else {
						vs.write(c, dst, v);
					}
				}
			}

			dst.encodeMask(max, pos, fl);
		}

		return n;
	}

	static final void readEntries(Context c, Source src, Map<Object, Object> m, GraphSerializer ks, GraphSerializer vs, int nkAt, boolean opK, boolean nV, boolean opV, int sz) {

		if (ks != null && vs != null) {
			if (nV) {
				pullKsVs(c, src, ks, vs, m, nkAt, opK, opV, sz);
			} else {
				nsPullKsVs(c, src, ks, vs, m, nkAt, opK, opV, sz);
			}
		} else {
			if (nV) {
				pull(c, src, ks, vs, m, nkAt, opK, opV, sz);
			} else {
				nsPull(c, src, ks, vs, m, nkAt, opK, opV, sz);
			}

		}
	}

	public static void readExtensions(Context c, Source src, Object m, Instantiator ctor) {
		if (m instanceof SortedMap) {
			src.mark();
			if (src.readByte() != NULL) {
				src.reset();
				Comparator<?> cmp = (Comparator<?>) c.readRefAndData(src);

				if (ctor == null) {
					ctor = c.instantiatorOf(m.getClass());
				}

				ctor.copy(ctor.allocate(cmp), m);
			}
		}
	}

	public static final void writeEntries(Context c, Sink dst, Map<Object, Object> m, GraphSerializer ks, GraphSerializer vs, boolean nK, boolean opK, boolean nV, boolean opV) {

		int sz = m.size();

		Set<Entry<Object, Object>> es = m.entrySet();
		Iterator<Entry<Object, Object>> left = es.iterator();

		int nkAt = 0;

		int p = dst.position();

		if (nK) {
			dst.writeInt(-1);
		}

		if (ks != null && vs != null && !nV) {
			nkAt = nsPushKsVs(c, dst, ks, vs, left, opK, opV, sz);
		} else {
			if (nV) {
				nkAt = push(c, dst, ks, vs, left, opK, opV, sz);
			} else {
				nkAt = nsPush(c, dst, ks, vs, left, opK, opV, sz);
			}
		}

		if (nK) {
			dst.writeInt(p, nkAt);
		}

	}

	@SuppressWarnings("rawtypes")
	public static void writeExtensions(Context c, Sink dst, Object o, boolean wt) {

		if (wt) {
			Context.rawWriteReplacing(dst, o.getClass());
			dst.writeByte(o instanceof SortedMap ? ObjectShape.SORTED : ObjectShape.NON_SORTED);
		}

		if (o instanceof SortedMap) {
			Comparator cmp = ((SortedMap) o).comparator();
			if (cmp == null) {
				dst.writeByte(ObjectShape.NON_SORTED);
			} else {
				c.writeRefAndData(dst, cmp);
			}
		}
	}

	static MapSerializer BASIC;

	final Instantiator ctor;

	final GraphSerializer ks;

	final GraphSerializer vs;

	final int fl;

	public MapSerializer(@SuppressWarnings("rawtypes") Class<? extends Map> mapType, Class<?> keyType, Class<?> valType, boolean nK, boolean opK, boolean nV, boolean opV) {

		Class<?> mt = nullIfNotConcrete(mapType);

		ctor = mt == null ? null : SerializerFactory.instantiatorOf(mt);

		ks = SerializerFactory.nullIfNotConcrete(keyType);
		vs = SerializerFactory.nullIfNotConcrete(valType);

		fl = (nK ? 0x1 : 0) | (opK ? 0x2 : 0) | (nV ? 0x4 : 0) | (opV ? 0x8 : 0) | (mt != null ? 0x10 | (SortedMap.class.isAssignableFrom(mt) ? 0x20 : 0) : 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void inflateData(Context c, Source src, Object o) {
		readExtensions(c, src, o, ctor);

		int sz = src.readVarInt();

		if (sz == 0) {
			return;
		}

		int fl = this.fl;

		int nkAt = (fl & 0x1) != 0 ? src.readInt() : -1;

		readEntries(c, src, (Map<Object, Object>) o, ks, vs, nkAt, (fl & 0x2) != 0, (fl & 0x4) != 0, (fl & 0x8) != 0, sz);
	}

	@Override
	public Object instantiate(Source src) {
		Object rv;
		Instantiator ctor = this.ctor;

		boolean sorted = (fl & 0x20) != 0;

		if (ctor == null) {
			ctor = SerializerFactory.instantiatorOf(Context.rawReadType(src));
			sorted = src.readByte() == ObjectShape.SORTED;
		}

		src.mark();

		int v = src.readVarInt();

		if (sorted) {
			rv = v == ObjectShape.NON_SORTED ? ctor.allocate() : ctor.allocateHollow();
		} else {
			rv = ctor.allocate(v);
		}

		src.reset();

		return rv;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeData(Context c, Sink dst, Object o) {
		int fl = this.fl;

		writeExtensions(c, dst, o, (fl & 0x10) == 0);

		Map<Object, Object> map = (Map<Object, Object>) o;

		int sz = map.size();

		dst.writeVarInt(sz);

		if (sz == 0) {
			return;
		}

		writeEntries(c, dst, map, ks, vs, (fl & 0x1) != 0, (fl & 0x2) != 0, (fl & 0x4) != 0, (fl & 0x8) != 0);
	}

}