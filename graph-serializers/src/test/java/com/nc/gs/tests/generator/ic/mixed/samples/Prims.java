package com.nc.gs.tests.generator.ic.mixed.samples;

import com.nc.gs.meta.Fields;
import com.nc.gs.tests.generator.ic.mixed.samples.HardCoded.Complex;
import com.nc.gs.tests.generator.ic.mixed.samples.HardCoded.Simple;

@SuppressWarnings("unused")
@Fields(compressByDefault = true)
public class Prims {

	boolean a;
	private boolean b;

	byte c;
	private byte d;

	char e;
	private char f;

	short g;
	private short h;

	int i;
	private int j;

	float k;
	private float l;

	long m;
	private long n;

	double o;
	private double p;

	HardCoded hcV;

	private HardCoded hcP;

	Complex cV;

	private Complex cP;

	Simple sV;

	private Simple sP;
}
