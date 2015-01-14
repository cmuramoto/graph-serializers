package com.nc.gs.io;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;

public interface In {

	class OfDataInput implements In {

		final DataInput in;

		OfDataInput(DataInput in) {
			super();
			this.in = in;
		}

		@Override
		public boolean isStream() {
			return false;
		}

		@Override
		public int read() throws IOException {
			return in.readInt();
		}

		@Override
		public int read(byte[] dst, int off, int len) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void readFully(byte[] dst, int len) throws IOException {
			in.readFully(dst, 0, len);
		}

		@Override
		public String readString() throws IOException {
			return in.readUTF();
		}
	}

	class OfInputStream implements In {

		final InputStream in;

		OfInputStream(InputStream in) {
			super();
			this.in = in;
		}

		@Override
		public boolean isStream() {
			return true;
		}

		@Override
		public int read() throws IOException {
			int b;
			int v = (b = in.read()) & 0x7F;
			if ((b & 0x80) != 0) {
				v |= ((b = in.read()) & 0x7F) << 7;
				if ((b & 0x80) != 0) {
					v |= ((b = in.read()) & 0x7F) << 14;
					if ((b & 0x80) != 0) {
						v |= ((b = in.read()) & 0x7F) << 21;
						if ((b & 0x80) != 0) {
							v |= (in.read() & 0x7F) << 28;
						}
					}
				}
			}

			return v;
		}

		@Override
		public int read(byte[] dst, int off, int len) throws IOException {

			return in.read(dst, off, len);
		}

		@Override
		public void readFully(byte[] dst, int len) throws IOException {

			int rem = len;
			int off = 0;

			while (rem > 0) {
				int r = in.read(dst, off, rem);

				if (r < 0) {
					break;
				}
				rem -= r;
				off += r;
			}
		}

		@Override
		public String readString() throws IOException {
			int len = in.read();

			byte[] b = new byte[len];

			readFully(b, len);

			return new String(b);
		}

	}

	public static In of(DataInput in) {
		return new OfDataInput(in);
	}

	public static In of(InputStream in) {
		return new OfInputStream(in);
	}

	boolean isStream();

	int read() throws IOException;

	int read(byte[] dst, int off, int len) throws IOException;

	void readFully(byte[] dst, int len) throws IOException;

	String readString() throws IOException;

}