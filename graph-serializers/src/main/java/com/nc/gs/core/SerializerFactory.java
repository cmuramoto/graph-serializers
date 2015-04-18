package com.nc.gs.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOError;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.security.CodeSource;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import com.nc.gs.generator.GenerationStrategy;
import com.nc.gs.generator.InstantiatorAdapter;
import com.nc.gs.generator.Reifier;
import com.nc.gs.generator.ShallowCopyAdapter;
import com.nc.gs.generator.opt.MultiCSOptimizer;
import com.nc.gs.generator.opt.MultiMSOptimizer;
import com.nc.gs.generator.opt.SimpleCSOptimizer;
import com.nc.gs.generator.opt.SimpleMSOptmizer;
import com.nc.gs.interpreter.ClassInfo;
import com.nc.gs.interpreter.MapShape;
import com.nc.gs.interpreter.Shape;
import com.nc.gs.interpreter.StreamShape;
import com.nc.gs.log.Log;
import com.nc.gs.serializers.java.lang.ArraySerializer;
import com.nc.gs.serializers.java.lang.LeafTypeArraySerializer;
import com.nc.gs.serializers.java.lang.OpaqueSerializer;
import com.nc.gs.serializers.java.util.CollectionSerializer;
import com.nc.gs.serializers.java.util.EnumSetSerializer;
import com.nc.gs.serializers.java.util.MapSerializer;
import com.nc.gs.serializers.java.util.SetSerializer;
import com.nc.gs.util.Utils;

public final class SerializerFactory {

	public static GraphSerializer compressing(Class<?> c) {
		return COMPRESSING.get(c);
	}

	public static ShallowCopyist copyistOf(Class<?> type) {
		return ShallowCopyAdapter.of(type);
	}

	public static GraphSerializer forArray(StreamShape ss) {
		GraphSerializer rv;

		Shape shape = ss.s;

		Class<?>[] types = shape.hierarchyTypes();
		if (types != null) {
			if (types.length == 1) {
				if (ss.opt) {
					if (shape.hasPolymorphicHierarchy()) {
						rv = MultiCSOptimizer.optmized(ss.optName, null, shape, false);
					} else {
						rv = SimpleCSOptimizer.optmized(ss.optName, null, shape, false);
					}
				} else {
					if (shape.hasPolymorphicHierarchy()) {
						rv = ArraySerializer.forShape(shape);
					} else {
						rv = new LeafTypeArraySerializer(types[0], shape.canBeNull(), shape.disregardRefs());
					}
				}
			} else {
				if (ss.opt) {
					rv = MultiCSOptimizer.optmized(ss.optName, null, shape, false);
				} else {
					rv = ArraySerializer.forShape(shape);
				}
			}
		} else {
			rv = ArraySerializer.forShape(shape);
		}

		return rv;
	}

	@SuppressWarnings("unchecked")
	public static GraphSerializer forCollection(StreamShape ss) {
		GraphSerializer rv;

		Shape shape = ss.s;

		Class<? extends Collection<?>> colType = (Class<? extends Collection<?>>) Utils.nullIfNotConcrete(ss.colType);

		if (ss.opt) {
			if (shape.hasPolymorphicHierarchy()) {
				rv = MultiCSOptimizer.optmized(ss.optName, colType, shape, ss.rep);
			} else {
				rv = SimpleCSOptimizer.optmized(ss.optName, colType, shape, ss.rep);
			}

		} else {
			Class<?> compType = shape.hasPolymorphicHierarchy() ? null : shape.hierarchy().uniqueConcrete();
			if ((colType == null) && (compType == null)) {
				rv = shape.disregardRefs() ? //
						shape.canBeNull() ? CollectionSerializer.NO_REFS : CollectionSerializer.NO_REFS_NON_NULL //
								: shape.canBeNull() ? CollectionSerializer.WITH_REFS : CollectionSerializer.WITH_REFS_NON_NULL;
			} else {
				rv = new CollectionSerializer(colType, compType, shape.canBeNull(), shape.disregardRefs());
			}
		}

		return rv;
	}

