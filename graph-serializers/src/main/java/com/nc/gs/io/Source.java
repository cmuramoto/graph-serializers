package com.nc.gs.io;

import static com.nc.gs.util.Utils.U;

import java.io.Closeable;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.objectweb.asm.Type;

import sun.misc.Unsafe;

import com.nc.gs.util.Bits;
import com.nc.gs.util.Utils;

@SuppressWarnings("restriction")
public final class Source extends InputStream implements Closeable, DataInput {

	public static Source allocate(InputStream src) {
		Source in = new Source();

		return in;
	}

	public static Source mmap(File file) {
		return null;
	}

	static final long ADDRESS_OFF = Utils.fieldOffset(Buffer.class, "address");

	Object src;

	protected long base;

	protected int pos;

	protected int mark;

	protected int lim;

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

	public Source clear() {
		pos = mark = 0;
		return this;
	}

	@Override
	public void close() throws IOException {
		U.freeMemory(base);
	}

	public long decodeMask(int max) {
		return max < 48 ? readLongP() : readLong();
	}

	public Source filledWith(byte[] src) {
		reset();
		fillGuard(src.length);
		Bits.copyFrom(base, src, 0, src.length);

		return this;
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
		byte[] b = new byte[4096];

		int r = 0;

		long p = 0;
		while ((r = is.read(b)) > 0) {
			fillGuard(r);

			Bits.copyFrom(base + p, b, 0, r);
			p += r;
		}

		return this;
	}

	private void fillGuard(int req) {
		if (req > lim) {
			int newLim = Utils.nextPowerOfTwo(lim + req);
			base = Bits.reallocateMemory(base, newLim, lim);
			lim = newLim;
		}
	}

	private void guard(int req) {
		if (lim - pos < req) {
			throw new BufferUnderflowException();
		}
	}

	private void inflate(boolean[] o) {
		Bits.readBitMask(this, o);
	}

	private void inflate(byte[] o) {
		Bits.copyTo(advance(o.length), o, 0, o.length);
	}

	private void inflate(char[] o) {
		Bits.copyTo(advance(o.length << 1), o, 0, o.length);
	}

	private void inflate(double[] o) {
		Bits.copyTo(advance(o.length << 3), o, 0, o.length);
	}

	private void inflate(float[] o) {
		Bits.copyTo(advance(o.length << 2), o, 0, o.length);
	}

	private void inflate(int[] o) {
		Bits.copyTo(advance(o.length << 2), o, 0, o.length);
	}

	private void inflate(long[] o) {
		Bits.copyTo(advance(o.length << 3), o, 0, o.length);
	}

	private void inflate(short[] o) {
		Bits.copyTo(advance(o.length << 1), o, 0, o.length);
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

	@Override
	public int read() {
		return pos < lim ? U.getByte(base + pos++) : -1;
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

		if (len > avail) {
			len = (int) avail;
		}

		if (len <= 0) {
			return 0;
		}

		Bits.copyTo(base, dst, off, len);

		pos += len;
		return len;
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
		int len = readIntP();
		int[] rv = new int[len];
		inflate(rv);

		return rv;
	}

	public int readIntP() {
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
		return null;
	}

	public final long readLongP() {
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

		pos += b - base - pos;

		return rv;
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
		final int charCount = readIntP();
		final char[] chars = new char[charCount];
		int c, ix = 0;
		while (ix < charCount) {
			c = readByte() & 0xff;

			switch (c >> 4) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				chars[ix++] = (char) c;
				break;
			case 12:
			case 13:
				chars[ix++] = (char) ((c & 0x1F) << 6 | readByte() & 0x3F);
				break;
			case 14:
				chars[ix++] = (char) ((c & 0x0F) << 12 | (readByte() & 0x3F) << 6 | (readByte() & 0x3F) << 0);
				break;
			default:// checkstyle
				break;
			}

		}

		String rv = Utils.allocateInstance(String.class);
		U.putObject(rv, Utils.V_OFF, chars);

		return rv;
	}

	public void refill(byte[] hb) {

	}

	@Override
	public void reset() {
		pos = mark;
		mark = 0;
	}

	public Sink shared() {
		Sink s = new Sink();
		s.base = base;
		s.lim = lim;
		return s;
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
		return 0;
	}

}