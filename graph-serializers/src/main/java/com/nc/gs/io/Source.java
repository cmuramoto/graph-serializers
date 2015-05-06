package com.nc.gs.io;

import static com.nc.gs.util.Utils.U;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import org.objectweb.asm.Type;

import sun.misc.Unsafe;

import com.nc.gs.util.Bits;
import com.nc.gs.util.Utils;

@SuppressWarnings("restriction")
public final class Source extends InputStream implements Closeable, DataInput {

	static final boolean isAsciiTuple(int s) {
		return (s & 0XFF) <= 0X7F && (s >> 8 & 0XFF) <= 0X7F;
	}

	static final boolean isAsciiTuple(long l) {
		return (l & 0xFF) <= 0X7F && (l >> 8 & 0xFF) <= 0X7F && (l >> 16 & 0xFF) <= 0X7F && (l >> 24 & 0xFF) <= 0X7F;
	}

	@SuppressWarnings("resource")
	public static Source of(byte[] hb) {
		return new Source(hb.length).filledWith(hb);
	}

	@SuppressWarnings("resource")
	public static Source of(InputStream src) throws IOException {
		return new Source().filledWith(src);
	}

	public static Source of(MappedByteBuffer bb) {
		return new Source(Utils.address(bb), bb.capacity());
	}

	static final int zeroExtend(int t) {
		return t & 0xFF | (t & 0XFF00) << 8;
	}

	static final long zeroExtend(long l) {
		return l & 0xFF | (l & 0XFF00) << 8 | (l & 0XFF0000) << 16 | (l & 0xFF000000) << 24;
	}

	static final long ADDRESS_OFF = Utils.fieldOffset(Buffer.class, "address");

	static final long ARRAY_CHAR_BASE_OFFSET = sun.misc.Unsafe.ARRAY_CHAR_BASE_OFFSET;

	static final int MAX_ASCII = 0x7F;

	byte[] chunk;

	protected boolean disposable;

	protected long base;

	protected long utfBuffer;

	protected int utfLen;

	protected int pos;

	protected int mark;

	protected int lim;

	static RuntimeException W = new RuntimeException("Wrong VLen Encoding");

	public Source() {
		this(4096);
	}

	public Source(int initialSize) {
		lim = Utils.nextPowerOfTwo(initialSize);
		base = Bits.allocateMemory(lim);
	}

	Source(long address, int capacity) {
		base = address;
		lim = capacity;
	}

	private long advance(int req) {
		if (lim - pos < req) {
			throw new BufferUnderflowException();
		}

		int p = pos;
		pos += req;

		return base + p;
	}

	@Override
	public int available() {
		return lim - pos;
	}

	private byte[] chunk() {
		byte[] c = chunk;

		if (c == null) {
			c = chunk = new byte[16 * 1024];
		}

		return c;
	}

	public Source clear() {
		pos = mark = 0;
		return this;
	}

	@Override
	public void close() {
		pos = mark = 0;

		if (disposable) {
			Bits.freeMemory(base);
		}
	}

	public long decodeMask(int max) {
		return max < 48 ? readVarLong() : readLong();
	}

	public Source disposable() {
		disposable = true;
		return this;
	}

	public Source filledWith(byte[] hb) {
		return filledWith(hb, 0, hb.length);
	}

	public Source filledWith(byte[] hb, int off, int len) {
		fillGuard(len);

		Bits.copyFrom(base, hb, off, len);

		return clear();
	}

	public Source filledWith(ByteBuffer src, boolean zeroCopy) {
		clear();
		int lim = src.limit();

		if (zeroCopy) {
			if (!src.isDirect()) {
				throw new IllegalArgumentException(src.getClass().getName());
			}

			base = U.getLong(src, ADDRESS_OFF);
		} else {
			fillGuard(lim);

			if (src.isDirect()) {
				U.copyMemory(U.getLong(src, ADDRESS_OFF), base, lim);
			} else {
				Bits.copyFrom(base, src.array(), 0, lim);
			}
		}

		this.lim = lim;

		return this;
	}

	public Source filledWith(InputStream is) throws IOException {
		byte[] b = chunk();

		int r = 0;

		reset();
		while ((r = is.read(b)) > 0) {
			fillGuard(r);

			Bits.copyFrom(advance(r), b, 0, r);
		}
		reset();

		return this;
	}

