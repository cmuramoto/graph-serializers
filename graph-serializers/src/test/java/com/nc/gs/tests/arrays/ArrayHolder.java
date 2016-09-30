package com.nc.gs.tests.arrays;

import java.util.Arrays;

import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;

public class ArrayHolder {

	boolean[] bools;

	byte[] bytes;

	char[] chars;

	short[] shorts;

	private int[] ints;

	float[] floats;

	long[] longs;

	double[] doubles;

	@NotNull
	boolean[] nn_bools;

	@NotNull
	byte[] nn_bytes;

	@NotNull
	private char[] nn_chars;

	@NotNull
	short[] nn_shorts;

	@NotNull
	int[] nn_ints;

	@NotNull
	float[] nn_floats;

	@NotNull
	long[] nn_longs;

	@NotNull
	double[] nn_doubles;

	@OnlyPayload
	boolean[] op_bools;

	@OnlyPayload
	byte[] op_bytes;

	@OnlyPayload
	char[] op_chars;

	@OnlyPayload
	short[] op_shorts;

	@OnlyPayload
	int[] op_ints;

	@OnlyPayload
	private float[] op_floats;

	@OnlyPayload
	long[] op_longs;

	@OnlyPayload
	double[] op_doubles;

	@NotNull
	@OnlyPayload
	boolean[] nn_op_bools;

	@NotNull
	@OnlyPayload
	private byte[] nn_op_bytes;

	@NotNull
	@OnlyPayload
	char[] nn_op_chars;

	@NotNull
	@OnlyPayload
	short[] nn_op_shorts;

	@NotNull
	@OnlyPayload
	int[] nn_op_ints;

	@NotNull
	@OnlyPayload
	float[] nn_op_floats;

	@NotNull
	@OnlyPayload
	long[] nn_op_longs;

