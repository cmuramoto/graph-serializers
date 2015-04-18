package com.nc.gs.interpreter;

import java.util.HashMap;

import com.nc.gs.util.Pair;

public final class VisitationContext implements AutoCloseable {

	public static VisitationContext current() {
		VisitationContext vc = CTXS.get();

		if (vc == null) {
			vc = new VisitationContext();
			CTXS.set(vc);
		}

		if (vc.visited == null) {
			vc.visited = new HashMap<>();
		}

		if (vc.depth++ == 0) {
			// Log.info("Creating Visitation Context");
		}

		return vc;
	}

	private static ThreadLocal<VisitationContext> CTXS = new ThreadLocal<>();

	int depth;
	private HashMap<String, Pair<ExtendedType, ClassInfo>> visited;

	public ExtendedType basic(String v) {
		ExtendedType rv;

		Pair<ExtendedType, ClassInfo> pair = visited.get(v);

		rv = pair == null ? null : pair.k;

		return rv;
	}

	@Override
	public void close() {

		if (--depth <= 0) {
			HashMap<String, Pair<ExtendedType, ClassInfo>> visited = this.visited;
			if (visited != null) {
				// Log.info("Destroying Visitation Context [cached types: %d]",
				// visited.size());
				visited.clear();
				visited = this.visited = null;
			}
		}
	}

	public ClassInfo info(String v, FieldTrap trap) {
		ClassInfo rv;

		Pair<ExtendedType, ClassInfo> pair = visited.get(v);

		rv = pair == null ? null : pair.v;

		if (rv != null && rv.trap != trap) {
			pair.v = rv = null;
		}

		return rv;
	}

	private void store(Pair<ExtendedType, ClassInfo> pair, ClassInfo ci) {
		if (pair != null) {
			pair.v = ci;
		} else {
			String name = ci.getName();
			String desc = ci.desc();

			Pair<ExtendedType, ClassInfo> p = Pair.of(ci.info, ci);

			visited.put(name, p);
			visited.put(desc, p);
		}
	}

	private void store(Pair<ExtendedType, ClassInfo> pair, ExtendedType bi) {
		if (pair != null) {
			pair.k = bi;
		} else {
			String name = bi.name;
			String desc = bi.desc;

			Pair<ExtendedType, ClassInfo> p = Pair.of(bi, null);

			visited.put(name, p);
			visited.put(desc, p);
		}
	}

	public void visited(ClassInfo ci) {
		Pair<ExtendedType, ClassInfo> pair = visited.get(ci.getName());

		store(pair, ci);
	}

	public void visited(ExtendedType bi) {
		Pair<ExtendedType, ClassInfo> pair = visited.get(bi.name);

		store(pair, bi);
	}
}