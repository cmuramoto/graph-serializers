package com.nc.gs.generator;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import symbols.io.abstraction._GraphSerializer;

import com.nc.gs.generator.ext.OfflineClassWriter;
import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.FieldInfo;
import com.nc.gs.interpreter.FieldTrap;
import com.nc.gs.util.Pair;

public enum GenerationStrategy {

	FULL_HIERARCHY {

		@Override
		public ClassInfo forRuntime(Class<?> clazz) throws IOException {
			return ClassInfo.getInfo(clazz, FieldTrap.DEFAULT, true);
		}

		@Override
		public GraphClassAdapter generator(ClassInfo ci, ClassWriter cw) {
			return new InheritanceGraphClassAdapterV3(ci, cw);
		}

		@Override
		public boolean requiresParentFirst() {
			return false;
		}

		@Override
		public List<FieldInfo> segregateNonNullableFields(ClassInfo ci) {
			return collectMultiple(ci, t -> !t.isIgnored() && !t.isPrimitive() && !t.canBeNull(), false);
		}

		@Override
		public List<FieldInfo> segregateNullableFields(ClassInfo ci) {
			return collectMultiple(ci, t -> !t.isIgnored() && t.canBeNull(), false);
		}

		@Override
		public List<FieldInfo> segregatePrimitiveFields(ClassInfo ci) {
			return collectMultiple(ci, t -> !t.isIgnored() && t.isPrimitive(), false);
		}

		@Override
		public int serializerAccessModifier(ClassInfo ci) {
			return ACC_PUBLIC | ACC_FINAL | ACC_SYNTHETIC;
		}

		@Override
		public Pair<String, String> superSerializerNameAndDesc(ClassInfo ci) {

			return Pair.of(_GraphSerializer.name, _GraphSerializer.desc);
		}

		@Override
		public boolean usesParentDelegation() {
			return false;
		}

	},

	DELEGATION {

		@Override
		public ClassInfo forRuntime(Class<?> clazz) throws IOException {
			return ClassInfo.getInfo(clazz, FieldTrap.DEFAULT, false);
		}

		@Override
		public GraphClassAdapter generator(ClassInfo ci, ClassWriter cw) {
			return null;
		}

		@Override
		public boolean requiresParentFirst() {
			return true;
		}

		@Override
		public List<FieldInfo> segregateNonNullableFields(ClassInfo ci) {
			return null;
		}

		@Override
		public List<FieldInfo> segregateNullableFields(ClassInfo ci) {
			return FULL_HIERARCHY.segregateNullableFields(ci);
		}

		@Override
		public List<FieldInfo> segregatePrimitiveFields(ClassInfo ci) {
			return FULL_HIERARCHY.segregatePrimitiveFields(ci);
		}

		@Override
		public int serializerAccessModifier(ClassInfo ci) {
			return ACC_PUBLIC | ACC_FINAL | ACC_SYNTHETIC;
		}

		@Override
		public Pair<String, String> superSerializerNameAndDesc(ClassInfo ci) {
			return FULL_HIERARCHY.superSerializerNameAndDesc(ci);
		}

		@Override
		public boolean usesParentDelegation() {
			return true;
		}

	};

	private static List<FieldInfo> collect(final ClassInfo ci, Predicate<FieldInfo> p, boolean draining) {
		List<FieldInfo> fields = ci.fields;

		List<FieldInfo> rv;

		if (fields != null) {
			rv = null;

			Iterator<FieldInfo> itr = fields.iterator();

			while (itr.hasNext()) {
				FieldInfo fi = itr.next();

				if (p.test(fi)) {
					if (rv == null) {
						rv = new ArrayList<>(fields.size());
					}
					rv.add(fi);

					if (draining) {
						itr.remove();
					}
				}
			}

			if (fields.isEmpty()) {
				ci.fields = null;
			}

		} else {
			rv = null;
		}

		return rv == null ? null : rv;
	}

	private static List<FieldInfo> collectMultiple(ClassInfo ci, Predicate<FieldInfo> p, boolean draining) {

		List<FieldInfo> rv = new LinkedList<>();

		while (ci != null) {
			List<FieldInfo> infos = collect(ci, p, draining);

			if (infos != null) {

				rv.addAll(infos);
			}

			ci = ci.lazyParent();
		}

		if (!rv.isEmpty()) {
			if (rv != null) {
				Comparator<FieldInfo> c = (left, right) -> Long.compare(left.objectFieldOffset(), right.objectFieldOffset());

				Collections.sort(rv, c);
			}
		}

		return rv.isEmpty() ? null : rv;
	}

	public static ClassWriter newClassWriter() {
		return new OfflineClassWriter(ClassWriter.COMPUTE_FRAMES);
	}

	public static ClassWriter newClassWriter(ClassReader cr) {
		return new OfflineClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
	}

	public static final Pair<String, Boolean> prefixForSerializer(ClassInfo ci) {
		String n = ci.getName();

		String pre = prefixForSerializer(n);

		return Pair.of(pre, pre != n);
	}

	public static String prefixForSerializer(String n) {
		String rv;
		if (n.startsWith("java") || n.startsWith("sun")) {
			String[] tokens = n.split("/");

			StringBuilder builder = new StringBuilder();

			for (int i = 0; i < tokens.length - 1; i++) {
				builder.append(tokens[i].charAt(0)).append('/');
			}
			builder.append(tokens[tokens.length - 1]);

			rv = builder.toString();
		} else {
			rv = n.replace('[', '@');
		}

		return rv;
	}

	public abstract ClassInfo forRuntime(Class<?> clazz) throws IOException;

	public abstract GraphClassAdapter generator(ClassInfo ci, ClassWriter cw);

	public boolean isHierarchyInSameNamespace(ClassInfo root) {
		String pn = root.packageName();
		ClassInfo curr = root.lazyParent();

		while (curr != null) {
			if (curr.fields != null && !pn.equals(curr.packageName())) {
				return false;
			}
			curr = curr.lazyParent();
		}

		return true;
	}

	public abstract boolean requiresParentFirst();

	public abstract List<FieldInfo> segregateNonNullableFields(ClassInfo ci);

	public abstract List<FieldInfo> segregateNullableFields(ClassInfo ci);

	public abstract List<FieldInfo> segregatePrimitiveFields(ClassInfo ci);

	public abstract int serializerAccessModifier(ClassInfo ci);

	public abstract Pair<String, String> superSerializerNameAndDesc(ClassInfo ci);

	public abstract boolean usesParentDelegation();
}
