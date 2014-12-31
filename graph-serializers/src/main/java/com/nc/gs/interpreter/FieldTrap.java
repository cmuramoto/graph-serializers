package com.nc.gs.interpreter;

public enum FieldTrap {

	ALL_INSTANCE, DEFAULT, SKIP;

	public boolean acceptsTransient() {
		return this == ALL_INSTANCE;
	}

}
