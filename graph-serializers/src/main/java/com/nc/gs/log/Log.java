package com.nc.gs.log;


public final class Log {

	public static void debug(String msg) {
		del.debug(msg);
	}

	public static void debug(String msg, Object... args) {
		del.debug(msg, args);
	}

	public static void error(String msg, Throwable ex) {
		del.error(msg, ex);
	}

	public static void error(Throwable ex) {
		del.error("", ex);
	}

	public static Class<?> getCallerClass() {
		return Log.class;
	}

	public static void info(String msg) {
		del.debug(msg);
	}

	public static void info(String msg, Object... args) {
		del.info(msg, args);
	}

	static String n(Class<?> c) {
		return c == null ? "?" : c.getName();
	}

	public static void warn(String msg, Object... args) {
		del.warn(msg, args);
	}

	static final LogDelegate del;

	static {
		LogDelegate d;
		try {

			d = new Log4jDelegate();

		} catch (Throwable err) {
			d = new LogDelegate.StdErrOut();
		}

		del = d;
	}

}
