package com.nc.gs.io;

import static com.nc.gs.util.Utils.U;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import org.objectweb.asm.Type;

import sun.misc.Unsafe;

import com.nc.gs.util.Bits;
import com.nc.gs.util.Utils;

@SuppressWarnings("restriction")
public final class Sink extends OutputStream implements DataOutput, Closeable {

	public static Sink mmap(File file) {
		return null;
	}

	static Object validate(Object target) {
		if (target != null) {
			if (!(target instanceof ByteBuffer) && target instanceof OutputStream) {
				throw new IllegalArgumentException(target.getClass().getName());
			}
		}
		return target;
	}

	static final long COPY_THRESHOLD = 1024L * 1024L;

	byte[] chunk;
	long base;
	int pos;
	int lim;

	int mark;

	public Sink() {
		this(4096);
	}

	public Sink(int initialSize) {
		lim = Utils.nextPowerOfTwo(initialSize);
		base = Bits.allocateMemory(lim);
	}

	byte[] chunk() {
		byte[] b = chunk;
		int p = (int) (pos - base);

		if (b == null || b.length < p) {
			b = chunk = new byte[p];
		}

		Bits.copyTo(base, b, 0, p);

		return b;
	}

	public void clear() {
		pos = mark = 0;
	}

	@Override
	public void close() {
		pos = mark = 0;
	}

	public int doPutInt(int v) {
		U.putInt(ix(4), v);
		return pos;
	}

	public void encodeMask(int lim, int p, long mask) {
		int cp = pos;
		pos = p;

		if (lim < 48) {
			writeLongP(mask | 1L << lim + 1);
		} else {
			writeLong(mask);
		}

		pos = cp;
	}

	@Override
	public void flush() {
	}

	public void flushTo(DataOutput target) throws IOException {

		if (target != null && target != this) {
			if (target instanceof Sink) {

			} else {
				target.write(chunk(), 0, (int) (pos - base));
			}
		}

	}

	void guard(int req) {
		if (pos + req > lim) {
			int newLim = Utils.nextPowerOfTwo(lim + req);
			base = Bits.reallocateMemory(base, newLim, pos);
			lim = newLim;
		}
	}

	private long ix(int req) {
		int p = pos;

		if (p + req > lim) {
			int newLim = Utils.nextPowerOfTwo(lim + req);
			base = Bits.reallocateMemory(base, newLim, pos);
			lim = newLim;
		}

		pos += req;

		return base + p;
	}

	public int limit() {
		return lim;
	}

	public Source mirror() {
		Source source = new Source();
		source.base = base;
		source.lim = lim;

		return source;
	}

