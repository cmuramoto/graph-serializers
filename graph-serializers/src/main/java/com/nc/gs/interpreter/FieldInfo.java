package com.nc.gs.interpreter;

import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_COL;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_COL_CMP;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_COMPLETE_HIERARCHY;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_COMPRESSED;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_IN;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_LEAF;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_MAP;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_MAP_CMP;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_MAYBE_OPAQUE;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_NON_NULL;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_NON_SERIALIZED;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_ONLY_DATA;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_SET;
import static symbols.io.abstraction._Tags.FieldInfo.ACC_SET_CMP;
import gnu.trove.set.hash.THashSet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

import symbols.io.abstraction._ArraySerializer;
import symbols.io.abstraction._CollectionSerializer;
import symbols.io.abstraction._Context;
import symbols.io.abstraction._EnumSetSerializer;
import symbols.io.abstraction._GraphSerializer;
import symbols.io.abstraction._LeaftTypeArraySerializer;
import symbols.io.abstraction._MapSerializer;
import symbols.io.abstraction._Meta;
import symbols.io.abstraction._SetSerializer;
import symbols.io.abstraction._Sink;
import symbols.io.abstraction._Source;
import symbols.io.abstraction._Tags.ObjectShape;
import symbols.java.lang._Class;
import symbols.java.lang._Number;
import symbols.java.lang._Object;

import com.nc.gs.log.Log;
import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Map;
import com.nc.gs.util.Pair;
import com.nc.gs.util.Utils;

public final class FieldInfo extends FieldVisitor implements Comparable<FieldInfo> {

	public static class CollectionMeta extends AnnotationVisitor {

		boolean optimize;
		ShapeVisitor shapeVisitor;
		ExtendedType concreteImpl;
		public boolean replacement;

		public CollectionMeta() {
			super(Opcodes.ASM5);
		}

		public ExtendedType getConcreteImpl() {
			return concreteImpl;
		}

		public ShapeVisitor getShape() {
			return shapeVisitor;
		}

		public boolean isOptimize() {
			return optimize;
		}

		ShapeVisitor shapeVisitor() {
			ShapeVisitor sv = shapeVisitor;
			return sv != null ? sv : (shapeVisitor = new ShapeVisitor());
		}

		@Override
		public void visit(String name, Object value) {
			switch (name) {
			case _Meta.Collection.optimize:
				optimize = (boolean) value;
				break;
			case _Meta.Collection.concreteImpl:
				concreteImpl = ExtendedType.forInternalName(((Type) value).getInternalName(), false);
				break;
			case _Meta.Collection.implForReplacement:
				replacement = (boolean) value;
				break;
			default:
				break;
			}
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String desc) {
			if (desc.equals(_Meta.Shape.desc)) {
				return shapeVisitor();
			}
			return null;
		}

		@Override
		public void visitEnd() {
			final ExtendedType ci = concreteImpl;
			if ((ci != null) && ci.isAbstract()) {
				Log.warn("Declared concreteImpl as an abstract type[%s]. Information will be disregarded!", ci.getClassName());

				concreteImpl = null;
			}

			final ShapeVisitor s = shapeVisitor;

			if (s != null) {

			}
		}

	}

	static class Generalizations extends AnnotationVisitor {

		boolean complete = true;
		Set<ExtendedType> gens;

		public Generalizations() {
			super(Opcodes.ASM5);
		}

		@Override
		public void visit(String name, Object value) {
			if (_Meta.Hierarchy.complete.equals(name)) {
				complete = (boolean) value;
			} else {
				// visiting array
				final ExtendedType et = ExtendedType.forInternalName(((Type) value).getInternalName(), false);

				if (et.isAbstract()) {
					Log.warn("Declared Hierarchy with an abstract type [%s]. Bypassing.", et.getClassName());
				} else {
					Set<ExtendedType> gens = this.gens;

					if (gens == null) {
						this.gens = gens = new THashSet<>();
					}

					if (gens != null) {
						gens.add(et);
					}
				}
			}
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			return this;
		}
	}

	public static class MapMeta extends AnnotationVisitor {
		ShapeVisitor key;

