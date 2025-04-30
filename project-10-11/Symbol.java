public class Symbol {
	public static final String KIND_STATIC = "static";
	public static final String KIND_FIELD = "field";
	public static final String KIND_ARGUMENT = "argument";
	public static final String KIND_LOCAL = "local";
	public static final String KIND_CONSTRUCTOR = "constructor";
	public static final String KIND_FUNCTION = "function";
	public static final String KIND_METHOD = "method";

	public String type;
	public String kind;
	public int index;

	public Symbol(String type, String kind, int index) {
		this.type = type;
		this.kind = kind;
		this.index = index;
	}

	public String toString() {
		return String.format("(%s, %s, %d)", this.type, this.kind, this.index);
	}
}
