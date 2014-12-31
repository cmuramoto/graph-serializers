package com.nc.gs.tests.serializers.suites;

import java.math.BigInteger;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.nc.gs.config.ProvisionService;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.serializers.java.lang.StringSerializer;
import com.nc.gs.serializers.java.lang.compressed.IntegerSerializer;
import com.nc.gs.serializers.java.math.BigIntegerSerializer;

@RunWith(Suite.class)
public class AutomappedTestSuite extends GSTestSuite {

	@BeforeClass
	public static void init() {
		ProvisionService.builder().skipResourceScan().useOnlyAutoMap()
				.bootstrap();

		SerializerFactory.register(String.class, new StringSerializer(), true);
		SerializerFactory.register(BigInteger.class,
				new BigIntegerSerializer(), false);
		SerializerFactory
				.register(Integer.class, new IntegerSerializer(), true);
		SerializerFactory.register(Integer.class, new IntegerSerializer(),
				false);

	}

}