		ShapeVisitor val;

		ExtendedType concreteImpl;

		boolean optimize;

		boolean replacement;

		public MapMeta() {
			super(Opcodes.ASM5);
		}

		public ExtendedType getConcreteImpl() {
			return concreteImpl;
		}

		public ShapeVisitor getKey() {
			return key;
		}

		public ShapeVisitor getVal() {
			return val;
		}

		public ShapeVisitor key() {
			ShapeVisitor k = key;
			return k != null ? k : (key = new ShapeVisitor());
		}

		@Override
		public String toString() {
			return MessageFormat.format("Map [key={0}, val={1}]", key, val);
		}

		private ShapeVisitor val() {
			ShapeVisitor v = val;
			return v != null ? v : (val = new ShapeVisitor());
		}

		@Override
		public void visit(String name, Object value) {
			switch (name) {
			case _Meta.Map.concreteImpl:
				concreteImpl = ExtendedType.forInternalName(((Type) value).getInternalName(), false);
				break;
			case _Meta.Map.optimize:
				optimize = (boolean) value;
				break;
			case _Meta.Map.implForReplacement:
				replacement = (boolean) value;
				break;
			default:
				break;
			}
		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String desc) {
			switch (name) {
			case _Meta.Map.key:
				return key();
			case _Meta.Map.val:
				return val();
			default:
				return null;
			}
		}

	}

	public class Serialized extends AnnotationVisitor {

		public Serialized() {
			super(Opcodes.ASM5);
		}

		@Override
		public void visit(String name, Object value) {
			overlaid = value;
		}
	}

	public static class ShapeVisitor extends AnnotationVisitor {
		Set<ExtendedType> hierarchy;
		boolean onlyPayload = false;
		boolean nullable = true;
		boolean complete = true;
		boolean isLeaf = false;

		public ShapeVisitor() {
			super(Opcodes.ASM5);
		}

		public ExtendedType first() {
			final Set<ExtendedType> h = hierarchy;

			if ((h != null) && !h.isEmpty()) {
				return h.iterator().next();
			}
			return null;
		}

		public Set<ExtendedType> getHierarchy() {
			return hierarchy;
		}

		public boolean isNullable() {
			return nullable;
		}

		public boolean isOnlyPayload() {
			return onlyPayload;
		}

		@Override
		public void visit(String name, Object value) {
			if (name != null) {
				switch (name) {
				case _Meta.Shape.onlyPayload:
					onlyPayload = (boolean) value;
					break;
				case _Meta.Shape.nullable:
					nullable = (boolean) value;
					break;
				case _Meta.Hierarchy.complete:
					complete = (boolean) value;
					break;
				}
			} else {
				// null name means we are visiting the types array, since we
				// have only one, there's
				// no need for branching.
				if (hierarchy == null) {
					hierarchy = new THashSet<>(2);
				}
				final ExtendedType et = ExtendedType.forInternalName(((Type) value).getInternalName(), false);

				if (et.isAbstract()) {
					Log.warn("Declared Hierarchy with an abstract type [%s]. Bypassing.", et.getClassName());
				} else {
					hierarchy.add(et);
				}
			}

		}

		@Override
		public AnnotationVisitor visitAnnotation(String name, String desc) {
			if (name.equals(_Meta.Shape.hierarchy)) {
				return this;
			}
			return null;
		}

		@Override
		public AnnotationVisitor visitArray(String name) {
			if (name.equals(_Meta.Hierarchy.types)) {
				return this;
			}
			return null;
		}

	}

