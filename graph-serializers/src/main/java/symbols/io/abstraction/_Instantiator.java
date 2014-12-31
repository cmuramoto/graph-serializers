package symbols.io.abstraction;

import symbols.java.lang._Object;
import symbols.java.util._Comparator;

public interface _Instantiator {

	String name = "com/nc/gc/core/Instantiator";

	String desc = "Lcom/nc/gc/core/Instantiator;";

	String allocate = "allocate";

	String allocateHollow = "allocateHollow";

	String availableMask = "availableMask";

	String copy = "copy";

	String allocate_D = "()" + _Object.desc;

	String allocateI_D = "(I)" + _Object.desc;

	String allocateC_D = "(" + _Comparator.desc + ")" + _Object.desc;

	String allocateIC_D = "(I" + _Comparator.desc + ")" + _Object.desc;

	String allocateCtor_D = "()V";
	String allocateCtorI_D = "(I)V";
	String allocateCtorC_D = "(" + _Comparator.desc + ")V";
	String allocateCtorIC_D = "(I" + _Comparator.desc + ")V";

	String prefix = "I.of.";

	String prefixIN = "I/of/";
}
