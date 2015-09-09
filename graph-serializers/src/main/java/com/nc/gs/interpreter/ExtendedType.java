package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ASM5;
import static symbols.io.abstraction._Tags.ExtendedType.ACC_COMPRESS_PRIMS;
import static symbols.io.abstraction._Tags.ExtendedType.ACC_DECL_FINAL;
import static symbols.io.abstraction._Tags.ExtendedType.ACC_DECL_INNER;
import static symbols.io.abstraction._Tags.ExtendedType.ACC_DECL_PVT;
import static symbols.io.abstraction._Tags.ExtendedType.ACC_FULL_HIERARCHY;
import static symbols.io.abstraction._Tags.ExtendedType.ACC_INNER;
import static symbols.io.abstraction._Tags.ExtendedType.ACC_LEAF;
import static symbols.io.abstraction._Tags.ExtendedType.ACC_REJECT_NON_TRANSIENT;
import static symbols.io.abstraction._Tags.ExtendedType.ACC_WELCOME_TRANSIENT;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.nc.gs.log.Log;
import com.nc.gs.util.Utils;

import symbols.io.abstraction._Meta;
import symbols.io.abstraction._Tags;
import symbols.java.lang._Class;
import symbols.java.lang._Iterable;
import symbols.java.lang._Number;
import symbols.java.lang._Object;
import symbols.java.util._Collection;
import symbols.java.util._EnumSet;
import symbols.java.util._List;
import symbols.java.util._Map;
import symbols.java.util._Set;

public final class ExtendedType implements Comparable<ExtendedType> {

	static class ShallowVisitor extends ClassVisitor {

		ExtendedType info;

		public ShallowVisitor() {
			super(ASM5);
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			info = new ExtendedType(access, name, superName, signature, interfaces);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (!visible) {
				switch (desc) {
				case _Meta.LeafNode.desc:
					info.access |= ACC_LEAF;
					break;
				case _Meta.Fields.desc:
					return new ClassInfo.Fields(info);
				case _Meta.Hierarchy.desc:
					info.access |= ACC_FULL_HIERARCHY;

					if (info.isFinal()) {
						return null;
					}

					return new AnnotationVisitor(Opcodes.ASM5, super.visitAnnotation(desc, visible)) {

						@Override
						public void visit(String name, Object value) {
							if (name == null) {
								// ExtendedType::isA might lead to circularity issues. Force
								// eager-caching of info.
								try (VisitationContext vc = VisitationContext.current()) {
									vc.visited(info);

									final ExtendedType et = ExtendedType.forInternalName(((Type) value).getInternalName(), false);

									if (et.isA(info)) {
										Set<ExtendedType> children = info.children;

										if (children == null) {
											children = info.children = new HashSet<ExtendedType>(2);
										}

										children.add(et);
									}
								}
							} else if (name.equals(_Meta.Hierarchy.complete)) {
								if (!(boolean) value) {
									info.access &= ~ACC_FULL_HIERARCHY;
								}
							}
						}

						@Override
						public AnnotationVisitor visitArray(String name) {
							if (name.equals(_Meta.Hierarchy.types)) {
								return this;
							}

							return null;
						}

					};
				default:
					break;
				}

			}
			return null;
		}

		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			ExtendedType info = this.info;
			if (outerName != null) {
				if (info.name.equals(outerName)) {
					info.access |= ACC_DECL_INNER;
				} else if ((innerName != null) && info.name.startsWith(outerName) && info.name.endsWith(innerName)) {
					info.access |= (ACC_INNER | access);
				}
			} else {
				// I am the one visiting an anonymous inner class class
				if (!name.equals(info.name)) {
					info.access |= ACC_DECL_INNER;
				}
				// I am the inner class!
				else {
					info.access |= (ACC_INNER | access);
				}
			}
		}

