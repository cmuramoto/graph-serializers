package com.nc.gs.config;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class CoreLocationService implements NamespaceLocationService {

	@Override
	public List<String> enumerateNamespaces() {
		return Arrays.asList("jdk.core");
	}

	@Override
	public List<URL> enumerateResources() {
		return null;
	}

}