	@NotNull
	@OnlyPayload
	double[] nn_op_doubles;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayHolder other = (ArrayHolder) obj;
		if (!Arrays.equals(bools, other.bools))
			return false;
		if (!Arrays.equals(bytes, other.bytes))
			return false;
		if (!Arrays.equals(chars, other.chars))
			return false;
		if (!Arrays.equals(doubles, other.doubles))
			return false;
		if (!Arrays.equals(floats, other.floats))
			return false;
		if (!Arrays.equals(ints, other.ints))
			return false;
		if (!Arrays.equals(longs, other.longs))
			return false;
		if (!Arrays.equals(nn_bools, other.nn_bools))
			return false;
		if (!Arrays.equals(nn_bytes, other.nn_bytes))
			return false;
		if (!Arrays.equals(nn_chars, other.nn_chars))
			return false;
		if (!Arrays.equals(nn_doubles, other.nn_doubles))
			return false;
		if (!Arrays.equals(nn_floats, other.nn_floats))
			return false;
		if (!Arrays.equals(nn_ints, other.nn_ints))
			return false;
		if (!Arrays.equals(nn_longs, other.nn_longs))
			return false;
		if (!Arrays.equals(nn_op_bools, other.nn_op_bools))
			return false;
		if (!Arrays.equals(nn_op_bytes, other.nn_op_bytes))
			return false;
		if (!Arrays.equals(nn_op_chars, other.nn_op_chars))
			return false;
		if (!Arrays.equals(nn_op_doubles, other.nn_op_doubles))
			return false;
		if (!Arrays.equals(nn_op_floats, other.nn_op_floats))
			return false;
		if (!Arrays.equals(nn_op_ints, other.nn_op_ints))
			return false;
		if (!Arrays.equals(nn_op_longs, other.nn_op_longs))
			return false;
		if (!Arrays.equals(nn_op_shorts, other.nn_op_shorts))
			return false;
		if (!Arrays.equals(nn_shorts, other.nn_shorts))
			return false;
		if (!Arrays.equals(op_bools, other.op_bools))
			return false;
		if (!Arrays.equals(op_bytes, other.op_bytes))
			return false;
		if (!Arrays.equals(op_chars, other.op_chars))
			return false;
		if (!Arrays.equals(op_doubles, other.op_doubles))
			return false;
		if (!Arrays.equals(op_floats, other.op_floats))
			return false;
		if (!Arrays.equals(op_ints, other.op_ints))
			return false;
		if (!Arrays.equals(op_longs, other.op_longs))
			return false;
		if (!Arrays.equals(op_shorts, other.op_shorts))
			return false;
		if (!Arrays.equals(shorts, other.shorts))
			return false;
		return true;
	}

	public boolean[] getBools() {
		return bools;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public char[] getChars() {
		return chars;
	}

	public double[] getDoubles() {
		return doubles;
	}

	public float[] getFloats() {
		return floats;
	}

	public int[] getInts() {
		return ints;
	}

	public long[] getLongs() {
		return longs;
	}

	public boolean[] getNn_bools() {
		return nn_bools;
	}

	public byte[] getNn_bytes() {
		return nn_bytes;
	}

	public char[] getNn_chars() {
		return nn_chars;
	}

	public double[] getNn_doubles() {
		return nn_doubles;
	}

	public float[] getNn_floats() {
		return nn_floats;
	}

	public int[] getNn_ints() {
		return nn_ints;
	}

	public long[] getNn_longs() {
		return nn_longs;
	}

	public boolean[] getNn_op_bools() {
		return nn_op_bools;
	}

	public byte[] getNn_op_bytes() {
		return nn_op_bytes;
	}

	public char[] getNn_op_chars() {
		return nn_op_chars;
	}

	public double[] getNn_op_doubles() {
		return nn_op_doubles;
	}

	public float[] getNn_op_floats() {
		return nn_op_floats;
	}

	public int[] getNn_op_ints() {
		return nn_op_ints;
	}

	public long[] getNn_op_longs() {
		return nn_op_longs;
	}

	public short[] getNn_op_shorts() {
		return nn_op_shorts;
	}

	public short[] getNn_shorts() {
		return nn_shorts;
	}

	public boolean[] getOp_bools() {
		return op_bools;
	}

	public byte[] getOp_bytes() {
		return op_bytes;
	}

	public char[] getOp_chars() {
		return op_chars;
	}

	public double[] getOp_doubles() {
		return op_doubles;
	}

	public float[] getOp_floats() {
		return op_floats;
	}

	public int[] getOp_ints() {
		return op_ints;
	}

	public long[] getOp_longs() {
		return op_longs;
	}

	public short[] getOp_shorts() {
		return op_shorts;
	}

	public short[] getShorts() {
		return shorts;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(bools);
		result = prime * result + Arrays.hashCode(bytes);
		result = prime * result + Arrays.hashCode(chars);
		result = prime * result + Arrays.hashCode(doubles);
		result = prime * result + Arrays.hashCode(floats);
		result = prime * result + Arrays.hashCode(ints);
		result = prime * result + Arrays.hashCode(longs);
		result = prime * result + Arrays.hashCode(nn_bools);
		result = prime * result + Arrays.hashCode(nn_bytes);
		result = prime * result + Arrays.hashCode(nn_chars);
		result = prime * result + Arrays.hashCode(nn_doubles);
		result = prime * result + Arrays.hashCode(nn_floats);
		result = prime * result + Arrays.hashCode(nn_ints);
		result = prime * result + Arrays.hashCode(nn_longs);
		result = prime * result + Arrays.hashCode(nn_op_bools);
		result = prime * result + Arrays.hashCode(nn_op_bytes);
		result = prime * result + Arrays.hashCode(nn_op_chars);
		result = prime * result + Arrays.hashCode(nn_op_doubles);
		result = prime * result + Arrays.hashCode(nn_op_floats);
		result = prime * result + Arrays.hashCode(nn_op_ints);
		result = prime * result + Arrays.hashCode(nn_op_longs);
		result = prime * result + Arrays.hashCode(nn_op_shorts);
		result = prime * result + Arrays.hashCode(nn_shorts);
		result = prime * result + Arrays.hashCode(op_bools);
		result = prime * result + Arrays.hashCode(op_bytes);
		result = prime * result + Arrays.hashCode(op_chars);
		result = prime * result + Arrays.hashCode(op_doubles);
		result = prime * result + Arrays.hashCode(op_floats);
		result = prime * result + Arrays.hashCode(op_ints);
		result = prime * result + Arrays.hashCode(op_longs);
		result = prime * result + Arrays.hashCode(op_shorts);
		result = prime * result + Arrays.hashCode(shorts);
		return result;
	}

	public void setBools(boolean[] bools) {
		this.bools = bools;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public void setChars(char[] chars) {
		this.chars = chars;
	}

	public void setDoubles(double[] doubles) {
		this.doubles = doubles;
	}

	public void setFloats(float[] floats) {
		this.floats = floats;
	}

	public void setInts(int[] ints) {
		this.ints = ints;
	}

	public void setLongs(long[] longs) {
		this.longs = longs;
	}

	public void setNn_bools(boolean[] nn_bools) {
		this.nn_bools = nn_bools;
	}

	public void setNn_bytes(byte[] nn_bytes) {
		this.nn_bytes = nn_bytes;
	}

	public void setNn_chars(char[] nn_chars) {
		this.nn_chars = nn_chars;
	}

	public void setNn_doubles(double[] nn_doubles) {
		this.nn_doubles = nn_doubles;
	}

	public void setNn_floats(float[] nn_floats) {
		this.nn_floats = nn_floats;
	}

	public void setNn_ints(int[] nn_ints) {
		this.nn_ints = nn_ints;
	}

	public void setNn_longs(long[] nn_longs) {
		this.nn_longs = nn_longs;
	}

	public void setNn_op_bools(boolean[] nn_op_bools) {
		this.nn_op_bools = nn_op_bools;
	}

	public void setNn_op_bytes(byte[] nn_op_bytes) {
		this.nn_op_bytes = nn_op_bytes;
	}

	public void setNn_op_chars(char[] nn_op_chars) {
		this.nn_op_chars = nn_op_chars;
	}

	public void setNn_op_doubles(double[] nn_op_doubles) {
		this.nn_op_doubles = nn_op_doubles;
	}

	public void setNn_op_floats(float[] nn_op_floats) {
		this.nn_op_floats = nn_op_floats;
	}

	public void setNn_op_ints(int[] nn_op_ints) {
		this.nn_op_ints = nn_op_ints;
	}

	public void setNn_op_longs(long[] nn_op_longs) {
		this.nn_op_longs = nn_op_longs;
	}

	public void setNn_op_shorts(short[] nn_op_shorts) {
		this.nn_op_shorts = nn_op_shorts;
	}

	public void setNn_shorts(short[] nn_shorts) {
		this.nn_shorts = nn_shorts;
	}

	public void setOp_bools(boolean[] op_bools) {
		this.op_bools = op_bools;
	}

	public void setOp_bytes(byte[] op_bytes) {
		this.op_bytes = op_bytes;
	}

	public void setOp_chars(char[] op_chars) {
		this.op_chars = op_chars;
	}

	public void setOp_doubles(double[] op_doubles) {
		this.op_doubles = op_doubles;
	}

	public void setOp_floats(float[] op_floats) {
		this.op_floats = op_floats;
	}

	public void setOp_ints(int[] op_ints) {
		this.op_ints = op_ints;
	}

	public void setOp_longs(long[] op_longs) {
		this.op_longs = op_longs;
	}

	public void setOp_shorts(short[] op_shorts) {
		this.op_shorts = op_shorts;
	}

	public void setShorts(short[] shorts) {
		this.shorts = shorts;
	}

}
