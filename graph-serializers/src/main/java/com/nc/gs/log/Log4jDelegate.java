package com.nc.gs.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerAdapter;

public final class Log4jDelegate implements LogDelegate {

	@SuppressWarnings({ "restriction", "deprecation" })
	static Logger logger() {
		// return LoggerFactory.getLogger(new Exception().getStackTrace()[3].getClassName());
		return LoggerFactory.getLogger(sun.reflect.Reflection.getCallerClass(4));
	}

	static {
		try {
			Class.forName(Log4jLoggerAdapter.class.getName());
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	@Override
	public void debug(String msg) {
		logger().debug(msg);
	}

	@Override
	public void debug(String msg, Object... args) {
		logger().debug(String.format(msg, args));
	}

	@Override
	public void error(String msg, Throwable ex) {
		logger().error(msg, ex);
	}

	@Override
	public void info(String msg, Object... args) {
		logger().info(String.format(msg, args));
	}

	@Override
	public void warn(String msg, Object... args) {
		logger().info(String.format(msg, args));
	}
}
