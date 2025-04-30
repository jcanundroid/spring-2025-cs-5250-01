import java.util.Arrays;

public class Language {
	public static final String KEYWORD_BOOLEAN = "boolean";
	public static final String KEYWORD_CHAR = "char";
	public static final String KEYWORD_CLASS = "class";
	public static final String KEYWORD_CONSTRUCTOR = "constructor";
	public static final String KEYWORD_DO = "do";
	public static final String KEYWORD_ELSE = "else";
	public static final String KEYWORD_FALSE = "false";
	public static final String KEYWORD_FIELD = "field";
	public static final String KEYWORD_FUNCTION = "function";
	public static final String KEYWORD_IF = "if";
	public static final String KEYWORD_INT = "int";
	public static final String KEYWORD_LET = "let";
	public static final String KEYWORD_METHOD = "method";
	public static final String KEYWORD_NULL = "null";
	public static final String KEYWORD_RETURN = "return";
	public static final String KEYWORD_STATIC = "static";
	public static final String KEYWORD_THIS = "this";
	public static final String KEYWORD_TRUE = "true";
	public static final String KEYWORD_VAR = "var";
	public static final String KEYWORD_VOID = "void";
	public static final String KEYWORD_WHILE = "while";

	public static final char[] SYMBOLS = new char[] {
		'&', '(', ')', '*', '+', ',', '-', '.', '/', ';',
		'<', '=', '>', '[', ']', '{', '|', '}', '~'
	};

	public static final String[] KEYWORDS = new String[] {
		Language.KEYWORD_BOOLEAN,
		Language.KEYWORD_CHAR,
		Language.KEYWORD_CLASS,
		Language.KEYWORD_CONSTRUCTOR,
		Language.KEYWORD_DO,
		Language.KEYWORD_ELSE,
		Language.KEYWORD_FALSE,
		Language.KEYWORD_FIELD,
		Language.KEYWORD_FUNCTION,
		Language.KEYWORD_IF,
		Language.KEYWORD_INT,
		Language.KEYWORD_LET,
		Language.KEYWORD_METHOD,
		Language.KEYWORD_NULL,
		Language.KEYWORD_RETURN,
		Language.KEYWORD_STATIC,
		Language.KEYWORD_THIS,
		Language.KEYWORD_TRUE,
		Language.KEYWORD_VAR,
		Language.KEYWORD_VOID,
		Language.KEYWORD_WHILE
	};

	public static final String[] CLASS_VARIABLE_VISIBILITIES = new String[] {
		Language.KEYWORD_STATIC,
		Language.KEYWORD_FIELD
	};

	public static final String[] CLASS_VARIABLE_TYPES = new String[] {
		Language.KEYWORD_BOOLEAN,
		Language.KEYWORD_CHAR,
		Language.KEYWORD_INT
	};

	public static final String[] CLASS_SUBROUTINE_TYPES = new String[] {
		Language.KEYWORD_CONSTRUCTOR,
		Language.KEYWORD_FUNCTION,
		Language.KEYWORD_METHOD
	};

	public static final String[] CLASS_SUBROUTINE_RETURN_TYPES = new String[] {
		Language.KEYWORD_BOOLEAN,
		Language.KEYWORD_CHAR,
		Language.KEYWORD_INT,
		Language.KEYWORD_VOID
	};

	public static final String[] LOCAL_VARIABLE_VISIBILITIES = new String[] {
		Language.KEYWORD_VAR
	};

	public static final String[] STATEMENT_STARTS = new String[] {
		Language.KEYWORD_LET,
		Language.KEYWORD_IF,
		Language.KEYWORD_WHILE,
		Language.KEYWORD_DO,
		Language.KEYWORD_RETURN
	};

	public static final String[] OPERATORS = new String[] {
		"&", "*", "+", "-", "/",
		"<", "=", ">", "|", "~"
	};

	public static final String[] UNARY_OPERATORS = new String[] {
		"-", "~"
	};

	public static final String[] KEYWORD_CONSTANTS = new String[] {
		Language.KEYWORD_TRUE,
		Language.KEYWORD_FALSE,
		Language.KEYWORD_NULL,
		Language.KEYWORD_THIS
	};

	public static final String[] BUILT_IN_CLASSES = new String[] {
		"Math", "String", "Array", "Output",
		"Screen", "Keyboard", "Memory", "Sys"
	};

	public static boolean isBuiltInClass(String className) {
		return Arrays.asList(Language.BUILT_IN_CLASSES).contains(className);
	}
}
