package com.nc.gs.io;

import com.nc.gs.util.Bits;
import com.nc.gs.util.Utils;

public final class UTF8Util {

	/**
	 * Same as {@link UTF8Util#utf8ToArray(long, char[])}, but unpacking from address-to address.
	 *
	 * @param off
	 *            - the base address
	 * @param dst
	 *            - the target address
	 * @return the address, repositioned by the amount of bytes consumed.
	 */
	public static native long utf8ToAddress(long src, long dst, int lim);

	/**
	 * Unpacks bytes from a raw address to a char array, using SSE instructions to bulk unpack
	 * 128-bits (16 ASCII chars) at a time. <br/>
	 * <br/>
	 * This method incurs JNI overhead, however it's worth if we have more than
	 * {@link Bits#JNI_UTF_VECTOR_THRESHOLD} characters.
	 *
	 * @param off
	 *            - the base address
	 * @param dst
	 *            - the target array
	 * @return the address, repositioned by the amount of bytes consumed.
	 */
	public static native long utf8ToArray(long off, char[] dst);

	/**
	 * Unpacks bytes from a raw address to a char array, using AVX2 instructions to bulk unpack
	 * 256-bits (32 ASCII chars) at a time. <br/>
	 * <br/>
	 * This method incurs JNI overhead, however it's worth if we have more than
	 * {@link Bits#JNI_UTF_VECTOR_THRESHOLD} characters. <br/>
	 * <br/>
	 * This Method will FallBack to SSE if AVX2 is not supported!
	 *
	 * @param off
	 *            - the base address
	 * @param dst
	 *            - the target array
	 * @return the address, repositioned by the amount of bytes consumed.
	 */
	public static native long utf8ToArrayAVX(long off, char[] dst);

	static {
		Utils.loadLibrary("precompiled/libgs-native.so");
	}

}