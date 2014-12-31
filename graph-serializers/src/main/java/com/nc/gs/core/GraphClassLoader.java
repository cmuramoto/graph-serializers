package com.nc.gs.core;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.nc.gs.util.Utils;

public class GraphClassLoader {

	static final Method dc;
	static final Method fc;

	
	static {
		try {
			dc = ClassLoader.class.getDeclaredMethod("defineClass",
					String.class, byte[].class, int.class, int.class);

			dc.setAccessible(true);

			fc = ClassLoader.class.getDeclaredMethod("findClass", String.class);
			fc.setAccessible(true);
		} catch (Throwable e) {

			throw new ExceptionInInitializerError(e);
		}
	}

	public static GraphClassLoader INSTANCE = new GraphClassLoader();
	final ConcurrentHashMap<String, Class<?>> loaded = new ConcurrentHashMap<>();

	GraphClassLoader() {
		super();
	}

	@SuppressWarnings("restriction")
	public Class<?> defineClassOnBootstrapCL(byte[] b) {
		return Utils.U.defineClass(null, b, 0, b.length,
				ClassLoader.getSystemClassLoader(), null);
	}

	public Class<?> findClass(String cn) {
		// Class<?> rv = loaded.get(cn);
		// if (rv == null) {
		// try {
		// rv = super.findClass(cn);
		// } catch (Throwable e) {
		// try {
		// rv = (Class<?>) fc.invoke(Thread.currentThread()
		// .getContextClassLoader(), cn);
		// } catch (Throwable ex) {
		// rv = null;
		// }
		// }
		// }
		// return rv;

		try {
			return Class.forName(cn);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public Class<?> load(ClassLoader cl, byte[] b) {
		// try {
		// return (Class<?>) dc.invoke(cl == null ? Thread.currentThread()
		// .getContextClassLoader() : cl, null, b, 0, b.length);
		// } catch (IllegalAccessException | IllegalArgumentException
		// | InvocationTargetException e) {
		// throw new RuntimeException(e);
		// }
		try {
			return (Class<?>) dc.invoke(
					GraphClassLoader.class.getClassLoader(), null, b, 0,
					b.length);
		} catch (Exception e) {
			return Utils.rethrow(e);
		}
	}

	@SuppressWarnings("restriction")
	public Class<?> loadAnonymous(Class<?> host, byte[] b) {

		return Utils.U.defineAnonymousClass(host, b, null);
	}

}
