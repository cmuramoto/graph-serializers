package com.nc.gs.core;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.nc.gs.config.ProvisionService;
import com.nc.gs.config.ValidMapping;
import com.nc.gs.ds.ClassTable;
import com.nc.gs.generator.Reifier;
import com.nc.gs.log.Log;
import com.nc.gs.serializers.SingletonSerializer;
import com.nc.gs.serializers.java.lang.OpaqueSerializer;
import com.nc.gs.util.Pair;
import com.nc.gs.util.Utils;

public final class Genesis {

	public static void bootstrap() {

	}

	public static Pair<int[], Pair<int[], Class<?>[]>> getClassData() {
		ProvisionService ps = ProvisionService.getInstance();
		if (ps == null) {
			throw new IllegalStateException("Tried to bootstrap more than once!!!");
		}
		return ps.getClassData();
	}

	static ClassTable getClassTableImpl() {
		return impl;
	}

	static void installSerializers(ProvisionService ps) {

		TreeMap<Integer, ValidMapping> td = ps == null ? null : ps.getTypeData();

		if (td == null) {
			return;
		}

		Set<Entry<Integer, ValidMapping>> es = td.entrySet();

		for (Entry<Integer, ValidMapping> e : es) {
			ValidMapping p = e.getValue();

			Class<?> type = p.type;
			Object overlaid = p.serializer;

			if (overlaid != null) {
				GraphSerializer gs;
				boolean tryReify = !p.skipReify && !p.isOpaqueGraph && !(overlaid instanceof SingletonSerializer);

				if (overlaid instanceof String) {
					String fqn = (String) overlaid;
					if (!tryReify) {
						gs = Utils.forNewInstance(fqn);
					} else {
						tryReify = true;
						try {
							gs = Reifier.reify(type, (String) overlaid);
							Log.info("Reified %s", fqn);
							tryReify = false;
						} catch (Throwable ex) {
							Log.warn("Reify failed for %s. (%s)", fqn, ex.getMessage());
							gs = Utils.forNewInstance(fqn);
						}
					}
				} else {
					gs = (GraphSerializer) overlaid;
				}

				SerializerFactory.register(type, gs, tryReify);
			} else {
				if (p.isOpaqueGraph) {
					SerializerFactory.register(type, new OpaqueSerializer(type, p.includes));
				}
			}
		}

		Map<String, String> replacements = ps.getReplacements();

		if (replacements != null) {

			Set<Entry<String, String>> res = replacements.entrySet();

			for (Entry<String, String> e : res) {
				try {
					Context.R_TYPES.put(Utils.forName(e.getKey()), Utils.forName(e.getValue()));
				} catch (Throwable cnfe) {
					Log.warn(cnfe.getMessage());
				}
			}
		}
	}

	static final ClassTable impl;

	static {
		try (ProvisionService ps = ProvisionService.getInstance()) {
			impl = ClassTable.initBridge(ps);

			installSerializers(ps);
		}
	}

	private Genesis() {
	}
}