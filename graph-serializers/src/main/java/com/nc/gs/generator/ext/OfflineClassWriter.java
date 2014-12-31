package com.nc.gs.generator.ext;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import symbols.java.lang._Object;

import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.interpreter.VisitationContext;

/**
 * Computes {@link ClassWriter#getCommonSuperClass(String,String)} without attempting ClassLoading
 * via {@link Class#forName(String)}
 *
 * @author cmuramoto
 */
public class OfflineClassWriter extends ClassWriter {

	public OfflineClassWriter(ClassReader cr, int flags) {
		super(cr, flags);
	}

	public OfflineClassWriter(int flags) {
		super(flags);
	}

	@Override
	public String getCommonSuperClass(String left, String right) {
		if (left.equals(right)) {
			return left;
		}

		try (VisitationContext vc = VisitationContext.current()) {
			ExtendedType l = ExtendedType.forInternalName(left, true);
			ExtendedType r = ExtendedType.forInternalName(right, true);

			if (l.isAssignableFrom(r)) {
				return l.name;
			}

			if (r.isAssignableFrom(l)) {
				return r.name;
			}

			if (l.isInterface() || r.isInterface()) {
				return _Object.name;
			}

			do {
				l = l.lazyParent();
			} while (!l.isAssignableFrom(r));

			return l.name;
		}
	}

}
