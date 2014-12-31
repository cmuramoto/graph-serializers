package com.nc.gs.config;

import java.util.Collections;
import java.util.Set;

public final class ValidMapping {

	public final Class<?> type;

	public final Object resolved;

	public final Object serializer;

	public final boolean isOpaqueGraph;

	public final boolean skipReify;

	public final Set<String> includes;

	public ValidMapping(Class<?> type, Object resolved, Object serializer, Set<String> includes, boolean isOpaqueGraph, boolean skipReify) {
		super();
		this.type = type;
		this.resolved = resolved;
		this.serializer = serializer;
		this.includes = includes == null ? Collections.<String> emptySet() : includes;
		this.isOpaqueGraph = isOpaqueGraph;
		this.skipReify = skipReify;
	}

	@Override
	public String toString() {
		return type == null ? null : type.toString();
	}

}
