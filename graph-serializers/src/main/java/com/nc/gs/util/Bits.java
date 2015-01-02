package com.nc.gs.util;

import static com.nc.gs.util.Utils.U;
import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_BYTE_INDEX_SCALE;
import static sun.misc.Unsafe.ARRAY_CHAR_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_CHAR_INDEX_SCALE;
import static sun.misc.Unsafe.ARRAY_DOUBLE_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_DOUBLE_INDEX_SCALE;
import static sun.misc.Unsafe.ARRAY_FLOAT_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_FLOAT_INDEX_SCALE;
import static sun.misc.Unsafe.ARRAY_INT_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_INT_INDEX_SCALE;
import static sun.misc.Unsafe.ARRAY_LONG_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_LONG_INDEX_SCALE;
import static sun.misc.Unsafe.ARRAY_SHORT_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_SHORT_INDEX_SCALE;
import gnu.trove.map.hash.TLongLongHashMap;

import com.nc.gs.io.Sink;

@SuppressWarnings("restriction")
public final class Bits {

	public static synchronized long allocateMemory(long cap) {
		int ps = U.pageSize();

		boolean pa = sun.misc.VM.isDirectMemoryPageAligned();

		long size = Math.max(1L, cap + (pa ? ps : 0));

		long base = U.allocateMemory(size);

		U.setMemory(base, size, (byte) 0);

		long address;

		if (pa && base % ps != 0) {
			address = base + ps - (base & ps - 1);
		} else {
			address = base;
		}

		if (address != base) {
			ADDR_TO_UNALIGNED.put(address, base);
		}

		return address;
	}

	public static void clearFast(Object[] arr) {
		clearFast(arr, arr.length);
	}

	public static void clearFast(Object[] arr, int len) {
		int count = 0;
		final int length = EMPTY.length;
		while (len - count > length) {
			System.arraycopy(EMPTY, 0, arr, count, length);
			count += length;
		}
		System.arraycopy(EMPTY, 0, arr, count, len - count);
	}

	public static void copyFrom(long base, byte[] src, int off, int len) {
		copyFromArray(base, src, ARRAY_BYTE_BASE_OFFSET, ARRAY_BYTE_INDEX_SCALE, off, len);
	}

	public static void copyFrom(long base, char[] src, int off, int len) {
		copyFromArray(base, src, ARRAY_CHAR_BASE_OFFSET, ARRAY_CHAR_INDEX_SCALE, off, len);
	}

	public static void copyFrom(long base, double[] src, int off, int len) {
		copyFromArray(base, src, ARRAY_DOUBLE_BASE_OFFSET, ARRAY_DOUBLE_INDEX_SCALE, off, len);
	}

	public static void copyFrom(long base, float[] src, int off, int len) {
		copyFromArray(base, src, ARRAY_FLOAT_BASE_OFFSET, ARRAY_FLOAT_INDEX_SCALE, off, len);
	}

	public static void copyFrom(long base, int[] src, int off, int len) {
		copyFromArray(base, src, ARRAY_INT_BASE_OFFSET, ARRAY_INT_INDEX_SCALE, off, len);
	}

	public static void copyFrom(long base, long[] src, int off, int len) {
		copyFromArray(base, src, ARRAY_LONG_BASE_OFFSET, ARRAY_LONG_INDEX_SCALE, off, len);
	}

	public static void copyFrom(long base, short[] src, int off, int len) {
		copyFromArray(base, src, ARRAY_SHORT_BASE_OFFSET, ARRAY_SHORT_INDEX_SCALE, off, len);
	}

	static void copyFromArray(long base, Object src, int baseOff, int scale, int off, int len) {
		long o = baseOff + off;
		long dst = base;
		long cLen = len * scale;
		long l = cLen;
		while (l > 0) {
			long size = cLen > COPY_THRESHOLD ? COPY_THRESHOLD : l;
			U.copyMemory(src, o, null, dst, size);
			l -= size;
			o += size;
			dst += size;
		}
	}

	public static void copyTo(long base, byte[] dst, int off, int len) {
		copyToArray(base, ARRAY_BYTE_BASE_OFFSET, ARRAY_BYTE_INDEX_SCALE, dst, off, len);
	}

	public static void copyTo(long base, char[] dst, int off, int len) {
		copyToArray(base, ARRAY_CHAR_BASE_OFFSET, ARRAY_CHAR_INDEX_SCALE, dst, off, len);
	}

	public static void copyTo(long base, double[] dst, int off, int len) {
		copyToArray(base, ARRAY_DOUBLE_BASE_OFFSET, ARRAY_DOUBLE_INDEX_SCALE, dst, off, len);
	}

	public static void copyTo(long base, float[] dst, int off, int len) {
		copyToArray(base, ARRAY_FLOAT_BASE_OFFSET, ARRAY_FLOAT_INDEX_SCALE, dst, off, len);
	}

	public static void copyTo(long base, int[] dst, int off, int len) {
		copyToArray(base, ARRAY_INT_BASE_OFFSET, ARRAY_INT_INDEX_SCALE, dst, off, len);
	}

	public static void copyTo(long base, long[] dst, int off, int len) {
		copyToArray(base, ARRAY_LONG_BASE_OFFSET, ARRAY_LONG_INDEX_SCALE, dst, off, len);
	}

	public static void copyTo(long base, short[] dst, int off, int len) {
		copyToArray(base, ARRAY_SHORT_BASE_OFFSET, ARRAY_SHORT_INDEX_SCALE, dst, off, len);
	}

	static void copyToArray(long base, int baseOff, int scale, Object dst, int off, int len) {
		long offset = baseOff + off;
		long cLen = len * scale;
		long l = cLen;

		while (l > 0) {
			long size = cLen > COPY_THRESHOLD ? COPY_THRESHOLD : l;
			U.copyMemory(null, base, dst, offset, size);
			l -= size;
			base += size;
			offset += size;
		}
	}

	public static synchronized void freeMemory(long address) {
		long mem = ADDR_TO_UNALIGNED == null ? address : ADDR_TO_UNALIGNED.remove(address);

		mem = mem == -1L ? address : mem;

		U.freeMemory(mem);
	}

	public static long reallocateMemory(long base, long newLim, int sz) {
		return U.reallocateMemory(base, newLim);
	}

	public static void writeBitMask(Sink dst, boolean[] o) {
		int len = o.length;
		int loops = len >>> 6;
		int r = len & 63;

		dst.writeVarInt(loops + (r > 0 ? 1 : 0));

		int ix = 0;
		long fl;

		for (int i = 0; i < loops; i++) {
			fl = 0L;
			for (int j = 0; i < 64; j++) {
				fl |= (o[ix++] ? 1L : 0L) << j;
			}
			dst.writeLong(fl);
		}

		if (r > 0) {
			fl = 0L;
			for (int i = 0; i < r; i++) {
				fl |= (o[ix++] ? 1L : 0L) << i;
			}
			dst.writeLong(fl);
		}
	}

	static final long COPY_THRESHOLD = 1024L * 1024L;

	static final Object[] EMPTY = new Object[4096];

	static final TLongLongHashMap ADDR_TO_UNALIGNED = sun.misc.VM.isDirectMemoryPageAligned() ? new TLongLongHashMap(4, .75f, -1L, -1L) : null;
}