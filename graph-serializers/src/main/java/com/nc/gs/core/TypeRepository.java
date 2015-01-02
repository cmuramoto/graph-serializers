package com.nc.gs.core;

import static symbols.io.abstraction._Tags.Serializer.TYPE_ID;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.nc.gs.io.Sink;

public interface TypeRepository {

	static final class Default implements TypeRepository {

		static final Comparator<Class<?>> NAME_CMP = (l, r) -> l.getName().compareTo(r.getName());

		static final Comparator<Object> CMP = (left, right) -> {
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

		static final ConcurrentSkipListMap<Object, Class<?>> TYPES = new ConcurrentSkipListMap<>(CMP);

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
		public void writeType(Sink dst, Class<?> type) {
			Object overlaid = unwrap(type);

			if (overlaid == type) {
				dst.writeByte(TYPE_ID);
				dst.writeUTF(type.getName());
			} else {
				Class<?>[] types = (Class<?>[]) overlaid;
				dst.writeVarInt(types.length + 1);
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

	void writeType(Sink dst, Class<?> type);

}
