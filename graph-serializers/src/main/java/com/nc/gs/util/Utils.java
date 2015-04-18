package com.nc.gs.util;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.PriorityBlockingQueue;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import sun.misc.Unsafe;
import symbols.io.abstraction._Sink;
import symbols.io.abstraction._Tags.ObjectShape;
import symbols.java.lang._Object;
import symbols.sun.misc._Unsafe;

import com.nc.gs.ds.Partition;
import com.nc.gs.generator.ext.OfflineClassWriter;
import com.nc.gs.interpreter.ExtendedType;
import com.nc.gs.io.Sink;

@SuppressWarnings("restriction")
public class Utils {

	public static String abbrevCN(String name, char sep) {
		String[] tokens = name.split("[\\./]");

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < tokens.length - 1; i++) {
			builder.append(tokens[i].charAt(0)).append(sep);
		}
		builder.append(tokens[tokens.length - 1]);

		return builder.toString();
	}

	public static String abbrevCNs(Class<?>[] types, String sep, String compSep, boolean keepLast, boolean plusWildCard) {
		String rv;
		if (types == null || types.length == 0) {
			rv = "*";
		} else {

			String[] names = new String[types.length];

			for (int i = 0; i < names.length; i++) {
				names[i] = types[i].getName();
			}

			rv = abbrevCNs(names, sep, compSep, keepLast, plusWildCard);
		}

		return rv;
	}

	public static String abbrevCNs(ExtendedType[] types, String sep, String compSep, boolean keepLast, boolean plusWildCard) {
		String rv;
		if (types == null || types.length == 0) {
			rv = "*";
		} else {
			String[] names = new String[types.length];

			for (int i = 0; i < names.length; i++) {
				names[i] = types[i].getInternalName();
			}

			rv = abbrevCNs(names, sep, compSep, keepLast, plusWildCard);
		}

		return rv;
	}

	public static String abbrevCNs(String[] names, String sep, String compSep, boolean keepLast, boolean plusWildCard) {
		StringBuilder builder = new StringBuilder();

		for (int k = 0; k < names.length; k++) {
			String name = names[k];
			String[] tokens = name.split("[\\./]");

			for (int i = 0; i < tokens.length - 1; i++) {
				builder.append(tokens[i].charAt(0)).append(sep);
			}
			builder.append(keepLast ? tokens[tokens.length - 1] : tokens[tokens.length - 1].charAt(0));

			if (k < names.length - 1) {
				builder.append(compSep);
			}
		}

		if (plusWildCard) {
			builder.append(compSep).append("*");
		}

		return builder.toString();
	}

	public static long address(MappedByteBuffer bb) {
		return U.getLong(Objects.requireNonNull(bb), ADDR_OFF);
	}

	@SuppressWarnings("unchecked")
	public static <T> T allocateInstance(Class<T> type) {
		try {
			return (T) U.allocateInstance(type);
		} catch (InstantiationException e) {
			U.throwException(e);
			return null;
		}
	}

	public static long[] allOffsets(Class<?> c) {

		long[] base = new long[0];

		Class<?> curr = c;

		while (curr != Object.class) {
			Field[] fields = c.getClass().getDeclaredFields();

			long[] ls = new long[fields.length];

			int total = 0;

			for (Field field : fields) {
				if (!Modifier.isStatic(field.getModifiers())) {
					ls[total++] = U.objectFieldOffset(field);
				}
			}

			if (total > 0) {
				int cp = base.length;
				base = Arrays.copyOf(base, base.length + total);
				System.arraycopy(ls, 0, base, cp, total);
			}
		}

		Arrays.sort(base);

		return base;
	}

	public static int asInt(boolean b) {
		return b ? 1 : 0;
	}

	private static ClassLoader ccl() {
		return Thread.currentThread().getContextClassLoader();
	}

	public static boolean classExists(String fqn) {
		return findClassWithCorrectName(fqnToResource(fqn)) != null;
	}

	public static final long fieldOffset(Class<?> clazz, String name) {
		try {
			return U.objectFieldOffset(clazz.getDeclaredField(name));
		} catch (NoSuchFieldException | SecurityException e) {
			U.throwException(e);
			return 0;
		}
	}

	public static final long fieldOffset(String cn, String name) {
		try {
			return U.objectFieldOffset(Class.forName(cn).getDeclaredField(name));
		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException e) {
			U.throwException(e);
			return 0;
		}
	}

	private static URL findClassWithCorrectName(String fqn) {
		URL url = ccl().getResource(fqn);

		if (url == null) {
			url = ClassLoader.getSystemClassLoader().getResource(fqn);
		}

		return url;
	}

	public static <T> T first(T[] vals) {
		return vals == null || vals.length == 0 ? null : vals[0];
	}

	public static Class<?> forName(String cn) {
		try {
			return Class.forName(cn);
		} catch (Exception e) {
			return rethrow(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T forNewInstance(String cn) {
		try {
			return (T) forName(cn).newInstance();
		} catch (Exception e) {
			return rethrow(e);
		}
	}

	public static String fqnToResource(String fqn) {
		String n = fqn.replace('.', '/');

		if (!n.endsWith(".class")) {
			n += ".class";
		}

		return n;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T[] getSharedEnumConstants(Class<T> type) {
		T[] rv = (T[]) U.getObject(type, EC_OFF);

		if (rv == null) {
			T[] ec = type.getEnumConstants();
			rv = (T[]) U.getObject(type, EC_OFF);

			if (rv == null) {
				rv = ec;
			}
		}

		return rv;
	}

	public static void guard(Sink dst, Object o) {
		dst.writeVarInt(o == null ? 0 : 0x1);
	}

	public static void guard(Sink dst, Object a, Object b) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2));
	}

	public static void guard(Sink dst, Object a, Object b, Object c) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f, Object g) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20) | (g == null ? 0 : 0x40));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20) | (g == null ? 0 : 0x40) | (h == null ? 0 : 0x80));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20) | (g == null ? 0 : 0x40) | (h == null ? 0 : 0x80) | (i == null ? 0 : 0x100));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20) | (g == null ? 0 : 0x40) | (h == null ? 0 : 0x80) | (i == null ? 0 : 0x100) | (j == null ? 0 : 0x200));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j, Object k) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20) | (g == null ? 0 : 0x40) | (h == null ? 0 : 0x80) | (i == null ? 0 : 0x100) | (j == null ? 0 : 0x200) | (k == null ? 0 : 0x400));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j, Object k, Object l) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20) | (g == null ? 0 : 0x40) | (h == null ? 0 : 0x80) | (i == null ? 0 : 0x100) | (j == null ? 0 : 0x200) | (k == null ? 0 : 0x400) | (l == null ? 0 : 0x800));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j, Object k, Object l, Object m) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20) | (g == null ? 0 : 0x40) | (h == null ? 0 : 0x80) | (i == null ? 0 : 0x100) | (j == null ? 0 : 0x200) | (k == null ? 0 : 0x400) | (l == null ? 0 : 0x800) | (m == null ? 0 : 0x1000));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j, Object k, Object l, Object m, Object n) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20) | (g == null ? 0 : 0x40) | (h == null ? 0 : 0x80) | (i == null ? 0 : 0x100) | (j == null ? 0 : 0x200) | (k == null ? 0 : 0x400) | (l == null ? 0 : 0x800) | (m == null ? 0 : 0x1000) | (n == null ? 0 : 0x2000));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j, Object k, Object l, Object m, Object n, Object o) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20) | (g == null ? 0 : 0x40) | (h == null ? 0 : 0x80) | (i == null ? 0 : 0x100) | (j == null ? 0 : 0x200) | (k == null ? 0 : 0x400) | (l == null ? 0 : 0x800) | (m == null ? 0 : 0x1000) | (n == null ? 0 : 0x2000)
				| (o == null ? 0 : 0x4000));
	}

	public static void guard(Sink dst, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j, Object k, Object l, Object m, Object n, Object o, Object p) {
		dst.writeVarInt((a == null ? 0 : 0x1) | (b == null ? 0 : 0x2) | (c == null ? 0 : 0x4) | (d == null ? 0 : 0x8) | (e == null ? 0 : 0x10) | (f == null ? 0 : 0x20) | (g == null ? 0 : 0x40) | (h == null ? 0 : 0x80) | (i == null ? 0 : 0x100) | (j == null ? 0 : 0x200) | (k == null ? 0 : 0x400) | (l == null ? 0 : 0x800) | (m == null ? 0 : 0x1000) | (n == null ? 0 : 0x2000)
				| (o == null ? 0 : 0x4000) | (p == null ? 0 : 0x8000));
	}

	public static String guardDesc(int size) {
		StringBuilder sb = new StringBuilder(size * _Object.desc.length());
		sb.append('(').append(_Sink.desc);

		for (int i = 0; i < size; i++) {
			sb.append(_Object.desc);
		}

		sb.append(")V");

		return sb.toString();
	}

	public static int nextPowerOfTwo(int mem) {
		return mem == 0 ? 1 : 1 << 32 - Integer.numberOfLeadingZeros(mem);
	}

	public static long nextPowerOfTwo(long mem) {
		return mem == 0 ? 1 : 1L << 64 - Long.numberOfLeadingZeros(mem);
	}

	public static int nextPowerOfTwoInc(int mem) {
		return mem == 0 ? 1 : (mem & mem - 1) == 0 ? mem : 1 << 32 - Integer.numberOfLeadingZeros(mem);
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> nullIfNotConcrete(Class<?> type) {
		if (type != null && (type.isInterface() || Modifier.isAbstract(type.getModifiers()))) {
			return null;
		}

		return (Class<? extends T>) type;
	}

	public static <T> List<List<T>> partition(List<T> c, int size) {
		return new Partition<>(c, size);
	}

	public static Class<?> primitive(char c) {
		Class<?> rv;

		switch (c) {
		case 'Z':
			rv = boolean.class;
			break;
		case 'C':
			rv = char.class;
			break;
		case 'B':
			rv = byte.class;
			break;
		case 'S':
			rv = short.class;
			break;
		case 'I':
			rv = int.class;
			break;
		case 'F':
			rv = float.class;
			break;
		case 'J':
			rv = long.class;
			break;
		case 'D':
			rv = double.class;
			break;
		case 'V':
			rv = void.class;
			break;
		default:
			rv = null;
			break;
		}

		return rv;
	}

	// public static String readString(final ByteBuffer src) {
	// int len = unpackI(src);
	// char[] v = new char[len];
	//
	// src.asCharBuffer().get(v, 0, len);
	//
	// src.position(src.position() + (len << 1));
	//
	// String rv = allocateInstance(String.class);
	// U.putObject(rv, V_OFF, v);
	//
	// return rv;
	// }

	public static Class<?> primitive(String nameOrDesc) {
		if (nameOrDesc.length() > 1) {
			return null;
		}

		return primitive(nameOrDesc.charAt(0));
	}

	public static String readString(final ByteBuffer src) {
		final int charCount = unpackI(src);
		final char[] chars = new char[charCount];
		int c, ix = 0;
		while (ix < charCount) {
			c = src.get() & 0xff;

			switch (c >> 4) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				chars[ix++] = (char) c;
				break;
			case 12:
			case 13:
				chars[ix++] = (char) ((c & 0x1F) << 6 | src.get() & 0x3F);
				break;
			case 14:
				chars[ix++] = (char) ((c & 0x0F) << 12 | (src.get() & 0x3F) << 6 | (src.get() & 0x3F) << 0);
				break;
			default:// checkstyle
				break;
			}

		}

		String rv = allocateInstance(String.class);
		U.putObject(rv, V_OFF, chars);

		// return new String(chars, 0, charCount);
		return rv;
	}

	public static byte[] removeSyntheticModifier(byte[] bc) {
		byte[] rv = bc;
		if (REMOVE_SYNTHETIC) {
			try {
				ClassReader cr = new ClassReader(bc);

				ClassWriter cw = new OfflineClassWriter(0);

				cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {

					@Override
					public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
						super.visit(version, access & ~Opcodes.ACC_SYNTHETIC, name, signature, superName, interfaces);
					}

					@Override
					public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
						return super.visitMethod(access & ~Opcodes.ACC_SYNTHETIC, name, desc, signature, exceptions);
					}

				}, 0);

				rv = cw.toByteArray();
			} catch (Exception e) {
				rv = bc;
			}
		}
		return rv;
	}

	public static <T> T rethrow(Throwable ex) {
		U.throwException(ex);
		// never reached
		return null;
	}

	public static void setAddress(MappedByteBuffer mbb, long addr) {
		U.putLong(Objects.requireNonNull(mbb), ADDR_OFF, addr);
	}

	public static Pair<Comparator<?>, Integer> shape(Object o) {
		Comparator<?> c;
		int k;

		if (o instanceof SortedSet) {
			c = ((SortedSet<?>) o).comparator();
			k = ObjectShape.SET;
		} else if (o instanceof SortedMap) {
			c = ((SortedMap<?, ?>) o).comparator();
			k = ObjectShape.SORTED_MAP;
		} else if (o instanceof PriorityQueue<?>) {
			c = ((PriorityQueue<?>) o).comparator();
			k = ObjectShape.PRIORITY_QUEUE;
		} else if (o instanceof PriorityBlockingQueue) {
			c = ((PriorityBlockingQueue<?>) o).comparator();
			k = ObjectShape.PRIORITY_QUEUE;
		} else {
			c = null;
			k = 0;
		}

		return Pair.<Comparator<?>, Integer> of(c, k);
	}

	public static String simpleName(String name) {
		String rv;

		if (name != null) {
			int ix = name.lastIndexOf('/');

			if (ix < 0) {
				ix = name.lastIndexOf('.');
			}

			if (ix > 0 && ix < name.length() - 1) {
				rv = name.substring(ix, name.length());
			} else {
				rv = name;
			}
		} else {
			rv = name;
		}

		return rv;
	}

	public static InputStream streamAnyCode(String fqn) throws IOException {
		String n = fqnToResource(fqn);

		URL url = ccl().getResource(n);

		if (url == null) {
			url = ClassLoader.getSystemClassLoader().getResource(n);

			if (url == null) {
				throw new IllegalArgumentException("Resource " + fqn + " is neither a classpath resource nor a system resource");
			}
		}

		return url.openStream();
	}

	public static InputStream streamCode(final Class<?> c) {
		final Class<?> enc = c.getEnclosingClass();

		if (enc == null) {
			return c.getResourceAsStream(c.getSimpleName() + ".class");
		}

		String name = c.getName();

		final int lastDot = name.lastIndexOf('.');

		if (lastDot > -1) {
			name = name.substring(lastDot + 1) + ".class";
		} else {
			name = name + ".class";
		}

		return c.getResourceAsStream(name);
	}

	public static InputStream streamCode(final String fqn) {
		String n = fqnToResource(fqn);

		return ccl().getResourceAsStream(n);
	}

	public static InputStream streamSysCode(final String fqn) {
		String n = fqnToResource(fqn);

		URL url = ClassLoader.getSystemClassLoader().getResource(n);

		try {
			return url == null ? null : url.openStream();
		} catch (IOException e) {
			return null;
		}
	}

	public static char unpackC(ByteBuffer src) {
		return src.getChar();
	}

	public static final double unpackD(ByteBuffer src) {
		return src.getDouble();
	}

	public static float unpackF(ByteBuffer src) {
		return Float.intBitsToFloat(unpackI(src));
	}

	public static final int unpackI(ByteBuffer src) {
		int b = src.get();
		int v = b & 0x7F;
		if ((b & 0x80) != 0) {
			b = src.get();
			v |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				b = src.get();
				v |= (b & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					b = src.get();
					v |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						b = src.get();
						v |= (b & 0x7F) << 28;
					}
				}
			}
		}
		return v;
	}

	public static int unpackI(final DataInput in) throws IOException {
		int b = in.readByte();
		int r = b & 0x7F;
		if ((b & 0x80) != 0) {
			b = in.readByte();
			r |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				b = in.readByte();
				r |= (b & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					b = in.readByte();
					r |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						b = in.readByte();
						r |= (b & 0x7F) << 28;
					}
				}
			}
		}
		return r;
	}

	public static final long unpackL(ByteBuffer src) {
		int b = src.get();
		long result = b & 0x7F;
		if ((b & 0x80) != 0) {
			b = src.get();
			result |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				b = src.get();
				result |= (b & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					b = src.get();
					result |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						b = src.get();
						result |= (long) (b & 0x7F) << 28;
						if ((b & 0x80) != 0) {
							b = src.get();
							result |= (long) (b & 0x7F) << 35;
							if ((b & 0x80) != 0) {
								b = src.get();
								result |= (long) (b & 0x7F) << 42;
								if ((b & 0x80) != 0) {
									b = src.get();
									result |= (long) (b & 0x7F) << 49;
									if ((b & 0x80) != 0) {
										b = src.get();
										result |= (long) b << 56;
									}
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

	public static short unpackS(ByteBuffer src) {
		final byte value = src.get();
		if (value == -1) {
			return src.getShort();
		}
		if (value < 0) {
			return (short) (value + 256);
		}

		return value;
	}

	public static boolean unpackZ(ByteBuffer src) {
		return src.get() == 1;
	}

	public static void writeASM(File ioBase, String string, byte[] bc) throws IOException {
		File file = new File(ioBase, string);

		if (file.exists()) {
			file.delete();
		}

		file.createNewFile();

		ClassReader cr = new ClassReader(bc);

		cr.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(file)), 0);
	}

	public static void writeClass(File base, String name, byte[] bc) {
		try {
			String fn = name.endsWith(".class") ? name : name + ".class";
			Files.write(base.toPath().resolve(fn.replace('/', '.')), removeSyntheticModifier(bc), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeClass(String name, byte[] bc) {
		writeClass(IO_BASE, name, bc);
	}

	public static boolean REMOVE_SYNTHETIC;

	public static final sun.misc.Unsafe U;

	static final long EC_OFF;

	public static final File IO_BASE;

	public static final long V_OFF;

	static final long ADDR_OFF;

	static {

		try {
			Field f = sun.misc.Unsafe.class.getDeclaredField(_Unsafe.instance);
			f.setAccessible(true);
			U = (Unsafe) f.get(null);
		} catch (Throwable e) {
			throw new ExceptionInInitializerError(e);
		}

		IO_BASE = new File(System.getProperty("user.home"), "Work/gs");

		if (!IO_BASE.exists()) {
			IO_BASE.mkdirs();
		}

		EC_OFF = fieldOffset(Class.class, "enumConstants");
		V_OFF = fieldOffset(String.class, "value");
		ADDR_OFF = fieldOffset(Buffer.class, "address");
	}

	public boolean isJavaType(String name) {
		return name.startsWith("java") || name.startsWith("sun");
	}
}