	public final void packL(long value) {
		Unsafe u = U;
		long b = base + pos;
		if (value >>> 7 == 0) {
			u.putByte(b, (byte) value);
			pos++;
			return;
		}
		if (value >>> 14 == 0) {
			u.putByte(b++, (byte) (value & 0x7F | 0x80));
			u.putByte(b, (byte) (value >>> 7));
			pos += 2;
			return;
		}
		if (value >>> 21 == 0) {
			u.putByte(b++, (byte) (value & 0x7F | 0x80));
			u.putByte(b++, (byte) (value >>> 7 | 0x80));
			u.putByte(b, (byte) (value >>> 14));
			pos += 3;
			return;
		}
		if (value >>> 28 == 0) {
			u.putByte(b++, (byte) (value & 0x7F | 0x80));
			u.putByte(b++, (byte) (value >>> 7 | 0x80));
			u.putByte(b++, (byte) (value >>> 14 | 0x80));
			u.putByte(b, (byte) (value >>> 21));
			pos += 4;
			return;
		}
		if (value >>> 35 == 0) {
			u.putByte(b++, (byte) (value & 0x7F | 0x80));
			u.putByte(b++, (byte) (value >>> 7 | 0x80));
			u.putByte(b++, (byte) (value >>> 14 | 0x80));
			u.putByte(b++, (byte) (value >>> 21 | 0x80));
			u.putByte(b, (byte) (value >>> 28));
			pos += 5;
			return;
		}
		if (value >>> 42 == 0) {
			u.putByte(b++, (byte) (value & 0x7F | 0x80));
			u.putByte(b++, (byte) (value >>> 7 | 0x80));
			u.putByte(b++, (byte) (value >>> 14 | 0x80));
			u.putByte(b++, (byte) (value >>> 21 | 0x80));
			u.putByte(b++, (byte) (value >>> 28 | 0x80));
			u.putByte(b, (byte) (value >>> 35));
			pos += 6;
			return;
		}
		if (value >>> 49 == 0) {
			u.putByte(b++, (byte) (value & 0x7F | 0x80));
			u.putByte(b++, (byte) (value >>> 7 | 0x80));
			u.putByte(b++, (byte) (value >>> 14 | 0x80));
			u.putByte(b++, (byte) (value >>> 21 | 0x80));
			u.putByte(b++, (byte) (value >>> 28 | 0x80));
			u.putByte(b++, (byte) (value >>> 35 | 0x80));
			u.putByte(b, (byte) (value >>> 42));
			pos += 7;
			return;
		}
		if (value >>> 56 == 0) {
			u.putByte(b++, (byte) (value & 0x7F | 0x80));
			u.putByte(b++, (byte) (value >>> 7 | 0x80));
			u.putByte(b++, (byte) (value >>> 14 | 0x80));
			u.putByte(b++, (byte) (value >>> 21 | 0x80));
			u.putByte(b++, (byte) (value >>> 28 | 0x80));
			u.putByte(b++, (byte) (value >>> 35 | 0x80));
			u.putByte(b++, (byte) (value >>> 42 | 0x80));
			u.putByte(b, (byte) (value >>> 49));
			pos += 8;
			return;
		}
		u.putByte(b++, (byte) (value & 0x7F | 0x80));
		u.putByte(b++, (byte) (value >>> 7 | 0x80));
		u.putByte(b++, (byte) (value >>> 14 | 0x80));
		u.putByte(b++, (byte) (value >>> 21 | 0x80));
		u.putByte(b++, (byte) (value >>> 28 | 0x80));
		u.putByte(b++, (byte) (value >>> 35 | 0x80));
		u.putByte(b++, (byte) (value >>> 42 | 0x80));
		u.putByte(b++, (byte) (value >>> 49 | 0x80));
		u.putByte(b, (byte) (value >>> 56));
		pos += 9;
	}

	public int position() {
		return pos;
	}

	public void putIntArray(int[] mag) {
		writeIntP(mag.length);
		write(mag, 0, mag.length);
	}

	public int reserveMask(int max) {
		int p = pos;

		if (max < 48) {
			pos = p + 1 + (max + 1) / 7;
		} else {
			pos += 8;
		}

		return p;
	}

	public void reserveNullSlot(int sz) {
		mark = pos;
		int p = mark = pos;

		int req = Utils.nextPowerOfTwoInc(sz) << 1;

		if (req > 0 && (req >>>= 6) <= 20) {
			pos = p + 1 + req / 7;
		} else {
			pos += 4;
		}
	}

	public void reset() {
		pos = mark;
		mark = 0;
	}

	public byte[] toByteArray() {
		byte[] b = new byte[pos];
		Bits.copyTo(base, b, 0, pos);
		return b;
	}

	private void write(boolean[] o, int off, int len) {
		Bits.writeBitMask(this, o);
	}

	@Override
	public void write(byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}

	@Override
	public void write(byte[] v, int off, int len) {
		Bits.copyFrom(ix(len), v, off, len);
	}

	public void write(char[] v, int off, int len) {
		Bits.copyFrom(ix(len << 1), v, off, len);
	}

	public void write(double[] v, int off, int len) {
		Bits.copyFrom(ix(len << 3), v, off, len);
	}

	public void write(float[] v, int off, int len) {
		Bits.copyFrom(ix(len << 2), v, off, len);
	}

	@Override
	public void write(int b) {
		U.putByte(ix(1), (byte) b);
	}

	public void write(int[] v, int off, int len) {
		Bits.copyFrom(ix(len << 2), v, off, len);
	}

	public void write(long[] v, int off, int len) {
		Bits.copyFrom(ix(len << 3), v, off, len);
	}

	public void write(short[] v, int off, int len) {
		Bits.copyFrom(ix(len << 1), v, off, len);
	}

	@Override
	public void writeBoolean(boolean v) {
		writeByte(v ? 1 : 0);
	}

	public void writeByte(byte b) {
		U.putByte(ix(1), b);
	}

	@Override
	public void writeByte(int v) {
		U.putByte(ix(1), (byte) v);
	}

