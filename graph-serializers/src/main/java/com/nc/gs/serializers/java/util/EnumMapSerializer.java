package com.nc.gs.serializers.java.util;

import static com.nc.gs.util.Utils.U;
import static com.nc.gs.util.Utils.fieldOffset;
import static com.nc.gs.util.Utils.getSharedEnumConstants;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class EnumMapSerializer extends GraphSerializer {

	public static EnumMapSerializer basic() {
		EnumMapSerializer rv = basic;

		if (rv == null) {
			rv = basic = new EnumMapSerializer(null, true, false);
		}

		return rv;
	}

	static final long KT_OFF = fieldOffset(EnumMap.class, "keyType");

	static EnumMapSerializer basic;

	final GraphSerializer vs;

	final int fl;

	public EnumMapSerializer(Class<?> type, boolean nV, boolean opV) {
		vs = type == null ? null : SerializerFactory.serializer(type);

		fl = (nV ? 0x1 : 0) | (opV ? 0x2 : 0);
	}

	@SuppressWarnings({ "restriction", "rawtypes", "unchecked" })
	@Override
	public void inflateData(Context c, Source src, Object o) {
		int sz = src.readIntP();

		if (sz == 0) {
			return;
		}

		EnumMap m = (EnumMap) o;
		Enum[] cts = getSharedEnumConstants((Class) U.getObject(o, KT_OFF));

		GraphSerializer vs = this.vs;
		boolean n = (fl & 0x1) != 0;
		boolean op = (fl & 0x2) != 0;

		if (n) {
			int loops = sz >>> 6;
			int r = sz & 63;
			int max = loops + 1;

			for (int i = 0; i < max; i++) {
				long fl = i < loops ? src.readLong() : src.readLongP();
				int lim = i < loops ? 64 : r;

				for (int j = 0; j < lim; j++) {
					Enum key = cts[src.readIntP()];
					Object v;

					if ((fl & 1L << j) != 0) {
						if (vs == null) {
							v = op ? c.readTypeAndData(src) : c.readRefAndData(src);
						} else {
							v = op ? vs.readData(c, src) : vs.read(c, src);
						}
					} else {
						v = null;
					}

					m.put(key, v);
				}
			}
		} else {
			for (int i = 0; i < sz; i++) {
				Enum key = cts[src.readIntP()];
				Object v;

				if (vs == null) {
					v = op ? c.readTypeAndData(src) : c.readRefAndData(src);
				} else {
					v = op ? vs.readData(c, src) : vs.read(c, src);
				}

				m.put(key, v);
			}
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object instantiate(Source src) {
		return new EnumMap(Context.rawReadType(src));
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "restriction" })
	@Override
	public void writeData(Context c, Sink dst, Object o) {
		EnumMap map = (EnumMap) o;

		Context.rawWriteType(dst, (Class<?>) U.getObject(o, KT_OFF));

		int sz = map.size();

		dst.writeIntP(sz);

		if (sz == 0) {
			return;
		}

		Set<Entry<Enum<?>, Object>> es = map.entrySet();

		GraphSerializer vs = this.vs;
		boolean n = (fl & 0x1) != 0;
		boolean op = (fl & 0x2) != 0;

		if (n) {
			int loops = sz >>> 6;
					int r = sz & 63;

					Iterator<Entry<Enum<?>, Object>> itr = es.iterator();

					for (int i = 0; i < loops; i++) {
						long fl = 0L;
						dst.writeLong(0);
						int p = dst.position();

						for (int j = 0; j < 64; j++) {

							Entry<Enum<?>, Object> e = itr.next();
							Object v = e.getValue();

							dst.writeIntP(e.getKey().ordinal());

							if (v != null) {
								fl |= 1L << j;

								GraphSerializer gs;

								if (vs == null) {
									Class<? extends Object> type = v.getClass();
									c.writeType(dst, type);
									gs = c.forType(type);
								} else {
									gs = vs;
								}

								if (op) {
									gs.writeData(c, dst, v);
								} else {
									gs.write(c, dst, v);
								}
							}
						}
						dst.writeLong(p, fl);
					}

					if (r > 0) {
						Entry<Enum<?>, Object>[] arr = new Entry[r];
						long fl = -1L >>> r;

						for (int j = 0; j < r; j++) {
							arr[j] = itr.next();
							Object v = arr[j].getValue();
							if (v == null) {
								fl &= ~(1L << j);
							}
						}

						dst.writeLongP(fl);

						for (int i = 0; i < arr.length; i++) {
							Entry<Enum<?>, Object> e = arr[i];

							dst.writeIntP(e.getKey().ordinal());

							Object v = e.getValue();
							if (v != null) {

								GraphSerializer gs;

								if (vs != null) {
									gs = vs;
								} else {
									Class<? extends Object> type = v.getClass();
									c.writeType(dst, type);
									gs = c.forType(type);
								}

								if (op) {
									gs.writeData(c, dst, v);
								} else {
									gs.write(c, dst, v);
								}
							}
						}
					}
		} else {
			for (Entry<Enum<?>, Object> e : es) {
				Enum<?> k = e.getKey();
				Object v = e.getValue();

				dst.writeIntP(k.ordinal());

				if (vs == null) {
					if (op) {
						c.writeTypeAndData(dst, v);
					} else {
						c.writeRefAndData(dst, v);
					}
				} else {
					if (op) {
						vs.writeData(c, dst, o);
					} else {
						vs.write(c, dst, o);
					}
				}
			}
		}
	}
}