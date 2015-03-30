package com.nc.gs.tests.serializers.suites;

import org.junit.runners.Suite.SuiteClasses;

import com.nc.gs.tests.AbstractGSSuite;
import com.nc.gs.tests.generator.inheritance.TestCyclicOptimization;
import com.nc.gs.tests.generator.typeannon.TestTypeAnnon;
import com.nc.gs.tests.serializers.cyclic.TreeNodeTest;
import com.nc.gs.tests.serializers.graphs.cols.TestCollectionDeclarations;
import com.nc.gs.tests.serializers.graphs.cols.TestReplacingBean;
import com.nc.gs.tests.serializers.graphs.cols.TestSerializersForOpaqueLogicalTypes;
import com.nc.gs.tests.serializers.graphs.cols.TestTrees;
import com.nc.gs.tests.serializers.graphs.ic.TestICObjects;
import com.nc.gs.tests.serializers.graphs.pvt_types.AssortedTests;
import com.nc.gs.tests.serializers.java.lang.TestArraySerialization;
import com.nc.gs.tests.serializers.java.util.TestSerializersForOpaqueTypes;
import com.nc.gs.tests.serializers.reification.TestArraySerReification;
import com.nc.gs.tests.serializers.reification.TestCollSerReification;
import com.nc.gs.tests.serializers.reification.TestMapReification;

@SuiteClasses({ AssortedTests.class, TestTypeAnnon.class, //
	TestArraySerialization.class, TestCollectionDeclarations.class, //
	TestTrees.class, TestICObjects.class, TestArraySerReification.class,//
	TestCollSerReification.class, TestMapReification.class,//
	TestReplacingBean.class, TestSerializersForOpaqueTypes.class, //
	TestSerializersForOpaqueLogicalTypes.class, TreeNodeTest.class,//
	TestCyclicOptimization.class })
public abstract class GSTestSuite extends AbstractGSSuite {

}