	static ExtendedType[] mergeConcreteGenericInfo(ExtendedType superType, ShapeVisitor s) {

		ExtendedType[] rv;

		final Set<ExtendedType> types = new THashSet<>(2);

		check: {
			if (s != null) {
				if (s.isLeaf) {
					rv = new ExtendedType[]{ superType };
					break check;
				} else {
					final Set<ExtendedType> hierarchy = s.hierarchy;

					if (hierarchy != null) {
						for (final ExtendedType et : hierarchy) {
							if ((et != null) && !et.isAbstract()) {
								if (et.isA(superType)) {
									types.add(et);
								} else {
									Log.warn("ShapeVisitor declares type %s not in hierarchy of %s. Bypassing.", et.name, superType.name);
								}
							}
						}
					}
				}
			}

			int last = 0;

			types.remove(superType);

			if (!superType.isAbstract() && !superType.desc.equals(ExtendedType.OBJECT.desc)) {
				last++;
			}

			rv = types.toArray(new ExtendedType[types.size() + last]);

			if (last > 0) {
				rv[rv.length - 1] = superType;
			} else if (rv.length == 0) {
				rv = null;
			}
		}
		return rv;
	}

	public long access;

	public String name;
	public String desc;
	public String signature;
	public long offset;
	final ExtendedType owner;

	ExtendedType type;

	Object hierarchy;

	/**
	 * This field is used to represent MetaData that cannot be simultaneously declared with another
	 * type. The following annotations are considered conflicting to each other, so this overlaid
	 * field will trap one of the following, in descending priority:
	 * <ol>
	 * <li>{@link Serialized}</li>
	 * <li>{@link Map}</li>
	 * <li>{@link Collection}</li>
	 * </ol>
	 * That is, if {@link Serialized} is declared on a field, every other MetaData will be ignored.
	 * If the field declared both {@link ParentNode} and {@link Map}, the {@link ParentNode}
	 * annotation will be ignored.
	 */
	Object overlaid;

	public FieldInfo(ExtendedType owner, int access, String name, String desc, String signature, boolean compressed) {
		super(Opcodes.ASM5);
		this.owner = owner;
		this.access = access | (compressed ? ACC_COMPRESSED : 0);
		this.name = name;
		this.desc = desc;
		this.signature = signature;
	}

	public Type asmType() {
		return type().type();
	}

	public SpecialField asSpecial() {
		if (isMap()) {
			return asSpecialMap();
		}

		if (isSet() || isCollection() || isArray()) {
			return asSpecialStream();
		}

		return null;
	}

	private MapField asSpecialMap() {
		final com.nc.gs.interpreter.Shape sk = new com.nc.gs.interpreter.Shape(ObjectShape.NULLABLE);

		final com.nc.gs.interpreter.Shape sv = new com.nc.gs.interpreter.Shape(ObjectShape.NULLABLE);

		boolean rep = false;
		boolean optimize = false;

		ExtendedType mt = null;

		final Pair<Hierarchy, Hierarchy> pair = mergeHierarchyForMap();

		sk.state = pair.k;
		sv.state = pair.v;

		String targetIN = null;
		String targetD = null;

		if (isTypeFinalOrLeaf()) {
			mt = type();
			// remove leaf modifier because Pojo leaves are handled differently
			// than Map leaves
			access &= ~ACC_LEAF;
		}

		final MapMeta meta = mapMeta();

		if (meta != null) {

			if ((mt == null) || mt.isAbstract()) {
				mt = meta.getConcreteImpl();
			}

			final ShapeVisitor ks = meta.getKey();
			final ShapeVisitor vs = meta.getVal();

			if (ks != null) {
				sk.setNullable(ks.isNullable());
				sk.setDisregardRefs(ks.isOnlyPayload());
			}

			if (vs != null) {
				sv.setNullable(vs.isNullable());
				sv.setDisregardRefs(vs.isOnlyPayload());
			}

			if (pair.k.declaresTypes() || pair.v.declaresTypes()) {
				optimize = meta.optimize;
			}

			if (mt != null) {
				if (meta.replacement) {
					rep = mt.canReplace(type());
				}
			}

			if (optimize) {
				targetIN = Symbols._R_optimizedMapName(mt == null ? null : mt.name, sk, sv, rep);
				targetD = "L" + targetIN + ";";
			}
		}

		if (targetIN == null) {
			targetIN = _MapSerializer.name;
			targetD = _MapSerializer.desc;
		}

		return new MapField(mt, targetIN, targetD, sk, sv, rep, optimize);
	}

