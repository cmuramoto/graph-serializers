package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

import java.util.Collection;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.PriorityBlockingQueue;

import org.objectweb.asm.MethodVisitor;

import symbols.io.abstraction._Shape;
import symbols.io.abstraction._Tags.ObjectShape;
import symbols.java.lang._Class;

import com.nc.gs.log.Log;
import com.nc.gs.util.Utils;

public final class Shape implements Cloneable {

	public static Shape SHAPELESS_COL = new Shape(null, ObjectShape.COLLECTION);
	public static Shape SHAPELESS_SET = new Shape(null, ObjectShape.SET);

	public static int kindOf(Collection<?> o) {
		int k;

		if (o instanceof SortedSet) {
			k = ObjectShape.SORTED_SET;
		} else if (o instanceof PriorityQueue) {
			k = ObjectShape.PRIORITY_QUEUE;
		} else if (o instanceof PriorityBlockingQueue) {
			k = ObjectShape.PRIORITY_B_QUEUE;
		} else {
			k = 0;
		}

		return k;
	}

	public static int of(Class<? extends Collection<?>> ct) {
		int rv;

		if (ct == null) {
			rv = ObjectShape.UNKONW;
		} else if (SortedSet.class.isAssignableFrom(ct)) {
			rv = ObjectShape.SORTED_SET;
		} else if (PriorityQueue.class.isAssignableFrom(ct)) {
			rv = ObjectShape.PRIORITY_QUEUE;
		} else if (PriorityBlockingQueue.class.isAssignableFrom(ct)) {
			rv = ObjectShape.PRIORITY_B_QUEUE;
		} else if (Set.class.isAssignableFrom(ct)) {
			rv = ObjectShape.SET;
		} else {
			rv = Utils.nullIfNotConcrete(ct) == null ? ObjectShape.UNKONW
					: ObjectShape.COLLECTION;
		}
		return rv;
	}

	public static Shape of(Collection<?> o) {
		Object s;
		int k;

		if (o instanceof SortedSet) {
			s = ((SortedSet<?>) o).comparator();
			k = ObjectShape.SORTED_SET;
		} else if (o instanceof PriorityQueue<?>) {
			s = ((PriorityQueue<?>) o).comparator();
			k = ObjectShape.PRIORITY_QUEUE;
		} else if (o instanceof PriorityBlockingQueue) {
			s = ((PriorityBlockingQueue<?>) o).comparator();
			k = ObjectShape.PRIORITY_QUEUE;
		} else {
			s = null;
			k = 0;
		}

		return s == null && k == 0 ? SHAPELESS_COL : new Shape(s, k);
	}

	public static Shape of(Map<?, ?> m) {
		Object s;
		int k;

		if (m instanceof SortedMap) {
			s = ((SortedMap<?, ?>) m).comparator();
			k = ObjectShape.SORTED_MAP;
		} else {
			s = null;
			k = 0;
		}

		return new Shape(s, k);
	}

	public static Shape of(Set<?> o) {
		Object s;
		int k;

		if (o instanceof SortedSet) {
			s = ((SortedSet<?>) o).comparator();
			k = ObjectShape.SORTED_SET;
		} else {
			s = null;
			k = 0;
		}

		return s == null && k == 0 ? SHAPELESS_SET : new Shape(s, k);
	}

	public static Object state(int s, Collection<?> o) {
		Object rv;
		if ((s & ObjectShape.SORTED) == 0) {
			rv = null;
		} else {
			if ((s & ObjectShape.SET) != 0) {
				rv = ((SortedSet<?>) o).comparator();
			} else if ((s & ObjectShape.PRIORITY_B_QUEUE) == ObjectShape.PRIORITY_B_QUEUE) {
				rv = ((PriorityBlockingQueue<?>) o).comparator();
			} else if ((s & ObjectShape.PRIORITY_QUEUE) == ObjectShape.PRIORITY_QUEUE) {
				rv = ((PriorityQueue<?>) o).comparator();
			} else {
				Log.warn("Can't capture state of:%s", o);
				rv = null;
			}
		}
		return rv;
	}

	public static Shape stateless(int k) {
		return new Shape(null, k);
	}

	public Object state;

	public int k;

	public Shape(int k) {
		this.k = k;
	}

	public Shape(Object state, int k) {
		this.state = state;
		this.k = k;
	}

	public boolean canBeNull() {
		return (k & ObjectShape.NULLABLE) != 0;
	}

	public boolean disregardRefs() {
		return (k & ObjectShape.ONLY_PAYLOAD) != 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Shape other = (Shape) obj;
		if (k != other.k)
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	public boolean hasDeclaredHierarchy() {
		return state instanceof Hierarchy;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	public boolean hasPolymorphicHierarchy() {
		Hierarchy h = hierarchy();

		// return !h.superType.isFinal() || !h.complete;
		return !h.complete || (h.types != null && h.types.length > 1);
	}

	public Hierarchy hierarchy() {
		return (Hierarchy) (state instanceof Hierarchy ? state : null);
	}

	public Class<?>[] hierarchyTypes() {
		Hierarchy h = hierarchy();

		if (h == null) {
			throw new IllegalStateException();
		}

		ExtendedType[] types = h.types;

		Class<?>[] rv;

		if (types == null || types.length == 0) {
			rv = null;
		} else {
			rv = new Class<?>[types.length];

			for (int i = 0; i < rv.length; i++) {
				rv[i] = types[i].runtimeType();
			}
		}

		return rv;
	}

	public boolean isArray() {
		return (k & ObjectShape.ARRAY) != 0;
	}

	public boolean isCollection() {
		return (k & ObjectShape.COLLECTION) != 0;
	}

	public boolean isEnumSet() {
		return (k & ObjectShape.ENUM_SET) == ObjectShape.ENUM_SET;
	}

	public boolean isSet() {
		return (k & ObjectShape.SET) != 0;
	}

	public void onStack(MethodVisitor mv) {

		mv.visitTypeInsn(NEW, _Shape.name);
		mv.visitInsn(DUP);

		Hierarchy h = hierarchy();

		if (h != null) {
			h.onstack(mv);
		} else {
			mv.visitInsn(ACONST_NULL);
		}
		Symbols.loadNumber(mv, k);

		mv.visitMethodInsn(INVOKESPECIAL, _Shape.name, _Class.ctor,
				_Shape.statefulCtor, false);
	}

	public void setDisregardRefs(boolean op) {
		if (op) {
			k |= ObjectShape.ONLY_PAYLOAD;
		} else {
			k &= ~ObjectShape.ONLY_PAYLOAD;
		}
	}

	public void setNullable(boolean n) {
		if (n) {
			k |= ObjectShape.NULLABLE;
		} else {
			k &= ~ObjectShape.NULLABLE;
		}
	}

	public byte sorted() {
		return (byte) (k & ObjectShape.SORTED);
	}

	public Shape with(boolean n, boolean op) {
		int k = this.k;

		if (n) {
			k |= ObjectShape.NULLABLE;
		} else {
			k &= ~ObjectShape.NULLABLE;
		}

		if (op) {
			k |= ObjectShape.ONLY_PAYLOAD;
		} else {
			k &= ~ObjectShape.ONLY_PAYLOAD;
		}

		this.k = k;

		return this;
	}

	public boolean isHierarchyComplete() {
		return state instanceof Hierarchy && ((Hierarchy) state).complete;
	}
	
	public Shape clone(){
		try {
			return (Shape) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}
}