package com.nc.gs.tests.serializers.suites;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.nc.gs.config.ProvisionService;
import com.nc.gs.core.SerializerFactory;
import com.nc.gs.serializers.java.lang.OpaqueSerializer;
import com.nc.gs.serializers.java.lang.StringSerializer;
import com.nc.gs.serializers.java.lang.compressed.IntegerSerializer;
import com.nc.gs.serializers.java.math.BigIntegerSerializer;

@RunWith(Suite.class)
public class FastBootstrapTestSuite extends GSTestSuite {

	static {
		ProvisionService.builder().skipResourceScan().bootstrap();
	}

	@BeforeClass
	public static void init() {
		SerializerFactory.register(String.class, new StringSerializer(), true);
		SerializerFactory.register(BigInteger.class,
				new BigIntegerSerializer(), false);
		SerializerFactory
				.register(Integer.class, new IntegerSerializer(), true);
		SerializerFactory.register(Integer.class, new IntegerSerializer(),
				false);
		
		SerializerFactory.register(BigDecimal.class,
				new OpaqueSerializer(BigDecimal.class, new HashSet<>(Arrays.asList("intCompact","precision"))), false);
	}

}
