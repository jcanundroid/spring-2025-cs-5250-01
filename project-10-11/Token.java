public class Token {
	public final static String CLASS = "class";
	public final static String CLASS_NAME = "className";
	public final static String CLASS_VARIABLE_DECLARATION = "classVarDec";
	public final static String DO_STATEMENT = "doStatement";
	public final static String EXPRESSION = "expression";
	public final static String EXPRESSION_LIST = "expressionList";
	public final static String IDENTIFIER = "identifier";
	public final static String IF_STATEMENT = "ifStatement";
	public final static String INTEGER_CONSTANT = "integerConstant";
	public final static String KEYWORD_CONSTANT = "keywordConstant";
	public final static String KEYWORD = "keyword";
	public final static String LET_STATEMENT = "letStatement";
	public final static String OPERATOR = "op";
	public final static String PARAMETER_LIST = "parameterList";
	public final static String RETURN_STATEMENT = "returnStatement";
	public final static String STATEMENTS = "statements";
	public final static String STATEMENT = "statement";
	public final static String STRING_CONSTANT = "stringConstant";
	public final static String SUBROUTINE_BODY = "subroutineBody";
	public final static String SUBROUTINE_CALL = "subroutineCall";
	public final static String SUBROUTINE_DECLARATION = "subroutineDec";
	public final static String SUBROUTINE_NAME = "subroutineName";
	public final static String SYMBOL = "symbol";
	public final static String TERM = "term";
	public final static String TYPE = "type";
	public final static String UNARY_OPERATOR = "unaryOp";
	public final static String VARIABLE_DECLARATION = "varDec";
	public final static String VARIABLE_NAME = "varName";
	public final static String WHILE_STATEMENT = "whileStatement";

	public String type;
	public String value;

	public Token() {
		this("", "");
	}

	public Token(String type, char value) {
		this(type, "" + value);
	}

	public Token(String type, String value) {
		this.type = type;
		this.value = value;
	}

	public String getPrintValue() {
		switch (this.value) {
			case "<":
				return "&lt;";
				// break;

			case ">":
				return "&gt;";
				// break;

			case "&":
				return "&amp;";
				// break;

			default:
				return this.value;
		}
	}

	public boolean isType(String[] values) {
		return this.is(Token.KEYWORD, values) ||
		       this.is(Token.IDENTIFIER);
	}

	public boolean is(String type, String[] values) {
		for (String value : values) {
			if (this.is(type, value)) {
				return true;
			}
		}

		return false;
	}

	public boolean is(String type) {
		return this.is(type, (String)null);
	}

	public boolean is(String type, String value) {
		return this.type.equals(type) && (value == null || this.value.equals(value));
	}
}