	private IterableField asSpecialStream() {
		// default values
		final com.nc.gs.interpreter.Shape s = new com.nc.gs.interpreter.Shape(null, isEnumSet() ? ObjectShape.ENUM_SET : isSet() ? ObjectShape.SET : isArray() ? ObjectShape.ARRAY : ObjectShape.COLLECTION).with(true, false);

		final Hierarchy h = mergedHierarchyForStreameable();

		ExtendedType type = null;

		ExtendedType[] types = h.types();

		s.state = h;

		boolean opt = false;
		boolean rep = false;

		String targetIN = null;
		String targetD = null;

		final CollectionMeta meta = collectionMeta();

		if (isTypeFinalOrLeaf()) {
			if (isArray()) {
				type = null;
			} else {
				type = type();
				access &= ~ACC_LEAF;
			}
		}

		if (meta != null) {
			final ExtendedType colType = meta.concreteImpl;

			if (type == null) {
				if ((colType != null) && !colType.isAbstract()) {
					type = colType;
				} else {
					type = type();
				}
			}

			final ShapeVisitor shapeVisitor = meta.shapeVisitor;

			if (shapeVisitor != null) {
				s.k |= shapeVisitor.nullable ? ObjectShape.NULLABLE : 0;
				s.k |= shapeVisitor.onlyPayload ? ObjectShape.ONLY_PAYLOAD : 0;
			}

			if ((types != null) && (types.length > 0)) {
				if (!isArray() || (getDimension() == 1)) {
					opt = meta.optimize;
				}
			}

			if (meta.replacement) {
				rep = type.canReplace(type());
			}

			if (opt) {
				if (s.isSet()) {
					targetIN = Symbols._R_optimizedSetName(type.name, types, s);
				} else if (s.isArray()) {
					targetIN = Symbols._R_optimizedArrayName(basicComponentType().name, types, s);
				} else {
					targetIN = Symbols._R_optimizedCollectionName(type.name, types, s, rep);
				}

				targetD = ("L" + targetIN + ";").intern();
			}
		}

		if ((type == null) && !isArray()) {
			type = type();
		}

		if ((targetD == null) || (targetIN == null)) {
			if (s.isSet()) {
				if (s.isEnumSet()) {
					targetIN = _EnumSetSerializer.name;
					targetD = _EnumSetSerializer.desc;
				} else {
					targetIN = _SetSerializer.name;
					targetD = _SetSerializer.desc;
				}
			} else if (s.isArray()) {
				if ((types != null) && (types.length == 1) && h.complete) {
					targetIN = _LeaftTypeArraySerializer.name;
					targetD = _LeaftTypeArraySerializer.desc;
					s.k |= ObjectShape.LEAF_ARRAY;
				} else {
					types = null;
					targetIN = _ArraySerializer.name;
					targetD = _ArraySerializer.desc;
					s.k |= ObjectShape.ARRAY;
				}
			} else {
				targetIN = _CollectionSerializer.name;
				targetD = _CollectionSerializer.desc;
			}
		}

		return new IterableField(type, targetIN, targetD, s, rep, opt);
	}

	public ExtendedType basicComponentType() {
		return type().basicComponentType();
	}

	public String bridgeGetDesc(ClassInfo owner) {
		return String.format("(%s)%s", owner.desc(), desc);
	}

	public String bridgeSetDesc(ClassInfo owner) {
		return String.format("(%s%s)V", owner.desc(), desc);
	}

	public boolean canBeNull() {
		return !isPrimitive() && ((access & ACC_NON_NULL) == 0);
	}

	public boolean canBeOpaque() {
		return ((access & ACC_MAYBE_OPAQUE) != 0) && (type().isInterface() || type().isAbstract());
	}

	public CollectionMeta collectionMeta() {
		return (CollectionMeta) (overlaid instanceof CollectionMeta ? overlaid : null);
	}

	CollectionMeta collectionMetaLazy() {
		return (CollectionMeta) (overlaid instanceof CollectionMeta ? overlaid : (overlaid = new CollectionMeta()));
	}

	@Override
	public int compareTo(FieldInfo o) {
		return Long.compare(objectFieldOffset(), o.objectFieldOffset());
	}