	private void fillGuard(int req) {
		if (req > lim) {
			int newLim = Utils.nextPowerOfTwo(lim + req);
			base = Bits.reallocateMemory(base, newLim);
			lim = newLim;
		}
	}

	private void guard(int req) {
		if (lim - pos < req) {
			throw new BufferUnderflowException();
		}
	}

	private void inflate(boolean[] o) {
		int loops = readVarInt();

		int ix = 0;

		for (int i = 0; i < loops; i++) {
			long l = readLong();
			for (int j = 0; j < 64 && ix < o.length; j++, ix++) {
				o[ix] = (l & 1L << j) != 0;
			}
		}
	}

	public void inflate(byte[] o) {
		Bits.copyTo(advance(o.length), o, 0, o.length);
	}

	public void inflate(char[] o) {
		Bits.copyTo(advance(o.length << 1), o, 0, o.length);
	}

	public void inflate(double[] o) {
		Bits.copyTo(advance(o.length << 3), o, 0, o.length);
	}

	public void inflate(float[] o) {
		Bits.copyTo(advance(o.length << 2), o, 0, o.length);
	}

	public void inflate(int[] o) {
		Bits.copyTo(advance(o.length << 2), o, 0, o.length);
	}

	public void inflate(long[] o) {
		Bits.copyTo(advance(o.length << 3), o, 0, o.length);
	}

	public void inflate(short[] o) {
		Bits.copyTo(advance(o.length << 1), o, 0, o.length);
	}

	public void inflateCharScalar(char[] chars) {
		int ix = tryReadAscii(chars);

		if (ix < chars.length) {
			long p = base + pos;

			int c;
			Unsafe u = U;

			do {
				guard(1);
				c = u.getByte(p++) & 0xFF;

				if (c <= 0x7F) {
					chars[ix++] = (char) c;
				} else if (c >> 4 < 14) {
					guard(1);
					chars[ix++] = (char) ((c & 0x1F) << 6 | u.getByte(p++) & 0x3F);
				} else {
					guard(2);
					chars[ix++] = (char) ((c & 0x0F) << 12 | (u.getByte(p++) & 0x3F) << 6 | (u.getByte(p++) & 0x3F) << 0);
				}
			} while (ix < chars.length);

			pos = (int) (p - base);
		}

	}

	public void inflateCharSIMD(char[] chars) {
		pos = (int) (UTF8Util.utf8ToArray(base + pos, chars) - base);
	}

	public void inflatePrimiteArray(int k, Object o) {
		switch (k) {
		case Type.BOOLEAN:
			inflate((boolean[]) o);
			break;
		case Type.CHAR:
			inflate((char[]) o);
			break;
		case Type.BYTE:
			inflate((byte[]) o);
			break;
		case Type.SHORT:
			inflate((short[]) o);
			break;
		case Type.INT:
			inflate((int[]) o);
			break;
		case Type.FLOAT:
			inflate((float[]) o);
			break;
		case Type.LONG:
			inflate((long[]) o);
			break;
		case Type.DOUBLE:
			inflate((double[]) o);
			break;
		default:
		}
	}

	public final void limit(int newLim) {
		lim = newLim;
	}

	public void mark() {
		mark = pos;
	}