	public static GraphSerializer forMap(MapShape ms) {
		GraphSerializer rv;

		String iN = ms.optName;
		@SuppressWarnings("unchecked")
		Class<? extends Map<?, ?>> mt = (Class<? extends Map<?, ?>>) Utils.nullIfNotConcrete(ms.mapType);

		Shape ks = ms.ks;
		Shape vs = ms.vs;

		Class<?>[] kt = ks.hierarchyTypes();
		Class<?>[] vt = vs.hierarchyTypes();

		if ((mt == null) && (kt == null) && (vt == null) && ks.canBeNull() && !ks.disregardRefs() && vs.canBeNull() && !vs.disregardRefs()) {
			rv = MapSerializer.basic();
		} else if (ms.opt) {
			if (!ms.ks.hasPolymorphicHierarchy() && !ms.vs.hasPolymorphicHierarchy()) {
				rv = SimpleMSOptmizer.optimized(iN, mt, ms);
			} else {
				rv = MultiMSOptimizer.optimized(iN, mt, ms);
			}
		} else {
			rv = new MapSerializer(mt, Utils.first(kt), Utils.first(vt), ks.canBeNull(), ks.disregardRefs(), vs.canBeNull(), vs.disregardRefs());
		}

		return rv;
	}

	@SuppressWarnings("unchecked")
	public static GraphSerializer forSet(StreamShape ss) {
		GraphSerializer rv;

		Shape shape = ss.s;
		Class<? extends Collection<?>> colType = (Class<? extends Collection<?>>) Utils.nullIfNotConcrete(ss.colType);

		Class<?>[] types = shape.hierarchyTypes();

		if (ss.opt && !shape.isEnumSet()) {
			if (types.length == 1) {
				rv = SimpleCSOptimizer.optmized(ss.optName, colType, types[0], shape, ss.rep);
			} else {
				rv = MultiCSOptimizer.optmized(ss.optName, colType, types, shape, ss.rep);
			}
		} else {
			if (shape.isEnumSet()) {
				rv = EnumSetSerializer.of(shape.hierarchy().opUniqueConcrete().orElse(null));
			} else {
				if ((colType == null) && ((types == null) || (types.length != 1)) && shape.canBeNull() && !shape.disregardRefs()) {
					rv = SetSerializer.basic();
				} else {
					rv = new SetSerializer((Class<? extends Set<?>>) colType, types[0], shape.canBeNull(), shape.disregardRefs());
				}
			}
		}

		return rv;
	}

	static synchronized GraphSerializer generate(Class<?> c) {
		GraphSerializer rv = SERIALIZERS.get(c);

		if (rv == null) {
			rv = ROOT_LOOKUP.lookup(c);
			if (rv == null) {
				if (STRATEGY.requiresParentFirst()) {
					rv = spinHierarchy(c);
				} else {
					rv = spin(c);
				}
			}
		}

		return rv;
	}

	public static boolean hasSerializer(Class<?> type) {
		return lookup(type) != null;
	}

	public static Instantiator instantiatorOf(Class<?> type) {
		Class<?> c = Utils.nullIfNotConcrete(type);

		Instantiator rv;

		if (c == null) {
			rv = null;
		} else {
			GraphSerializer gs = SERIALIZERS.get(type);

			if (gs instanceof Instantiator) {
				rv = (Instantiator) gs;
			} else {
				rv = InstantiatorAdapter.of(type);
			}
		}

		return rv;
	}

	public static boolean isProbablyOpaque(Class<?> type) {
		return ((type.getClassLoader() != GraphSerializer.class.getClassLoader()) && !Modifier.isPublic(type.getModifiers())) || (lookup(type) instanceof OpaqueSerializer);
	}

	private static void load(File dir) throws Exception {
		File[] files = dir.listFiles();

		for (File file : files) {
			if (file.isDirectory()) {
				load(file);
			} else {
				String name = file.getName().substring(0, file.getName().length() - 4);
				Class<?> c = Class.forName(name);

				GraphSerializer gs = (GraphSerializer) GraphClassLoader.INSTANCE.load(GraphSerializer.class.getClassLoader(), Files.readAllBytes(file.toPath())).newInstance();

				if (!gs.getClass().isSynthetic()) {
					throw new IllegalStateException("Precompiled Serializer must be synthetic");
				}

				if (file.getParent().endsWith("compressed")) {
					COMPRESSING.put(c, gs);
				} else {
					SERIALIZERS.put(c, gs);
				}
			}
		}

	}

	private static void load(ZipInputStream zip) throws Exception {
		byte[] chunk = new byte[16 * 1024];
		ClassLoader cl = GraphClassLoader.class.getClassLoader();
		while (true) {
			ZipEntry e = zip.getNextEntry();

			if (e == null) {
				break;
			}

			if (e.isDirectory()) {
				continue;
			}

			String name = e.getName();

			if (name.contains("precompiled")) {
				int sz = (int) e.getSize();

				if (sz > chunk.length) {
					chunk = new byte[sz];
				}

				int tl = 0;
				while ((tl = zip.read(chunk, tl, sz - tl)) > 0) {
					;
				}

				GraphSerializer gs = (GraphSerializer) GraphClassLoader.INSTANCE.load(cl, chunk).newInstance();

				if (!gs.getClass().isSynthetic()) {
					throw new IllegalStateException("Precompiled Serializer must be synthetic");
				}

				Class<?> c = Class.forName(name.substring(name.lastIndexOf('/') + 1, name.length() - 4));

				if (name.contains("compressed")) {
					COMPRESSING.put(c, gs);
				} else {
					SERIALIZERS.put(c, gs);
				}
			}
		}
	}

