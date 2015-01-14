package com.nc.gs.io;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

public interface Out {

	class OfDataOutput implements Out {
		final DataOutput out;

		public OfDataOutput(DataOutput out) {
			this.out = out;
		}

		@Override
		public boolean isStream() {
			return false;
		}

		@Override
		public void write(byte[] buff) throws IOException {
			out.write(buff);
		}

		@Override
		public void write(byte[] buff, int len) throws IOException {
			out.write(buff, 0, len);
		}

		@Override
		public void write(int v) throws IOException {
			out.writeInt(v);
		}

		@Override
		public void write(String str) throws IOException {
			out.writeUTF(str);
		}

	}

	class OfOutputStream implements Out {

		final OutputStream out;

		public OfOutputStream(OutputStream target) {
			out = target;
		}

		@Override
		public boolean isStream() {
			return true;
		}

		@Override
		public void write(byte[] buff) throws IOException {
			out.write(buff);
		}

		@Override
		public void write(byte[] buff, int len) throws IOException {
			out.write(buff, 0, len);
		}

		@Override
		public void write(int v) throws IOException {
			if (v >>> 7 == 0) {
				out.write(v);
				return;
			}
			if (v >>> 14 == 0) {
				out.write(v & 0x7F | 0x80);
				out.write(v >>> 7);
				return;
			}
			if (v >>> 21 == 0) {
				out.write(v & 0x7F | 0x80);
				out.write(v >>> 7 | 0x80);
				out.write(v >>> 14);
				return;
			}
			if (v >>> 28 == 0) {
				out.write(v & 0x7F | 0x80);
				out.write(v >>> 7 | 0x80);
				out.write(v >>> 14 | 0x80);
				out.write(v >>> 21);
				return;
			}

			out.write(v & 0x7F | 0x80);
			out.write(v >>> 7 | 0x80);
			out.write(v >>> 14 | 0x80);
			out.write(v >>> 21 | 0x80);
			out.write(v >>> 28);
		}

		@Override
		public void write(String str) throws IOException {
			write(str.length());
			out.write(str.getBytes());
		}

	}

	public static Out of(DataOutput target) {
		return new OfDataOutput(target);
	}

	public static Out of(OutputStream target) {
		return new OfOutputStream(target);
	}

	boolean isStream();

	void write(byte[] buff) throws IOException;

	void write(byte[] buff, int len) throws IOException;

	void write(int v) throws IOException;

	void write(String str) throws IOException;
}