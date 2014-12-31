package com.nc.gs.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nc.gs.interpreter.VisitationContext;

public abstract class AbstractGSSuite {
	
	@BeforeClass
	public static void initializeContext() {
		vc = VisitationContext.current();
	}

	static VisitationContext vc;

	@AfterClass
	public static void doneContext() {
		vc.close();
	}

}
