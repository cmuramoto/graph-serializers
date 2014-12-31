package com.nc.gs.core;

import static symbols.io.abstraction._Tags.Serializer.TYPE_ID;

import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.nc.gs.util.Utils;

public interface TypeRepository {

	static final class Default implements TypeRepository {

		static final Comparator<Class<?>> NAME_CMP = new Comparator<Class<?>>() {

			@Override
			public int compare(Class<?> l, Class<?> r) {
				return l.getName().compareTo(r.getName());
			}
		};

		static final Comparator<Object> CMP = new Comparator<Object>() {

			@Override
			public int compare(Object left, Object right) {
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
								if ((rv = (l[i].getName().compareTo(r[i]
										.getName()))) != 0) {
									break;
								}
							}
						}
					}
				}
				return rv;
			}
		};

		static final ConcurrentSkipListMap<Object, Class<?>> TYPES = new ConcurrentSkipListMap<>(
				CMP);

		static final ConcurrentHashMap<Class<?>, Object> COMMON = new ConcurrentHashMap<>();

		@Override
		public Class<?> lookup(Class<?>[] intf) {
			return null;
		}

		@Override
		public Class<?> lookup(Object o) {
			return null;
		}

		@Override
		public Class<?> lookup(String fqn) {
			return null;
		}

		@Override
		public Object unwrap(Class<?> t) {
			ConcurrentHashMap<Class<?>, Object> ct = COMMON;
			Object rv = ct.get(t);

			if (rv == null) {
				if (Proxy.isProxyClass(t)) {
					rv = t.getInterfaces();
					Arrays.sort((Class<?>[]) rv, NAME_CMP);
				} else {
					rv = t;
				}
				ct.putIfAbsent(t, rv);
			}

			return rv;
		}

		@Override
		public void writeType(ByteBuffer dst, Class<?> type) {
			Object overlaid = unwrap(type);

			if (overlaid == type) {
				dst.put(TYPE_ID);
				Utils.writeString(dst, type.getName());
			} else {
				Class<?>[] types = (Class<?>[]) overlaid;
				Utils.packI(dst, types.length + 1);
				for (Class<?> t : types) {
					writeType(dst, t);
				}
			}
		}

	}

	Class<?> lookup(Class<?>[] intf);

	Class<?> lookup(Object o);

	Class<?> lookup(String fqn);

	Object unwrap(Class<?> c);

	void writeType(ByteBuffer dst, Class<?> type);

}
