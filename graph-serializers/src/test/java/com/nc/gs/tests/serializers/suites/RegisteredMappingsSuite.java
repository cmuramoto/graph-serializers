package com.nc.gs.tests.serializers.suites;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.nc.gs.core.Genesis;

@RunWith(Suite.class)
public class RegisteredMappingsSuite extends GSTestSuite {

	@BeforeClass
	public static void forceInit() throws IOException {

		Genesis.bootstrap();
	}
}