	public boolean disregardReference() {
		return (access & ACC_ONLY_DATA) != 0;
	}

	public CollectionMeta getCollectionMeta() {
		final Object ov = overlaid;

		if (ov instanceof CollectionMeta) {
			return (CollectionMeta) ov;
		}

		return null;
	}

	public int getDimension() {
		return type().name.lastIndexOf('[') + 1;
	}

	public ExtendedType[] getGenericParameters() {
		ExtendedType[] rv;

		final String sig = signature;

		if ((sig == null) || sig.isEmpty()) {
			rv = null;
		} else {
			final int start = sig.indexOf('<');

			final int end = sig.lastIndexOf('>');

			// invalid sig
			if ((start < 0) || (end < 0)) {
				rv = null;
			} else {
				String args = sig.substring(start + 1, end);

				if (args.indexOf('<') > 0) {
					Log.info("Nested generics not supported!!!");
					rv = null;
				} else {

					args = args.replace("*", _Object.desc).replaceAll("[+-]", "");

					final Type[] types = Type.getArgumentTypes("(" + args + ")V");

					rv = new ExtendedType[types.length];

					try (VisitationContext vc = VisitationContext.current()) {
						for (int i = 0; i < types.length; i++) {
							rv[i] = ExtendedType.forInternalName(types[i].getInternalName(), false);
						}
					}
				}
			}
		}

		return rv;
	}

	public String getTypeInternalName() {
		return type().name;
	}

	public boolean hasDeclaredHierarchy() {
		final Set<ExtendedType> gens = overlaidGens();
		if ((gens == null) || gens.isEmpty()) {
			return hierarchy instanceof Hierarchy;
		}

		return true;
	}

	public boolean hasSerializer() {
		return overlaid instanceof Serialized;
	}

	public Hierarchy hierarchy() {
		Hierarchy rv = (Hierarchy) (hierarchy instanceof Hierarchy ? hierarchy : null);

		if (rv == null) {
			final Set<ExtendedType> gen = overlaidGens();

			if (gen == null) {
				throw new IllegalStateException("Field does not declare hierarchy!");
			} else {
				final List<ExtendedType> l = new ArrayList<>(gen.size());

				final ExtendedType et = type();

				for (final ExtendedType c : gen) {

					if (c.isAbstract()) {
						Log.warn("[%s::%s]Invalid generalization declaration [abstract]:%s", owner().simpleName(), name, c.simpleName());
					} else if (!et.isAssignableFrom(c)) {
						Log.warn("[%s::%s]Invalid generalization declaration %s not assignable from %s", owner().simpleName(), name, et.simpleName(), c.simpleName());
					} else if (!l.contains(c)) {
						l.add(c);
					}
				}

				if (!et.isAbstract() && !l.contains(et)) {
					l.add(et);
				}

				final ExtendedType[] types = l.toArray(new ExtendedType[l.size()]);
				Arrays.sort(types);

				hierarchy = rv = new Hierarchy(et, types, isHierarchyComplete());
			}
		}

		return rv;
	}

	public ExtendedType info() {
		ExtendedType type = this.type;

		if (type == null) {
			try (VisitationContext c = VisitationContext.current()) {
				type = this.type = ExtendedType.forInternalName(name, false);
			}
		}

		return type;
	}

	public boolean isAbstract() {
		return type().isAbstract();
	}

	public boolean isAbstractEnum() {
		return desc.equals(_Class.Enum_D);
	}

	public boolean isArray() {
		return type().isArray();
	}

	public boolean isCollection() {
		boolean rv;
		if ((access & ACC_COL_CMP) != 0) {
			rv = (access & ACC_COL) != 0;
		} else {
			access |= ACC_COL_CMP;

			if (rv = type().isA(ExtendedType.COLLECTION)) {
				access |= ACC_COL;
			}
		}
		return rv;
	}

	public boolean isCompressed() {
		switch (asmType().getSort()) {
		case Type.BOOLEAN:
			return true;
		case Type.BYTE:
			return false;
		default:
			return (access & ACC_COMPRESSED) != 0;
		}
	}

