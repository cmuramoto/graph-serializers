package com.nc.gs.ds;

import static com.nc.gs.util.Utils.U;

import java.util.Arrays;

import com.nc.gs.config.ProvisionService;
import com.nc.gs.core.Genesis;
import com.nc.gs.log.Log;
import com.nc.gs.util.Bits;
import com.nc.gs.util.Pair;

public abstract class ClassTable {

	static final class AutoMap extends ClassTable {

		final IdentityMap<Class<?>> map;

		Class<?>[] slots;

		int max;

		public AutoMap(Pair<int[], Class<?>[]> mapped) {
			IdentityMap<Class<?>> map = new IdentityMap<>(-1);
			Class<?>[] types;

			int max = 0;

			if (mapped != null) {
				int[] tIds = mapped.k;
				types = mapped.v;

				for (int i = 0; i < tIds.length; i++) {
					int tId = tIds[i];
					if (tId == 0) {
						continue;
					}
					map.put(types[i], tId);

					if (tId > max) {
						max = tId;
					}
				}
			} else {
				types = new Class<?>[256];
			}

			this.max = ++max;
			slots = types;
			this.map = map;
		}

		@Override
		public int id(Class<?> c) {
			int next = max;
			int cid = map.putIfAbsent(c, next);

			if (cid == -1) {
				insert(cid = max++, c);
			}

			return cid;
		}

		final void insert(int ix, Class<?> c) {
			if (ix >= slots.length) {
				slots = Arrays.copyOf(slots, ix * 3 / 2);
			}

			slots[ix] = c;
		}

		@Override
		public Class<?> type(int c) {
			return slots[c];
		}
	}

	static final class FrozenMap extends ClassTable {

		final IdentityMap<Class<?>> map;

		Class<?>[] slots;

		public FrozenMap(Pair<int[], Class<?>[]> mapped) {
			IdentityMap<Class<?>> map = new IdentityMap<>(0);
			Class<?>[] types;
			if (mapped != null) {
				int[] tIds = mapped.k;
				types = mapped.v;
				for (int i = 0; i < tIds.length; i++) {
					int tId = tIds[i];
					map.put(types[i], tId);
				}
			} else {
				types = new Class<?>[1];
			}

			// map.compact();

			slots = types;
			this.map = map;
		}

		@Override
		public int id(Class<?> c) {
			return map.get(c);
		}

		@Override
		public Class<?> type(int c) {
			return slots[c];
		}
	}

	@SuppressWarnings("restriction")
	static final class PerfectHash extends ClassTable {

		static final long B;
		static final int P;
		static final int M;
		static final Class<?>[] S;
		static final boolean COMPATIBLE;

		static {
			Pair<int[], Pair<int[], Class<?>[]>> pair = Genesis.getClassData();

			COMPATIBLE = !ProvisionService.getInstance().isConfigFrozen();

			if (pair == null || pair.k == null) {
				Log.info("Perfect Hash Unavailable for domain!");
				throw new ExceptionInInitializerError();
			}

			int[] tIds = pair.v.k;
			Class<?>[] table = pair.v.v;

			P = pair.k[0];
			M = pair.k[1];
			int bytes = M << 2;
			S = table;
			B = Bits.allocateMemory(bytes);
			U.setMemory(B, bytes, (byte) 0);

			for (int i = 0; i < S.length; i++) {
				int tId = tIds[i];

				if (tId == 0) {
					continue;
				}

				int ix = table[i].hashCode() % P;

				U.putInt(B + (ix << 2), tId);
			}

			Log.info("{Heap Slots: %05d, Native Slots: %d, P: %d}", table.length, M, P);
		}

		@Override
		public int id(Class<?> c) {
			int ix = c.hashCode() % P;

			int tId = ix < M ? U.getInt(B + (ix << 2)) : 0;

			/**
			 * This will check for a hash collision in the case compatible mode is enabled. If not,
			 * we must not check for compatibility because the context will not write types
			 * properly.
			 */
			if (COMPATIBLE && tId != 0 && S[tId] != c) {
				tId = 0;
			}

			return tId;
		}

		@Override
		public Class<?> type(int tId) {
			return S[tId];
		}

	}

	public static ClassTable initBridge(ProvisionService ps) {
		Pair<int[], Pair<int[], Class<?>[]>> cd = ps.getClassData();

		ClassTable rv;

		if (ps.isOnlyAutoMap()) {
			rv = new ClassTable.AutoMap(cd.v);
		} else {
			int[] perfect = cd.k;
			if (perfect != null) {
				rv = new ClassTable.PerfectHash();
			} else {
				rv = new ClassTable.FrozenMap(cd.v);
			}
		}

		return rv;
	}

	public abstract int id(Class<?> c);

	public abstract Class<?> type(int tId);
}