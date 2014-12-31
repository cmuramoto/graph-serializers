package com.nc.gs.config;

public final class IllegalMappingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IllegalMappingException(String message) {
		super(message, null, true, false);
	}

}