	public boolean isEnum() {
		return type().isEnum();
	}

	public boolean isEnumMap() {
		return type().isA(ExtendedType.ENUM_MAP);
	}

	public boolean isEnumSet() {
		return type().isA(ExtendedType.ENUM_SET);
	}

	public boolean isFinal() {
		return (access & Opcodes.ACC_FINAL) != 0;
	}

	public boolean isHierarchyComplete() {
		return (access & ACC_COMPLETE_HIERARCHY) != 0;
	}

	public boolean isIgnored() {
		return (access & ACC_NON_SERIALIZED) != 0;
	}

	public boolean isInternalNode() {
		return (access & ACC_IN) != 0;
	}

	public boolean isMap() {
		boolean rv;
		if ((access & ACC_MAP_CMP) != 0) {
			rv = (access & ACC_MAP) != 0;
		} else {
			access |= ACC_MAP_CMP;

			if (rv = type().isA(ExtendedType.MAP)) {
				access |= ACC_MAP;
			}
		}
		return rv;
	}

	public boolean isPrimitive() {
		final int s = type().getSort();
		return (s > 0) && (s < 9);
	}

	public boolean isReadAccessible(ExtendedType root) {
		return ((access & Opcodes.ACC_PRIVATE) == 0) && ((access & Opcodes.ACC_FINAL) == 0) && root.isInSameNamespace(owner());
	}

	public boolean isSet() {
		boolean rv;
		if ((access & ACC_SET_CMP) != 0) {
			rv = (access & ACC_SET) != 0;
		} else {
			access |= ACC_SET_CMP;

			if (rv = type().isA(ExtendedType.SET)) {
				access |= ACC_SET;
			}
		}
		return rv;
	}

	public boolean isSpecial() {
		return isArray() || isMap() || isSet() || isCollection();
	}

	public boolean isTransient() {
		return (access & ACC_TRANSIENT) != 0;
	}

	public boolean isTypeFinalOrLeaf() {
		boolean rv;
		if (isPrimitive() || isWrapper()) {
			rv = true;
		} else {
			if ((access & ACC_IN) != 0) {
				rv = type().isFinal();

				if (rv) {
					Log.warn("Class %s has declared Field %s with @InternalNode, but it's type %s is final!", owner(), name, type());
				}

			} else {
				rv = ((access & ACC_LEAF) != 0) || type().isFinal() || type().isLeaf();
			}
		}
		return rv;
	}

	public boolean isWrapper() {
		switch (desc) {
		case _Number.Boolean_D:
		case _Number.Byte_D:
		case _Number.Short_D:
		case _Number.Character_D:
		case _Number.Integer_D:
		case _Number.Float_D:
		case _Number.Long_D:
		case _Number.Double_D:
			return true;
		default:
			return false;
		}
	}

	public boolean isWriteAccessible(ClassInfo root) {
		return isWriteAccessible(root.basic());
	}

	public boolean isWriteAccessible(ExtendedType root) {
		return ((access & Opcodes.ACC_PRIVATE) == 0) && ((access & Opcodes.ACC_FINAL) == 0) && root.isInSameNamespace(owner());
	}

	public MapMeta mapMeta() {
		return (MapMeta) (overlaid instanceof MapMeta ? overlaid : null);
	}

	MapMeta mapMetaLazy() {
		return (MapMeta) (overlaid instanceof MapMeta ? overlaid : (overlaid = new MapMeta()));
	}

	public ExtendedType[] mergedConcreteTypes() {
		ExtendedType superType;

		if (isArray()) {
			final ExtendedType ct = type().basicComponentType();

			if (ct.isPrimitive()) {
				return null;
			}

			superType = type().basicComponentType();
		} else {
			final ExtendedType[] params = getGenericParameters();

			superType = (params != null) & (params.length == 1) ? params[0] : null;
		}
		final CollectionMeta meta = collectionMeta();

		final ShapeVisitor s = meta == null ? null : meta.getShape();

		return mergeConcreteGenericInfo(superType, s);
	}

