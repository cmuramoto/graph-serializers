package serializers.impl.gs;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.nc.gs.config.NamespaceLocationService;

public class TypeLookup implements NamespaceLocationService {

	@Override
	public List<String> enumerateNamespaces() {
		return Arrays.asList("domain.std");
	}

	@Override
	public List<URL> enumerateResources() {
		return null;
	}

}