	public void writeBytes(byte[] v) {
		writeBytes(v, 0, v.length);
	}

	public void writeBytes(byte[] v, int off, int len) {
		Bits.copyFrom(ix(len), v, off, len);
	}

	@Override
	public void writeBytes(String s) throws IOException {

	}

	public void writeChar(char v) {
		U.putChar(ix(2), v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		writeChar((char) v);
	}

	public void writeCharP(char v) {
		U.putChar(ix(2), v);
	}

	@Override
	public void writeChars(String s) {

	}

	@Override
	public void writeDouble(double v) {
		U.putDouble(ix(8), v);
	}

	@Override
	public void writeFloat(float v) {
		U.putFloat(ix(4), v);
	}

	@Override
	public void writeInt(int v) {
		U.putInt(ix(4), v);
	}

	public void writeInt(int p, int v) {
		U.putInt(base + p, v);
	}

	public void writeIntP(int v) {
		sun.misc.Unsafe u = U;

		guard(5);

		long b = base + pos;
		if (v >>> 7 == 0) {
			// guard(1);
			U.putByte(b, (byte) v);
			pos++;
			return;
		}
		if (v >>> 14 == 0) {
			// guard(2);
			u.putByte(b++, (byte) (v & 0x7F | 0x80));
			u.putByte(b, (byte) (v >>> 7));
			pos += 2;
			return;
		}
		if (v >>> 21 == 0) {
			// guard(3);
			u.putByte(b++, (byte) (v & 0x7F | 0x80));
			u.putByte(b++, (byte) (v >>> 7 | 0x80));
			u.putByte(b, (byte) (v >>> 14));
			pos += 3;
			return;
		}
		if (v >>> 28 == 0) {
			// guard(4);
			u.putByte(b++, (byte) (v & 0x7F | 0x80));
			u.putByte(b++, (byte) (v >>> 7 | 0x80));
			u.putByte(b++, (byte) (v >>> 14 | 0x80));
			u.putByte(b, (byte) (v >>> 21));
			pos += 4;
			return;
		}

		// guard(5);
		u.putByte(b++, (byte) (v & 0x7F | 0x80));
		u.putByte(b++, (byte) (v >>> 7 | 0x80));
		u.putByte(b++, (byte) (v >>> 14 | 0x80));
		u.putByte(b++, (byte) (v >>> 21 | 0x80));
		u.putByte(b, (byte) (v >>> 28));
		pos += 5;
	}

	public void writeLong(int p, long v) {
		U.putLong(base + p, v);
	}

	@Override
	public void writeLong(long v) {
		U.putLong(ix(8), v);
	}

	public void writeLongP(long v) {
		Unsafe u = U;
		guard(9);
		long b = base + pos;
		if (v >>> 7 == 0) {
			// guard(1);
			u.putByte(b, (byte) v);
			pos++;
			return;
		}
		if (v >>> 14 == 0) {
			// guard(2);
			u.putByte(b++, (byte) (v & 0x7F | 0x80));
			u.putByte(b, (byte) (v >>> 7));
			pos += 2;
			return;
		}
		if (v >>> 21 == 0) {
			// guard(3);
			u.putByte(b++, (byte) (v & 0x7F | 0x80));
			u.putByte(b++, (byte) (v >>> 7 | 0x80));
			u.putByte(b, (byte) (v >>> 14));
			pos += 3;
			return;
		}
		if (v >>> 28 == 0) {
			// guard(4);
			u.putByte(b++, (byte) (v & 0x7F | 0x80));
			u.putByte(b++, (byte) (v >>> 7 | 0x80));
			u.putByte(b++, (byte) (v >>> 14 | 0x80));
			u.putByte(b, (byte) (v >>> 21));
			pos += 4;
			return;
		}
		if (v >>> 35 == 0) {
			// guard(5);
			u.putByte(b++, (byte) (v & 0x7F | 0x80));
			u.putByte(b++, (byte) (v >>> 7 | 0x80));
			u.putByte(b++, (byte) (v >>> 14 | 0x80));
			u.putByte(b++, (byte) (v >>> 21 | 0x80));
			u.putByte(b, (byte) (v >>> 28));
			pos += 5;
			return;
		}
		if (v >>> 42 == 0) {
			// guard(6);
			u.putByte(b++, (byte) (v & 0x7F | 0x80));
			u.putByte(b++, (byte) (v >>> 7 | 0x80));
			u.putByte(b++, (byte) (v >>> 14 | 0x80));
			u.putByte(b++, (byte) (v >>> 21 | 0x80));
			u.putByte(b++, (byte) (v >>> 28 | 0x80));
			u.putByte(b, (byte) (v >>> 35));
			pos += 6;
			return;
		}
		if (v >>> 49 == 0) {
			// guard(7);
			u.putByte(b++, (byte) (v & 0x7F | 0x80));
			u.putByte(b++, (byte) (v >>> 7 | 0x80));
			u.putByte(b++, (byte) (v >>> 14 | 0x80));
			u.putByte(b++, (byte) (v >>> 21 | 0x80));
			u.putByte(b++, (byte) (v >>> 28 | 0x80));
			u.putByte(b++, (byte) (v >>> 35 | 0x80));
			u.putByte(b, (byte) (v >>> 42));
			pos += 7;
			return;
		}
		if (v >>> 56 == 0) {
			// guard(8);
			u.putByte(b++, (byte) (v & 0x7F | 0x80));
			u.putByte(b++, (byte) (v >>> 7 | 0x80));
			u.putByte(b++, (byte) (v >>> 14 | 0x80));
			u.putByte(b++, (byte) (v >>> 21 | 0x80));
			u.putByte(b++, (byte) (v >>> 28 | 0x80));
			u.putByte(b++, (byte) (v >>> 35 | 0x80));
			u.putByte(b++, (byte) (v >>> 42 | 0x80));
			u.putByte(b, (byte) (v >>> 49));
			pos += 8;
			return;
		}
		// guard(9);
		u.putByte(b++, (byte) (v & 0x7F | 0x80));
		u.putByte(b++, (byte) (v >>> 7 | 0x80));
		u.putByte(b++, (byte) (v >>> 14 | 0x80));
		u.putByte(b++, (byte) (v >>> 21 | 0x80));
		u.putByte(b++, (byte) (v >>> 28 | 0x80));
		u.putByte(b++, (byte) (v >>> 35 | 0x80));
		u.putByte(b++, (byte) (v >>> 42 | 0x80));
		u.putByte(b++, (byte) (v >>> 49 | 0x80));
		u.putByte(b, (byte) (v >>> 56));
		pos += 9;
	}

	public void writePrimitiveArray(int k, Object o) {

		int len = Array.getLength(o);
		switch (k) {
		case Type.BOOLEAN:
			write((boolean[]) o, 0, len);
			break;
		case Type.CHAR:
			write((char[]) o, 0, len);
			break;
		case Type.BYTE:
			write((byte[]) o, 0, len);
			break;
		case Type.SHORT:
			write((short[]) o, 0, len);
			break;
		case Type.INT:
			write((int[]) o, 0, len);
			break;
		case Type.FLOAT:
			write((float[]) o, 0, len);
			break;
		case Type.LONG:
			write((long[]) o, 0, len);
			break;
		case Type.DOUBLE:
			write((double[]) o, 0, len);
			break;
		}
	}

	@Override
	public void writeShort(int v) {
		U.putShort(ix(2), (short) v);
	}

	public void writeShort(short v) {
		U.putShort(ix(2), v);
	}

	public void writeShortP(short v) {
		guard(3);
		if (v >= 0 && v < 255) {
			U.putByte(pos++, (byte) v);
		} else {
			U.putByte(pos++, (byte) -1);
			U.putShort(pos, v);
			pos += 2;
		}
	}

	@Override
	public void writeUTF(String s) {
		char[] value = (char[]) U.getObject(s, Utils.V_OFF);
		final int len = value.length;
		writeIntP(len);
		int c;

		for (int i = 0; i < len; i++) {
			c = value[i];
			if (c <= 0x007F) {
				writeByte((byte) c);
			} else if (c > 0x07FF) {
				writeByte((byte) (0xE0 | c >> 12 & 0x0F));
				writeByte((byte) (0x80 | c >> 6 & 0x3F));
				writeByte((byte) (0x80 | c >> 0 & 0x3F));
			} else {
				writeByte((byte) (0xC0 | c >> 6 & 0x1F));
				writeByte((byte) (0x80 | c >> 0 & 0x3F));
			}
		}
	}
}