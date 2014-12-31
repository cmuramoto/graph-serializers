package com.nc.gs.config;

import java.net.URL;
import java.util.List;

public interface NamespaceLocationService {

	List<String> enumerateNamespaces();

	List<URL> enumerateResources();

}