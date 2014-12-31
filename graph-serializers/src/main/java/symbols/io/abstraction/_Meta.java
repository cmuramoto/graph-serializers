package symbols.io.abstraction;

public interface _Meta {

	interface Collection {
		String desc = "Lcom/nc/gs/meta/Collection;";
		String concreteImpl = "concreteImpl";
		String tryInfer = "tryInfer";
		String shape = "shape";
		String optimize = "optimize";
		String implForReplacement = "implForReplacement";
	}

	interface Compress {
		String desc = "Lcom/nc/gs/meta/Compress;";
	}

	interface Fields {
		String desc = "Lcom/nc/gs/meta/Fields;";

		String include = "include";

		String exclude = "exclude";

		String welcomesTransient = "welcomesTransient";

		String welcomesAllNonTransient = "welcomesAllNonTransient";

		String compressByDefault = "compressByDefault";
	}

	interface Hierarchy {

		String desc = "Lcom/nc/gs/meta/Hierarchy;";

		String types = "types";

		String complete = "complete";
	}

	interface InternalNode {
		String desc = "Lcom/nc/gs/meta/InternalNode;";
	}

	interface LeafNode {
		String desc = "Lcom/nc/gs/meta/LeafNode;";
	}

	interface Map {
		String desc = "Lcom/nc/gs/meta/Map;";

		String key = "key";

		String val = "val";

		String concreteImpl = "concreteImpl";

		String tryInfer = "tryInfer";

		String optimize = "optimize";

		String implForReplacement = "implForReplacement";
	}

	interface MaybeOpaque {
		String desc = "Lcom/nc/gs/meta/MaybeOpaque;";
	}

	interface NonSerialized {
		String desc = "Lcom/nc/gs/meta/NonSerialized;";
	}

	interface NotNull {
		String desc = "Lcom/nc/gs/meta/NotNull;";
		String javax_D = "Ljavax/validation/constraints/NotNull;";
		String oval_D = "Lnet/sf/oval/constraint/NotNull;";
	}

	interface OnlyPayload {
		String desc = "Lcom/nc/gs/meta/OnlyPayload;";
	}

	interface Serialized {
		String desc = "Lcom/nc/gs/meta/Serialized;";
	}

	interface Shape {

		String desc = "Lcom/nc/gs/meta/Shape;";

		String hierarchy = "hierarchy";

		String nullable = "nullable";

		String onlyPayload = "onlyPayload";
	}

}