	@Override
	public void mark(int readAheadLimit) {
		mark = pos;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	public Sink mirror() {
		return new Sink(base, lim);
	}

	@Override
	public int read() {
		return pos < lim ? U.getByte(base + pos++) & 0xff : -1;
	}

	@Override
	public int read(byte dst[], int off, int len) {
		if (dst == null || off < 0 || len < 0 || len > dst.length - off) {
			throw new IndexOutOfBoundsException();
		}

		long avail = lim - pos;

		if (avail <= 0) {
			return -1;
		}

		if (len <= 0) {
			return 0;
		}

		if (len > avail) {
			len = (int) avail;
		}

		Bits.copyTo(base, dst, off, len);

		pos += len;
		return len;
	}

	// TODO - Bounds checking
	int readAsciiVec(char[] chars) {
		int ix = 0;
		Unsafe u = U;
		int len = chars.length >> 3;

		int dp = 0;

		for (int i = 0; i < len; i++) {
			long txt = readLong();

			long r;

			if (isAsciiTuple(r = txt & 0xFFFFFFFFL)) {
				u.putLong(chars, ARRAY_CHAR_BASE_OFFSET + (ix << 1), zeroExtend(r));
				ix += 4;
				if (isAsciiTuple(r = txt >> 32)) {
					u.putLong(chars, ARRAY_CHAR_BASE_OFFSET + (ix << 1), zeroExtend(r));
					ix += 4;
				} else {
					dp = 4;
					break;
				}
			} else {
				dp = 8;
				break;
			}
		}

		if (dp == 0) {
			switch (chars.length & 7) {
			case 1: {
				char c = (char) readByte();
				if (c <= 0X7F) {
					chars[ix++] = c;
				} else {
					dp = 1;
				}
				break;
			}
			case 2: {
				int s = readShort();

				if (isAsciiTuple(s)) {
					u.putInt(chars, ARRAY_CHAR_BASE_OFFSET + (ix << 1), zeroExtend(s));
					ix += 2;
				} else {
					dp = 2;
				}
				break;
			}
			case 3: {
				int s = readShort();
				char c;

				if (isAsciiTuple(s)) {
					u.putInt(chars, ARRAY_CHAR_BASE_OFFSET + (ix << 1), zeroExtend(s));
					ix += 2;
					if ((c = (char) readByte()) <= 0X7F) {
						chars[ix++] = c;
					} else {
						dp = 1;
					}
				} else {
					dp = 2;
				}
				break;
			}
			case 4: {
				long s = readInt();

				if (isAsciiTuple(s)) {
					u.putLong(chars, ARRAY_CHAR_BASE_OFFSET + (ix << 1), zeroExtend(s));
					ix += 4;
				} else {
					dp = 4;
				}
				break;
			}
			case 5: {
				long s = readInt();
				char c;

				if (isAsciiTuple(s)) {
					u.putLong(chars, ARRAY_CHAR_BASE_OFFSET + (ix << 1), zeroExtend(s));
					ix += 4;
					if ((c = (char) readByte()) <= 0x7F) {
						chars[ix++] = c;
					} else {
						dp = 1;
					}
				} else {
					dp = 4;
				}

				break;
			}
			case 6: {
				long s = readInt();
				int t;

				if (isAsciiTuple(s)) {
					u.putLong(chars, ARRAY_CHAR_BASE_OFFSET + (ix << 1), zeroExtend(s));
					ix += 4;
					if (isAsciiTuple(t = readShort())) {
						u.putInt(chars, ARRAY_CHAR_BASE_OFFSET + (ix << 1), zeroExtend(t));
						ix += 2;
					} else {
						dp = 2;
					}
				} else {
					dp = 4;
				}

				break;
			}
			case 7: {
				long s = readInt();
				int t;
				char c;

				if (isAsciiTuple(s)) {
					u.putLong(chars, ARRAY_CHAR_BASE_OFFSET + (ix << 1), zeroExtend(s));
					ix += 4;
					if (isAsciiTuple(t = readShort())) { //
						u.putInt(chars, ARRAY_CHAR_BASE_OFFSET + (ix << 1), zeroExtend(t));
						ix += 2;
						if ((c = (char) readByte()) <= 0X7F) {
							chars[ix++] = c;
						} else {
							dp = 1;
						}
					} else {
						dp = 2;
					}
				} else {
					dp = 4;
				}

			}
			default:
				break;
			}
		}

		pos -= dp;
		return ix;
	}

	@Override
	public boolean readBoolean() {
		return readByte() == 1;
	}

	@Override
	public byte readByte() {
		return U.getByte(advance(1));
	}

	@Override
	public char readChar() {
		return U.getChar(advance(2));
	}

	public String readChars() {
		int len = readVarInt();
		char[] dst = new char[len];
		Bits.copyTo(advance(len << 1), dst, 0, len);

		return Utils.allocateString(dst);
	}

	@Override
	public double readDouble() {
		return U.getDouble(advance(8));
	}

	@Override
	public float readFloat() {
		return U.getFloat(advance(4));
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		Bits.copyFrom(advance(len), b, off, len);
	}

	@Override
	public int readInt() {
		return U.getInt(advance(4));
	}

	public int[] readIntArray() {
		int len = readVarInt();
		int[] rv = new int[len];
		inflate(rv);

		return rv;
	}

	@Override
	public String readLine() throws IOException {
		return null;
	}

	@Override
	public final long readLong() {
		guard(8);
		long v = U.getLong(base + pos);
		pos += 8;
		return v;
	}

	public long[] readLongArray() {
		int len = readVarInt();
		long[] rv = new long[len];
		inflate(rv);

		return rv;
	}

	private long readRawVarint64SlowPath() {
		long result = 0;
		for (int shift = 0; shift < 64; shift += 7) {
			final byte b = readByte();
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return result;
			}
		}
		throw W;
	}