	public Hierarchy mergedHierarchyForStreameable() {
		ExtendedType superType;
		ExtendedType[] types = null;

		if (isArray()) {
			final ExtendedType ct = type().basicComponentType();

			if (ct.isPrimitive() || ct.isAbstract()) {
				superType = ExtendedType.OBJECT;
			} else {
				superType = ct;
			}
		} else {
			final ExtendedType[] params = getGenericParameters();

			superType = (params != null) && (params.length == 1) ? params[0] : ExtendedType.OBJECT;
		}

		boolean complete = superType.isFinal() || superType.isLeaf();

		final CollectionMeta meta = collectionMeta();
		ShapeVisitor s;

		if (meta != null) {
			s = meta.getShape();

			if ((s != null) && !complete) {
				complete = (superType != ExtendedType.OBJECT) && !superType.isAbstract() && (s.complete || s.isLeaf);
			}
		} else {
			s = null;
		}

		types = mergeConcreteGenericInfo(superType, s);

		return new Hierarchy(superType, types, complete);
	}

	public Pair<Hierarchy, Hierarchy> mergeHierarchyForMap() {
		final ExtendedType[] types = getGenericParameters();

		ExtendedType ks;

		ExtendedType vs;

		if ((types != null) && (types.length == 2)) {
			ks = types[0] != null ? types[0] : ExtendedType.OBJECT;
			vs = types[1] != null ? types[1] : ExtendedType.OBJECT;
		} else {
			ks = vs = ExtendedType.OBJECT;
		}

		boolean kc = ks.isFinal() || ks.isLeaf();
		boolean vc = vs.isFinal() || vs.isLeaf();

		final MapMeta mm = mapMeta();
		ShapeVisitor key;
		ShapeVisitor val;

		if (mm != null) {
			key = mm.getKey();

			if ((key != null) && !kc) {
				kc = key.complete || key.isLeaf;
			}

			val = mm.getVal();
			if ((val != null) && !vc) {
				vc = val.complete || val.isLeaf;
			}

		} else {
			key = val = null;
		}

		return Pair.of(new Hierarchy(ks, mergeConcreteGenericInfo(ks, key), kc), new Hierarchy(vs, mergeConcreteGenericInfo(vs, val), vc));
	}

	public long objectFieldOffset() {
		long offset = this.offset;

		if (offset == 0) {
			offset = this.offset = Utils.fieldOffset(owner.runtimeType(), name);
		}

		return offset;
	}

	private Set<ExtendedType> overlaidGens() {
		return hierarchy instanceof Generalizations ? ((Generalizations) hierarchy).gens : hierarchy instanceof ShapeVisitor ? ((ShapeVisitor) hierarchy).hierarchy : null;
	}

	public ExtendedType owner() {
		return owner;
	}

	public String readInlineDesc() {
		return String.format("(%s%s)%s", _Context.desc, _Source.desc, desc);
	}

	public String readInlineName() {
		return _GraphSerializer.IC_MULTI_R + name;
	}