		@Override
		public void visitOuterClass(String owner, String name, String desc) {
			super.visitOuterClass(owner, name, desc);
			info.access |= ACC_INNER;
		}

	}

	static final Class<?>[] PM = { boolean.class, byte.class, char.class, short.class, int.class, float.class, double.class };

	static String[] EMPTY = new String[0];

	static final ExtendedType OBJECT;

	static final ExtendedType BOOLEAN;

	static final ExtendedType BYTE;

	static final ExtendedType SHORT;

	static final ExtendedType CHAR;

	static final ExtendedType INT;

	static final ExtendedType FLOAT;

	static final ExtendedType LONG;

	static final ExtendedType DOUBLE;

	static final ExtendedType COLLECTION;

	static final ExtendedType LIST;

	static final ExtendedType SET;

	static final ExtendedType MAP;
	static final ExtendedType ENUM_SET;
	static final ExtendedType ENUM_MAP;
	static final Pattern SYSTEM_RESOURCES;

	static {
		BOOLEAN/**/ = prim(_Number.boolean_D);
		CHAR/*   */ = prim(_Number.char_D);
		BYTE/*   */ = prim(_Number.byte_D);
		SHORT/*  */ = prim(_Number.short_D);
		INT/*    */ = prim(_Number.int_D);
		FLOAT/*  */ = prim(_Number.float_D);
		LONG/*   */ = prim(_Number.long_D);
		DOUBLE/* */ = prim(_Number.double_D);

		OBJECT = new ExtendedType(ACC_PUBLIC, _Object.name, null, null, null);

		COLLECTION/**/ = intf(_Collection.name, _Iterable.name);
		LIST/*      */ = intf(_List.name, _Collection.name, _Iterable.name);
		MAP/*       */ = intf(_Map.name);
		SET/*       */ = intf(_Set.name, _Collection.name, _Iterable.name);

		ENUM_SET = type(_EnumSet.name, _Set.abstractIN, _Set.name, _Collection.name, _Iterable.name);

		ENUM_MAP = type(_EnumSet.name, _Map.abstractIN, _Map.name);

		SYSTEM_RESOURCES = Pattern.compile("(java/|javax/|sun/|com/sun/|com/oracle/)");
	}

	static ExtendedType basicOrNull(String name) {
		if (name.length() > 1) {
			return null;
		}

		switch (name.charAt(0)) {
		case 'Z':
			return BOOLEAN;
		case 'B':
			return BYTE;
		case 'S':
			return SHORT;
		case 'C':
			return CHAR;
		case 'I':
			return INT;
		case 'F':
			return FLOAT;
		case 'J':
			return LONG;
		case 'D':
			return DOUBLE;
		}
		return null;
	}

	public static ExtendedType forInternalName(String name, boolean deep) {
		ExtendedType cached = basicOrNull(name);

		if (cached != null) {
			return cached;
		}

		try (VisitationContext current = VisitationContext.current()) {
			cached = current.basic(name);

			if (cached != null) {
				return cached;
			}

			if ((cached == null) && name.startsWith("[")) {
				current.visited(cached = prim(name));
				return cached;
			}

			final ExtendedType rv;
			if (isSystemResource(name)) {
				if (name.equals(_Object.name)) {
					rv = OBJECT;
				} else {
					// Try with resources do not throw NPE on close if resource
					// is null! :)
					try (InputStream is = Utils.streamSysCode(name)) {

						if (is != null) {
							ClassReader cr = new ClassReader(is);

							ShallowVisitor cv = new ShallowVisitor();
							cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);

							rv = cv.info;
						} else {
							try {
								Class<?> rt = Class.forName(name.replace("/", "."));
								String sn = null;

								if (!rt.isInterface()) {
									Class<?> p = rt.getSuperclass();
									sn = p == null ? null : p.getName().replace('.', '/');
								}

								rv = new ExtendedType(rt.getModifiers(), name, sn, null, toStr(rt.getInterfaces()));
							} catch (ClassNotFoundException ex) {
								throw new RuntimeException(ex);
							}
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}

				}
			} else {
				try (InputStream is = Utils.streamAnyCode(name)) {
					ClassReader cr = new ClassReader(is);

					ShallowVisitor cv = new ShallowVisitor();
					cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);

					rv = cv.info;

				} catch (IOException e) {
					return Utils.rethrow(e);
				}
			}

			current.visited(rv);
			if (deep) {
				rv.parent = ((rv == OBJECT) || (rv.superName == null)) ? null : forInternalName(rv.superName, deep);
			}

			return rv;
		}
	}

	public static Type forName(String name) {
		Type rv = primitiveOrNull(name);

		if (rv == null) {
			rv = Type.getObjectType(name);
		}

		return rv;
	}

	public static ExtendedType forRuntime(Class<?> clazz) {
		return forInternalName(Type.getInternalName(clazz), false);
	}

	public static ExtendedType[] forRuntime(Class<?>[] types) {
		ExtendedType[] rv = types == null ? null : new ExtendedType[types.length];

		if (rv != null) {
			for (int i = 0; i < rv.length; i++) {
				rv[i] = forRuntime(types[i]);
			}
		}

		return rv;
	}

	public static ExtendedType forTypeDescriptor(String desc, boolean deep) {
		ExtendedType rv = ExtendedType.basicOrNull(desc);

		if (rv == null) {
			rv = forInternalName(Type.getType(desc).getInternalName(), deep);
		}

		return rv;
	}

	private static ExtendedType intf(String name, String... intf) {
		return new ExtendedType(ACC_PUBLIC | ACC_INTERFACE, name, name, null, intf);
	}

	public static boolean isSystemResource(String name) {
		return SYSTEM_RESOURCES.matcher(name).find();
	}

	private static ExtendedType prim(String desc) {
		return new ExtendedType(desc);
	}

	public static Type primitiveOrNull(String name) {
		if (name.length() > 1) {
			return null;
		}

		switch (name.charAt(0)) {
		case 'Z':
			return Type.BOOLEAN_TYPE;
		case 'B':
			return Type.BYTE_TYPE;
		case 'S':
			return Type.SHORT_TYPE;
		case 'C':
			return Type.CHAR_TYPE;
		case 'I':
			return Type.INT_TYPE;
		case 'F':
			return Type.FLOAT_TYPE;
		case 'J':
			return Type.LONG_TYPE;
		case 'D':
			return Type.DOUBLE_TYPE;
		}
		return null;
	}

	private static String[] toStr(Class<?>[] interfaces) {
		if ((interfaces == null) || (interfaces.length == 0)) {
			return EMPTY;
		}
		String[] rv = new String[interfaces.length];

		for (int i = 0; i < rv.length; i++) {
			rv[i] = interfaces[i].getName().replace('.', '/').intern();
		}

		return rv;
	}

	private static ExtendedType type(String name, String parent, String... intf) {
		return new ExtendedType(ACC_PUBLIC, name, parent, null, intf);
	}

	public long access;

	public final String name;

	public final String desc;
	public final String superName;
	public final String signature;

	public String[] interfaces;
	String packageName;
	Type type;

	ExtendedType parent;

	Set<ExtendedType> children;

	public ExtendedType(int access, String name, String superName, String signature, String[] interfaces) {
		super();
		this.access = access;
		this.name = name.intern();
		desc = name.charAt(0) == '[' ? name : ("L" + name + ";").intern();
		this.superName = superName == null ? null : superName.intern();
		this.signature = signature;
		this.interfaces = (interfaces == null) || (interfaces.length == 0) ? EMPTY : interfaces;
	}

	private ExtendedType(String name) {
		access = ACC_FINAL & ACC_PUBLIC;
		this.name = name;
		desc = name;
		superName = null;
		signature = null;
		interfaces = EMPTY;
	}

	public ExtendedType basicComponentType() {
		ExtendedType rv = this;
		if (isArray()) {
			String n = name;
			int len = n.length();
			boolean obj = n.lastIndexOf(';') > 0;
			String iN = n.substring(n.lastIndexOf('[') + (obj ? 2 : 1), obj ? len - 1 : len);

			try (VisitationContext vc = VisitationContext.current()) {
				return forInternalName(iN, false);
			}

		}
		return rv;
	}

	public boolean canReplace(ExtendedType declared) {
		boolean rv = false;
		if (isAbstract()) {
			Log.warn("Invalid replacement declaration. %s is not concrete", this);
		} else if (!declared.isAssignableFrom(this)) {
			Log.warn("Invalid replacement declaration. %s is not in same hierarchy of %s", this, declared);
		} else {
			rv = true;
		}

		return rv;
	}

	@Override
	public int compareTo(ExtendedType r) {
		if ((this == r) || desc.equals(r.desc)) {
			return 0;
		}

		if (isAssignableFrom(r)) {
			return 1;
		}

		if (r.isAssignableFrom(this)) {
			return -1;
		}

		return name.compareTo(r.name);
	}

	public boolean compressPrims() {
		return (access & ACC_COMPRESS_PRIMS) != 0;
	}

	private boolean contains(String[] interfaces, String name) {
		if (interfaces == EMPTY) {
			return false;
		}
		for (String interface1 : interfaces) {
			if (interface1.equals(name)) {
				return true;
			}
		}

		return false;
	}

	public boolean declaresFinalField(boolean deep) {
		boolean rv = (access & ACC_DECL_FINAL) != 0;

		if (!rv && deep && (lazyParent() != null)) {
			rv = lazyParent().declaresFinalField(deep);

			if (rv) {
				fieldDeclared(ACC_FINAL);
			}
		}

		return rv;
	}

	public boolean declaresPrivateField(boolean deep) {
		boolean rv = (access & ACC_DECL_PVT) != 0;

		if (!rv && deep && (lazyParent() != null)) {
			rv = lazyParent().declaresPrivateField(deep);

			if (rv) {
				fieldDeclared(ACC_PRIVATE);
			}
		}

		return rv;
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
		ExtendedType other = (ExtendedType) obj;
		if (desc == null) {
			if (other.desc != null) {
				return false;
			}
		} else if (!desc.equals(other.desc)) {
			return false;
		}
		return true;
	}

	public void fieldDeclared(int access) {
		if ((access & ACC_PRIVATE) != 0) {
			this.access |= ACC_DECL_PVT;
		}

		if ((access & ACC_FINAL) != 0) {
			this.access |= ACC_DECL_FINAL;
		}

	}

	public String getClassName() {
		return type().getClassName();
	}

	public String getInternalName() {
		return name;
	}

	public int getSort() {
		Type t = primitiveOrNull(name);

		int s;

		if (t != null) {
			s = t.getSort();
		} else {
			s = type().getSort();
		}

		return s;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((desc == null) ? 0 : desc.hashCode());
		return result;
	}

	public Set<ExtendedType> hierarchy() {
		Set<ExtendedType> rv = children;

		return rv == null ? Collections.emptySet() : rv;
	}

	public boolean isA(ExtendedType upper) {
		if ((this == upper) || name.equals(upper.name)) {
			return true;
		}

		ExtendedType curr = this;
		String[] currIfaces = curr.interfaces;

		do {
			if (upper.isInterface()) {
				if ((currIfaces != null) && (currIfaces.length > 0)) {
					if (contains(currIfaces, upper.name)) {
						return true;
					}
					if (walkInterfaces(curr, upper.name)) {
						return true;
					}
				}

			} else {
				if (curr.name.equals(upper.name)) {
					return true;
				}
			}

			curr = curr.lazyParent();
			currIfaces = curr == null ? EMPTY : curr.interfaces;
		} while (curr != null);

		return false;
	}

	public boolean isAbstract() {
		return (access & ACC_ABSTRACT) != 0;
	}

	public boolean isAbstractEnum() {
		return isEnum() && name.equals(_Class.Enum);
	}

	public boolean isArray() {
		return name.charAt(0) == '[';
	}

	public boolean isAssignableFrom(ExtendedType lower) {
		return lower.isA(this);
	}

	public boolean isEnum() {
		return (access & ACC_ENUM) != 0;
	}

	public boolean isFinal() {
		return (access & ACC_FINAL) != 0;
	}

	public boolean isFullHierarchyDeclared() {
		return (access & ACC_FULL_HIERARCHY) != 0;
	}

	public boolean isInnerClass() {
		return (access & ACC_INNER) != 0;
	}

	public boolean isInSameNamespace(ExtendedType other) {
		if ((other == this) || other.name.equals(name)) {
			return true;
		}

		return packageName().equals(other.packageName());
	}

	public boolean isInterface() {
		return (access & ACC_INTERFACE) != 0;
	}

	public boolean isLeaf() {
		return (access & ACC_LEAF) != 0;
	}

	public boolean isNonStaticInnerClass() {
		return ((access & ACC_INNER) != 0) && ((access & ACC_STATIC) == 0);
	}

	public boolean isPrimitive() {
		return name.length() == 1;
	}

	public boolean isPrivate() {
		return (access & ACC_PRIVATE) != 0;
	}

	public boolean isSynthetic() {
		return (access & ACC_SYNTHETIC) != 0;
	}

	public boolean isSystemResource() {
		return isSystemResource(name);
	}

	public ExtendedType lazyParent() {
		if ((this == OBJECT) || isInterface()) {
			return null;
		}

		ExtendedType p = parent;

		if ((p == null) && (p != OBJECT) && (superName != null)) {
			parent = p = forInternalName(superName, false);
		}

		return p;
	}

	public String packageName() {
		String pn = packageName;

		if (pn == null) {
			int last = name.lastIndexOf("/");

			pn = packageName = last > 0 ? name.substring(0, last) : "";
		}

		return pn;
	}

	public Class<?> primitiveOrNullRT(String nameOrDesc) {
		Class<?> rv = Utils.primitive(nameOrDesc);

		if (rv == null) {
			for (Class<?> c : PM) {
				if (c.getName().equals(nameOrDesc)) {
					rv = c;
					break;
				}
			}
		}

		return rv;
	}

	public boolean propagateInterned() {
		return (access & _Tags.ExtendedType.ACC_INTERNED) != 0;
	}

	public boolean propagateOnlyPayload() {
		return (access & _Tags.ExtendedType.ACC_ONLY_DATA) != 0;
	}

	public Class<?> runtimeType() {
		Class<?> rv = primitiveOrNullRT(name);

		if (rv == null) {
			try {
				String javaName;

				if (type().getSort() == Type.ARRAY) {
					javaName = type().getDescriptor().replace('/', '.');
				} else {
					javaName = type().getClassName();
				}
				rv = Class.forName(javaName, false, Thread.currentThread().getContextClassLoader());
			} catch (ClassNotFoundException e) {
				rv = Utils.rethrow(e);
			}
		}
		return rv;
	}

	public String simpleName() {
		int ix = name.lastIndexOf('/');
		return (ix > 0) && (ix < name.length()) ? name.substring(ix + 1) : name;
	}

	@Override
	public String toString() {
		return name;
	}

	public Type type() {
		Type rv = type;
		return rv == null ? rv = type = forName(name) : rv;
	}

	private boolean walkInterfaces(ExtendedType curr, String name) {
		String[] intf = curr.interfaces;

		if ((intf != null) && (intf.length > 0)) {
			for (String iface : intf) {
				ExtendedType et = ExtendedType.forInternalName(iface, false);
				if (et.name.equals(name)) {
					return true;
				}
				if (walkInterfaces(et, name)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean welcomesAllNonTransient() {
		return (access & ACC_REJECT_NON_TRANSIENT) == 0;
	}

	public boolean welcomesTransient() {
		return (access & ACC_WELCOME_TRANSIENT) != 0;
	}
}