	@Override
	public short readShort() {
		return U.getShort(advance(2));
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return 0;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return 0;
	}

	@Override
	public String readUTF() {
		final int len = readVarInt();
		char[] chars = new char[len];

		if (Bits.SIMD_AVAILABLE && len >= Bits.UTF_VECTORIZATION_THRESHOLD) {
			inflateCharSIMD(chars);
		} else {
			inflateCharScalar(chars);
		}

		return Utils.allocateString(chars);
	}

	// protobuf impl
	public final long readVaLongP() {
		fastpath: {
			if (pos == lim) {
				break fastpath;
			}

			Unsafe u = U;
			long p = base + pos;

			long x;
			int y;
			if ((y = u.getByte(p++)) >= 0) {
				pos++;
				return y;
			} else if (lim - pos < 10) {
				break fastpath;
			} else if ((x = y ^ u.getByte(p++) << 7) < 0L) {
				x ^= ~0L << 7;
			} else if ((x ^= u.getByte(p++) << 14) >= 0L) {
				x ^= ~0L << 7 ^ ~0L << 14;
			} else if ((x ^= u.getByte(p++) << 21) < 0L) {
				x ^= ~0L << 7 ^ ~0L << 14 ^ ~0L << 21;
			} else if ((x ^= (long) u.getByte(p++) << 28) >= 0L) {
				x ^= ~0L << 7 ^ ~0L << 14 ^ ~0L << 21 ^ ~0L << 28;
			} else if ((x ^= (long) u.getByte(p++) << 35) < 0L) {
				x ^= ~0L << 7 ^ ~0L << 14 ^ ~0L << 21 ^ ~0L << 28 ^ ~0L << 35;
			} else if ((x ^= (long) u.getByte(p++) << 42) >= 0L) {
				x ^= ~0L << 7 ^ ~0L << 14 ^ ~0L << 21 ^ ~0L << 28 ^ ~0L << 35 ^ ~0L << 42;
			} else if ((x ^= (long) u.getByte(p++) << 49) < 0L) {
				x ^= ~0L << 7 ^ ~0L << 14 ^ ~0L << 21 ^ ~0L << 28 ^ ~0L << 35 ^ ~0L << 42 ^ ~0L << 49;
			} else {
				x ^= (long) u.getByte(p++) << 56;
				x ^= ~0L << 7 ^ ~0L << 14 ^ ~0L << 21 ^ ~0L << 28 ^ ~0L << 35 ^ ~0L << 42 ^ ~0L << 49 ^ ~0L << 56;
				if (x < 0L) {
					if (u.getByte(p++) < 0L) {
						break fastpath; // Will throw malformedVarint()
					}
				}
			}
			pos = (int) (p - base);
			return x;
		}

		return readRawVarint64SlowPath();
	}

	public char readVarChar() {
		int c = readByte() & 0xff;
		char rv;

		switch (c >> 4) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
			rv = (char) c;
			break;
		case 12:
		case 13:
			rv = (char) ((c & 0x1F) << 6 | readByte() & 0x3F);
			break;
		case 14:
			rv = (char) ((c & 0x0F) << 12 | (readByte() & 0x3F) << 6 | (readByte() & 0x3F) << 0);
			break;
		default:// wrong encoding??:
			rv = '\0';
		}