	public String specialFieldPrefix() {
		if (isSet()) {
			return _SetSerializer.preffix;
		} else if (isMap()) {
			return _MapSerializer.preffix;
		} else if (isCollection()) {
			return _CollectionSerializer.preffix;
		} else if (isArray()) {
			return _ArraySerializer.pref;
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "FieldInfo [name=" + name + ", desc=" + desc + "]";
	}

	public ExtendedType type() {
		ExtendedType rv = type;

		if (rv == null) {
			rv = type = ExtendedType.forTypeDescriptor(desc, false);
		}

		return rv;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (!visible) {

			switch (desc) {
			case _Meta.NonSerialized.desc:
				access |= ACC_NON_SERIALIZED;
				break;
			case _Meta.Shape.desc:
				final ShapeVisitor s = new ShapeVisitor();
				hierarchy = s;
				return s;
			case _Meta.Collection.desc:
				if (hasSerializer()) {
					Log.warn("@Serialized declaration overrides @Collection.");
					return null;
				}
				return collectionMetaLazy();
			case _Meta.Map.desc:
				if (hasSerializer()) {
					Log.warn("@Serialized declaration overrides @Map.");
					return null;
				}
				return mapMetaLazy();
			case _Meta.Hierarchy.desc:
				AnnotationVisitor rv;
				if (type().isFinal()) {
					Log.warn("Incompatible declaration. Final types (%s::%s) can't have hierarchies.", type().name, name);
					rv = null;
				} else if (hierarchy != null) {
					Log.info("Field %s::%s declaring @ShapeVisitor should declare @Hierarchy as a nested element.", type().name, name);
					rv = null;
				} else {
					hierarchy = rv = new Generalizations();
				}

				return rv;
			case _Meta.NotNull.desc:
				// we can't rely on this annotations. We might want to serialize
				// an invalid object.
				// case _Meta.NotNull.javax_D:
				// case _Meta.NotNull.oval_D:
				access |= ACC_NON_NULL;
				break;
			case _Meta.OnlyPayload.desc:
				access |= ACC_ONLY_DATA;
				break;
			case _Meta.Serialized.desc:
				return new Serialized();
			case _Meta.Compress.desc:
				access |= ACC_COMPRESSED;
				break;
			case _Meta.LeafNode.desc:
				access |= ACC_LEAF;
				break;
			case _Meta.InternalNode.desc:
				access |= ACC_IN;
				break;
			case _Meta.MaybeOpaque.desc:
				access |= ACC_MAYBE_OPAQUE;
				break;
			case _Meta.Optimize.desc:
				ExtendedType type = type();
				if (type.isA(ExtendedType.MAP)) {
					mapMetaLazy().optimize = true;
				} else if (type.isA(ExtendedType.COLLECTION) || type.isArray()) {
					collectionMetaLazy().optimize = true;
				}
				break;
			default:
				break;
			}
		}

		return null;
	}

	@Override
	public void visitEnd() {
		if (hierarchy instanceof ShapeVisitor) {
			final ShapeVisitor s = (ShapeVisitor) hierarchy;
			if (s.nullable) {
				access &= ~ACC_NON_NULL;
			} else {
				access |= ACC_NON_NULL;
			}

			if (s.onlyPayload) {
				access |= ACC_ONLY_DATA;
			} else {
				access &= ~ACC_ONLY_DATA;
			}

			if (s.complete) {
				access |= ACC_COMPLETE_HIERARCHY;
			} else {
				access &= ~ACC_COMPLETE_HIERARCHY;
			}
		} else if (hierarchy instanceof Generalizations) {
			final Generalizations g = (Generalizations) hierarchy;
			if (g.complete) {
				access |= ACC_COMPLETE_HIERARCHY;
			} else {
				access &= ~ACC_COMPLETE_HIERARCHY;
			}
		}
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		AnnotationVisitor rv = super.visitTypeAnnotation(typeRef, typePath, desc, visible);

		if (typePath != null) {
			String s = typePath.toString();

			ShapeVisitor sv = null;

			ExtendedType et = type();

			switch (s) {
			case "0":
				if (et.isA(ExtendedType.COLLECTION)) {
					CollectionMeta meta = collectionMetaLazy();
					sv = meta.shapeVisitor();
				} else if (et.isA(ExtendedType.MAP)) {
					MapMeta meta = mapMetaLazy();
					sv = meta.key();
				}
				break;
			case "1":
				if (et.isA(ExtendedType.MAP)) {
					MapMeta meta = mapMetaLazy();
					sv = meta.val();
				}
			}

			if (sv != null) {
				switch (desc) {
				case _Meta.NotNull.desc:
					sv.nullable = false;
					break;
				case _Meta.OnlyPayload.desc:
					sv.onlyPayload = true;
					break;
				case _Meta.LeafNode.desc:
					sv.complete = true;
					sv.isLeaf = true;
					break;
				case _Meta.Hierarchy.desc:
					rv = sv;
				default:
					break;
				}
			}

		}

		return rv;
	}

	public String writeInlineDesc() {
		return String.format("(%s%s%s)V", _Context.desc, _Sink.desc, desc);
	}

	public String writeInlineName() {
		return _GraphSerializer.IC_MULTI_W + name;
	}
}