	private static void loadPrecompiled(CodeSource cs) {
		URL url = cs.getLocation();

		try (InputStream stream = url.openStream()) {

			if (stream instanceof ByteArrayInputStream) {

				load(new File(url.getFile(), "precompiled"));

			} else if (stream instanceof ZipInputStream) {
				load((ZipInputStream) stream);
			}
		} catch (Throwable e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static GraphSerializer lookup(Class<?> c) {
		return SERIALIZERS.get(c);
	}

	public static GraphSerializer lookup(Type c) {
		try {
			return lookup(Class.forName(c.getClassName(), false, Thread.currentThread().getContextClassLoader()));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static GraphSerializer nullIfNotConcrete(Class<?> c) {
		Class<?> type = Utils.nullIfNotConcrete(c);

		return type == null ? null : serializer(type);
	}

	public static void register(Class<?> type, GraphSerializer gs) {
		GraphSerializer old = SERIALIZERS.put(type, gs);

		if ((old != null) && (old != gs)) {
			Log.info("Replaced %s with %s", old, gs);
		}
	}

	public static void register(Class<?> type, GraphSerializer ser, boolean tryReify) {
		GraphSerializer gs = ser;

		if (tryReify && !ser.getClass().isSynthetic()) {
			try {
				gs = Reifier.reify(type, ser.getClass());
				Log.info("Reified %s!!!", ser.getClass());
			} catch (Throwable e) {
				Log.error(e);
			}
		}

		SERIALIZERS.putIfAbsent(type, gs);
	}

	public static GraphSerializer serializer(Class<?> c) {
		GraphSerializer rv = SERIALIZERS.get(c);

		if (rv == null) {
			GraphSerializer old = SERIALIZERS.putIfAbsent(c, rv = generate(c));
			if (old != null) {
				rv = old;
			}
		}

		return rv;
	}

	@SuppressWarnings({ "all", "restriction" })
	static GraphSerializer spin(Class<?> c) {

		try (InputStream gsIs = Utils.streamCode(GraphSerializer.class)) {
			ClassReader cr = new ClassReader(gsIs);

			ClassWriter cw = STRATEGY.newClassWriter();

			ClassInfo info = ClassInfo.getInfo(c);
			ClassVisitor gca = STRATEGY.generator(info, cw);

			cr.accept(gca, ClassReader.SKIP_DEBUG);

			byte[] bc = cw.toByteArray();

			Utils.writeClass(c.getName(), bc);

			// Utils.writeASM(IO_BASE, c.getName() + ".txt", bc);

			return (GraphSerializer) GraphClassLoader.INSTANCE.load(c.getClassLoader(), bc).newInstance();
			// return (GraphSerializer)
			// GraphSerializer.U.defineAnonymousClass(c, bc,
			// null).newInstance();
		} catch (Exception e) {
			throw new IOError(e);
		}

	}

	private static GraphSerializer spinHierarchy(Class<?> c) {
		GraphSerializer rv = null;
		Class<?> curr = c;
		LinkedList<Class<?>> hierarchy = new LinkedList<>();

		while (curr != Object.class) {
			hierarchy.add(curr);
			curr = curr.getSuperclass();
		}

		while ((curr = hierarchy.pollLast()) != null) {
			GraphSerializer gc = SERIALIZERS.get(curr);

			if (gc == null) {
				gc = spin(curr);
				SERIALIZERS.put(curr, gc);
			}

			if (curr == c) {
				rv = gc;
			}
		}

		return rv;
	}

	private static final ConcurrentHashMap<Class<?>, GraphSerializer> SERIALIZERS;

	private static final IdentityHashMap<Class<?>, GraphSerializer> COMPRESSING;

	private static final GenerationStrategy STRATEGY;

	private static final SerializerLookup ROOT_LOOKUP;

	static {
		SERIALIZERS = new ConcurrentHashMap<>();
		COMPRESSING = new IdentityHashMap<>();
		STRATEGY = GenerationStrategy.FULL_HIERARCHY;
		ROOT_LOOKUP = new SerializerLookup.Basic(0);

		CodeSource cs = GraphSerializer.class.getProtectionDomain().getCodeSource();

		if (cs != null) {
			loadPrecompiled(cs);
		}
	}

}