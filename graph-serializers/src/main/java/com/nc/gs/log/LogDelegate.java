package com.nc.gs.log;

interface LogDelegate {

	static class StdErrOut implements LogDelegate {
		static String temp = "[%s] %s\n";

		@Override
		public void debug(String msg) {
			System.out.printf(temp, n(getCallerClass()), msg);
		}

		@Override
		public void debug(String msg, Object... args) {
			info(msg, args);
		}

		@Override
		public void error(String msg, Throwable ex) {
			System.err.printf(temp, n(getCallerClass()), msg);

			if (ex != null) {
				ex.printStackTrace(System.err);
			}
		}

		@Override
		public void info(String msg, Object... args) {
			System.out.printf(temp, n(getCallerClass()), String.format(msg, args));
		}

		@Override
		public void warn(String msg, Object... args) {
			System.err.printf(temp, n(getCallerClass()), String.format(msg, args));
		}
	}

	@SuppressWarnings({ "restriction", "deprecation" })
	public static Class<?> getCallerClass() {
		return sun.reflect.Reflection.getCallerClass(4);
	}

	static String n(Class<?> c) {
		return c == null ? "?" : c.getName();
	}

	void debug(String msg);

	void debug(String msg, Object... args);

	void error(String msg, Throwable ex);

	void info(String msg, Object... args);

	void warn(String msg, Object... args);
}