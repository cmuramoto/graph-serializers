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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("restriction")
public final class Bits {

	private static long alignAddress(long base, int ps) {
		long r = base & ps - 1;
		if (r != 0) {
			return base + ps - r;
		}
		return base;
	}

	private static long alignSize(long max, int sz) {
		while ((max & sz - 1) != 0) {
			max = Utils.nextPowerOfTwo(max);
		}
		return max;
	}

	public static synchronized long allocateMemory(long cap) {
		// U.pageSize();

		// boolean pa = sun.misc.VM.isDirectMemoryPageAligned();

		long size = alignSize(cap + 16, 16);

		long base = U.allocateMemory(size);

		U.setMemory(base, size, (byte) 0);

		long address = alignAddress(base, 16);

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

		// This should be faster, however it's not! Probably not an intrinsic.
		// U.setMemory(arr, ARRAY_LONG_BASE_OFFSET, ARRAY_LONG_INDEX_SCALE*len, (byte)0);
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

	public static long expandMemory(long base, int newLim, int pos) {
		long rv = allocateMemory(newLim);

		U.copyMemory(base, rv, pos);

		return rv;
	}

	public static synchronized void freeMemory(long address) {
		Long m = ADDR_TO_UNALIGNED.remove(address);

		long mem = m == null ? address : m;

		U.freeMemory(mem);
	}

	public static final int nextPrime(int key) {
		long b = PRIMES;
		int low = 0;
		int high = PLEN;

		while (low <= high) {
			int mid = low + high >>> 1;
			int midVal = U.getInt(b + (mid << 2));

			if (midVal < key) {
				low = mid + 1;
			} else if (midVal > key) {
				high = mid - 1;
			} else {
				return mid;
			}
		}

		return U.getInt(b + (low + 1 << 2));
	}

	public static synchronized long reallocateMemory(long base, long newLim) {
		Long m = ADDR_TO_UNALIGNED.remove(base);

		long mem = m == null ? base : m;

		mem = U.reallocateMemory(mem, newLim + 16);
		long address = alignAddress(mem, 16);

		if (address != mem) {
			ADDR_TO_UNALIGNED.put(address, mem);
		}

		return address;
	}

	public static final String SIMD_INSN_SET;

	public static final boolean SIMD_AVAILABLE;

	public static final int UTF_VECTORIZATION_THRESHOLD = 128;

	static final long PRIMES;

	static final int PLEN;

	static final long COPY_THRESHOLD = 1024L * 1024L;

	static final Object[] EMPTY = new Object[1024];

	static final Map<Long, Long> ADDR_TO_UNALIGNED;

	static {
		ADDR_TO_UNALIGNED = new TreeMap<>();

		try (InputStream is = Bits.class.getClassLoader().getResourceAsStream("primes.bin")) {
			// available always works in this case
			int av = is.available();

			assert av > 0 && av % 4 == 0;

			byte[] buff = new byte[av];

			int r = 0;
			while ((r = is.read(buff, r, buff.length - r)) > 0) {
				;
			}

			long m = allocateMemory(av);

			IntBuffer buffer = ByteBuffer.wrap(buff).asIntBuffer();

			r = 0;
			while (buffer.hasRemaining()) {
				U.putInt(m + (r << 2), buffer.get());
				r++;
			}

			// copyFromByteArray(m, buff, 0, buff.length);

			PRIMES = m;
			PLEN = av / 4 - 1;
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}

		String flags = Utils.execAndTrapOutput("/bin/sh", "-c", "cat /proc/cpuinfo | grep -m 1 flags");

		if (flags.contains("avx512")) {
			// SSE2:
			// [_mm_loadu_si128,_mm_storeu_si128,_mm_movemask_epi8,_mm_unpacklo_epi8,_mm_unpackhi_epi8]
			// SSE3: [_mm_lddqu_si128]
			SIMD_INSN_SET = "AVX512";
		} else if (flags.contains("avx2")) {
			// AVX: [_mm256_loadu_si256,_mm256_lddqu_si256,_mm256_storeu_si256]
			// AVX2: [_mm256_movemask_epi8,_mm256_unpackhi_epi8,_mm256_unpackhi_epi8]
			SIMD_INSN_SET = "AVX2";
		} else if (flags.contains("sse3")) {
			SIMD_INSN_SET = "SSE3";
		} else {
			SIMD_INSN_SET = null;
		}

		SIMD_AVAILABLE = SIMD_INSN_SET != null;
	}
}