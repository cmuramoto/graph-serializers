package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import symbols.io.abstraction._Hierarchy;
import symbols.java.lang._Class;

import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.SerializerFactory;

public final class Hierarchy implements Comparator<Class<?>> {

	public static Hierarchy from(Class<?>[] types) {
		Hierarchy h = Hierarchy.unknown();

		Arrays.sort(types, h);

		Type[] sers = new Type[types.length];
		ExtendedType[] kt = new ExtendedType[types.length];

		long reified = 0l;
		for (int i = 0; i < types.length; i++) {
			Class<? extends GraphSerializer> gs = SerializerFactory.serializer(types[i]).getClass();
			reified |= gs.isSynthetic() ? 1L << i : 0;
			sers[i] = Type.getType(gs);
			kt[i] = ExtendedType.forRuntime(types[i]);
		}

		h.types = kt;
		h.reified = reified;

		return h;
	}

	public static Hierarchy unknown() {
		return new Hierarchy(ExtendedType.OBJECT, null, false);
	}

	public ExtendedType superType;

	private ExtendedType[] types;

	public Type[] sers;

	public String[] serNames;

	public boolean complete;

	public long reified;

	boolean sorted;

	public Hierarchy(Class<?> superType, Class<?>[] types, boolean complete) {
		this.superType = ExtendedType.forRuntime(superType);
		if (types != null) {
			Arrays.sort(types, this);
		}
		this.types = (types == null) || (types.length == 0) ? null : ExtendedType.forRuntime(types);
		this.complete = complete;
	}

	public Hierarchy(ExtendedType superType, ExtendedType[] types, boolean complete) {
		this.superType = superType;
		this.types = types;
		this.complete = complete;
	}

	public ExtendedType at(int ix) {
		if (!sorted) {
			sort();
		}

		return types[ix];
	}

	public Comparator<Class<?>> cmp() {
		return this;
	}

	@Override
	public int compare(Class<?> l, Class<?> r) {

		return l == r ? 0 : l.isAssignableFrom(r) ? +1 : r.isAssignableFrom(l) ? -1 : l.getName().compareTo(r.getName());
	}

	public boolean declaresTypes() {
		return (types != null) && (types.length > 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Hierarchy other = (Hierarchy) obj;
		if (complete != other.complete) {
			return false;
		}
		if (superType == null) {
			if (other.superType != null) {
				return false;
			}
		} else if (!superType.equals(other.superType)) {
			return false;
		}
		if (!Arrays.equals(types, other.types)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	public boolean isPolymorphic() {
		return !complete || ((types != null) && (types.length > 1));
	}

	public Hierarchy markSerializers() {
		if (declaresTypes()) {
			long reified = 0l;

			Class<?>[] rts = new Class<?>[types.length];

			for (int i = 0; i < rts.length; i++) {
				rts[i] = types[i].runtimeType();
			}

			Arrays.sort(rts, this);

			Type[] sers = new Type[types.length];

			for (int i = rts.length - 1; i >= 0; i--) {
				Class<?> type = rts[i];
				Class<? extends GraphSerializer> gs = SerializerFactory.serializer(type).getClass();
				sers[i] = Type.getType(gs);
				reified |= gs.isSynthetic() ? 1L << i : 0;
			}

			this.sers = sers;
			this.reified = reified;
		}

		return this;
	}

	public void onstack(MethodVisitor mv) {
		mv.visitTypeInsn(NEW, _Hierarchy.name);
		mv.visitInsn(DUP);

		mv.visitLdcInsn(superType.type());

		ExtendedType[] types = this.types;

		if ((types == null) || (types.length == 0)) {

			mv.visitInsn(ACONST_NULL);

		} else {

			Symbols.loadNumber(mv, types.length);
			mv.visitTypeInsn(ANEWARRAY, _Class.name);

			for (int i = 0; i < types.length; i++) {
				mv.visitInsn(DUP);
				Symbols.loadNumber(mv, i);
				mv.visitLdcInsn(types[i].type());
				mv.visitInsn(AASTORE);
			}
		}

		Symbols.loadBoolean(mv, complete);

		mv.visitMethodInsn(INVOKESPECIAL, _Hierarchy.name, _Class.ctor, _Hierarchy.ctor_D, false);
	}

	public Optional<Class<?>> opUniqueConcrete() {
		ExtendedType[] types = this.types;

		return ((types == null) || (types.length != 1)) ? //
		Optional.empty()
				: Optional.of(types[0].runtimeType());
	}

	public Class<?>[] runtimeTypes() {
		Class<?>[] rv;

		if ((types == null) || (types.length == 0)) {
			rv = null;
		} else {
			rv = new Class<?>[types.length];

			for (int i = 0; i < rv.length; i++) {
				rv[i] = types[i].runtimeType();
			}

			Arrays.sort(rv, this);
		}

		return rv;
	}

	private void sort() {
		Class<?>[] rts = runtimeTypes();

		if (rts != null) {
			ExtendedType[] types = new ExtendedType[rts.length];

			for (int i = 0; i < types.length; i++) {
				types[i] = ExtendedType.forRuntime(rts[i]);
			}

			this.types = types;
		}
		sorted = true;
	}

	public int totalTypes() {
		return types == null ? 0 : types.length;
	}

	public ExtendedType[] types() {
		if (!sorted) {
			sort();
		}

		return types;
	}

	public Class<?> uniqueConcrete() {
		return opUniqueConcrete().orElseThrow(() -> new IllegalStateException(("N-Types: " + types) == null ? "" : Arrays.toString(types)));
	}
}