		return rv;
	}

	public double readVarDouble() {
		return readDouble();
	}

	public float readVarFloat() {
		return readFloat();
	}

	public int readVarInt() {
		guard(1);

		Unsafe u = U;
		long p = base + pos;

		int b;
		int v = (b = u.getByte(p++)) & 0x7F;
		if ((b & 0x80) != 0) {
			guard(1);
			v |= ((b = u.getByte(p++)) & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				guard(1);
				v |= ((b = u.getByte(p++)) & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					guard(1);
					v |= ((b = u.getByte(p++)) & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						guard(1);
						v |= (u.getByte(p++) & 0x7F) << 28;
					}
				}
			}
		}

		pos = (int) (p - base);

		return v;
	}

	// protobuf impl
	public int readVarIntP() {

		Unsafe u = U;

		long p = base + pos;

		fastpath: {
			if (pos == lim) {
				break fastpath;
			}

			int x;
			if ((x = U.getByte(p++)) >= 0) {
				pos++;
				return x;
			} else if (lim - pos < 10) {
				break fastpath;
			} else if ((x ^= u.getByte(p++) << 7) < 0L) {
				x ^= ~0L << 7;
			} else if ((x ^= u.getByte(p++) << 14) >= 0L) {
				x ^= ~0L << 7 ^ ~0L << 14;
			} else if ((x ^= u.getByte(p++) << 21) < 0L) {
				x ^= ~0L << 7 ^ ~0L << 14 ^ ~0L << 21;
			} else {
				int y = u.getByte(p++);
				x ^= y << 28;
				x ^= ~0L << 7 ^ ~0L << 14 ^ ~0L << 21 ^ ~0L << 28;
				if (y < 0 && u.getByte(p++) < 0 && u.getByte(p++) < 0 && u.getByte(p++) < 0 && u.getByte(p++) < 0 && u.getByte(p++) < 0) {
					break fastpath; // Will throw malformedVarint()
				}
			}
			pos = (int) (p - base);
			return x;
		}

		return (int) readRawVarint64SlowPath();
	}

	public final long readVarLong() {
		guard(1);

		Unsafe u = U;
		long b = base + pos;
		int v = u.getByte(b++);
		long rv = v & 0x7F;

		if ((v & 0x80) != 0) {
			guard(1);
			v = u.getByte(b++);
			rv |= (v & 0x7F) << 7;
			if ((v & 0x80) != 0) {
				guard(1);
				v = u.getByte(b++);
				rv |= (v & 0x7F) << 14;
				if ((v & 0x80) != 0) {
					guard(1);
					v = u.getByte(b++);
					rv |= (v & 0x7F) << 21;
					if ((v & 0x80) != 0) {
						guard(1);
						v = u.getByte(b++);
						rv |= (long) (v & 0x7F) << 28;
						if ((v & 0x80) != 0) {
							guard(1);
							v = u.getByte(b++);
							rv |= (long) (v & 0x7F) << 35;
							if ((v & 0x80) != 0) {
								guard(1);
								v = u.getByte(b++);
								rv |= (long) (v & 0x7F) << 42;
								if ((v & 0x80) != 0) {
									guard(1);
									v = u.getByte(b++);
									rv |= (long) (v & 0x7F) << 49;
									if ((v & 0x80) != 0) {
										guard(1);
										v = u.getByte(b++);
										rv |= (long) v << 56;
									}
								}
							}
						}
					}
				}
			}
		}

		pos = (int) (b - base);

		return rv;
	}

	public short readVarShort() {
		final byte value = readByte();
		if (value == -1) {
			return readShort();
		}
		if (value < 0) {
			return (short) (value + 256);
		}

		return value;
	}

	@Override
	public void reset() {
		pos = mark;
		mark = 0;
	}

	public Source reusable() {
		disposable = false;
		return this;
	}

	@Override
	public long skip(long n) {
		long k = lim - pos;
		if (n < k) {
			k = n < 0 ? 0 : n;
		}

		pos += k;
		return k;
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return (int) skip(n);
	}

	int tryReadAscii(char[] dst) {
		int ix = 0;
		guard(dst.length);
		long p = base + pos;

		int c;
		Unsafe u = U;

		// This loop is aggressively optimized by the JIT.
		while (ix < dst.length) {
			c = u.getByte(p) & 0xFF;
			if (c > 0X7F) {
				break;
			}
			p++;
			dst[ix++] = (char) c;
		}

		pos = (int) (p - base);

		return ix;
	}

	public long utfBuffer(int len) {
		if (len > utfLen) {
			utfBuffer = utfBuffer == 0 ? Bits.allocateMemory(len) : Bits.reallocateMemory(utfBuffer, len);
		}
		return utfBuffer;
	}
}