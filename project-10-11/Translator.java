import java.io.PrintWriter;

public class Translator {
	private PrintWriter _outputWriter;
	private String _moduleName;
	private int _classStaticIndex;
	private int _classFieldIndex;
	private SymbolTable _classSymbols;
	private SymbolTable _localSymbols;
	private int _tempLabelNumber;

	private static SymbolTable _globalSymbols;

	static {
		_globalSymbols = new SymbolTable();
	}

	public Translator(PrintWriter outputWriter, String moduleName) {
		this._outputWriter = outputWriter;
		this._moduleName = moduleName;
		this._classStaticIndex = 0;
		this._classFieldIndex = 0;
		this._classSymbols = new SymbolTable();
		this._localSymbols = new SymbolTable();
		this._tempLabelNumber = 1;
	}

	public boolean translate(ParsedToken classDefinition) {
		return this._translateClass((ParentToken)classDefinition);
	}

	private boolean _translateClass(ParentToken classDefinition) {
		ParsedToken[] children = classDefinition.getChildren();

		for (int i = 3; i < children.length - 1; ++i) {
			ParentToken declaration = (ParentToken)children[i];

			boolean result = declaration.is(Token.CLASS_VARIABLE_DECLARATION) ?
				this._translateClassVariableDeclaration(declaration) :
				this._translateClassSubroutineDeclaration(declaration);

			if (!result) {
				return false;
			}
		}

		return true;
	}

	private boolean _translateClassVariableDeclaration(ParentToken declaration) {
		ParsedToken[] children = declaration.getChildren();

		String visibility = ((TerminalToken)children[0]).getContent();
		String type = ((TerminalToken)children[1]).getContent();

		for (int i = 2; i < children.length; i += 2) {
			String name = ((TerminalToken)children[i]).getContent();
			boolean result = true;

			if (visibility.equals(Language.KEYWORD_STATIC)) {
				result = this._classSymbols.add(name, type, Symbol.KIND_STATIC, this._classStaticIndex++);
			}
			else {
				result = this._classSymbols.add(name, type, Symbol.KIND_FIELD, this._classFieldIndex++);
			}

			if (result == false) {
				System.err.printf("Class variable %s re-defined\n", name);
				return false;
			}
		}

		return true;
	}

	private boolean _translateClassSubroutineDeclaration(ParentToken declaration) {
		ParsedToken[] children = declaration.getChildren();

		String type = ((TerminalToken)children[0]).getContent();
		String returnType = ((TerminalToken)children[1]).getContent();
		String name = ((TerminalToken)children[2]).getContent();

		if (!Translator._globalSymbols.add(this._moduleName + "." + name, returnType, type, 0)) {
			System.err.printf("Subroutine %s re-defined\n", name);
			return false;
		}

		ParsedToken[] parameterListChildren = ((ParentToken)children[4]).getChildren();

		int argumentIndex = 0;
		int localIndex = 0;

		this._localSymbols.clear();

		this._tempLabelNumber = 1;

		if (type == Language.KEYWORD_METHOD) {
			this._localSymbols.add("this", this._moduleName, Symbol.KIND_ARGUMENT, argumentIndex++);
		}

		for (int i = 0; i < parameterListChildren.length; i += 3) {
			String parameterType = ((TerminalToken)parameterListChildren[i]).getContent();
			String parameterName = ((TerminalToken)parameterListChildren[i + 1]).getContent();

			boolean result = this._localSymbols.add(parameterName, parameterType, Symbol.KIND_ARGUMENT, argumentIndex++);

			if (!result) {
				System.err.printf("Parameter %s re-defined\n", parameterName);
				return false;
			}
		}

		ParsedToken[] subroutineBodyChildren = ((ParentToken)children[6]).getChildren();

		for (int i = 1; i < subroutineBodyChildren.length - 2; ++i) {
			ParentToken variableDeclaration = (ParentToken)subroutineBodyChildren[i];

			int result = this._translateVariableDeclaration(variableDeclaration, localIndex);

			if (result == 0) {
				return false;
			}

			localIndex += result;
		}

		this._outputWriter.printf("function %s.%s %d\n", this._moduleName, name, localIndex);

		if (type.equals(Language.KEYWORD_METHOD)) {
			// set the "this" segment to the first argument
			this._outputWriter.println("\tpush argument 0");
			this._outputWriter.println("\tpop pointer 0");
		}
		else if (type.equals(Language.KEYWORD_CONSTRUCTOR)) {
			// allocate and set "this", which has a size equal to the number of class fields
			this._outputWriter.printf("\tpush constant %d\n", this._classFieldIndex);
			this._outputWriter.printf("\tcall Memory.alloc 1\n");
			this._outputWriter.printf("\tpop pointer 0\n");
		}

		ParsedToken statements = subroutineBodyChildren[subroutineBodyChildren.length - 2];

		if (!this._translateStatements(statements)) {
			return false;
		}

		return true;
	}

	private int _translateVariableDeclaration(ParentToken declaration, int localIndex) {
		ParsedToken[] children = declaration.getChildren();
		int count = 0;

		String type = ((TerminalToken)children[1]).getContent();

		for (int i = 2; i < children.length; i += 2) {
			String name = ((TerminalToken)children[i]).getContent();

			boolean result = this._localSymbols.add(name, type, Symbol.KIND_LOCAL, localIndex + count++);

			if (result == false) {
				System.err.printf("Local variable %s re-defined\n", name);
				return 0;
			}
		}

		return count;
	}

	private boolean _translateStatements(ParsedToken statements) {
		ParsedToken[] children = ((ParentToken)statements).getChildren();

		for (ParsedToken statement : children) {
			ParsedToken[] statementChildren = ((ParentToken)statement).getChildren();
			boolean result = true;

			switch (statement.getTagName()) {
				case Token.LET_STATEMENT:
					result = this._translateLetStatement(statementChildren);
					break;

				case Token.IF_STATEMENT:
					result = this._translateIfStatement(statementChildren);
					break;

				case Token.WHILE_STATEMENT:
					result = this._translateWhileStatement(statementChildren);
					break;

				case Token.DO_STATEMENT:
					result = this._translateDoStatement(statementChildren);
					break;

				case Token.RETURN_STATEMENT:
					result = this._translateReturnStatement(statementChildren);
					break;
			}

			if (!result) {
				return false;
			}
		}

		return true;
	}

	private boolean _translateLetStatement(ParsedToken[] children) {
		String variableName = ((TerminalToken)children[1]).getContent();
		Symbol variable = this._getScopedSymbol(variableName);

		if (variable == null) {
			System.err.printf("Un-defined variable: %s\n", variableName);
			return false;
		}

		if (children.length == 5) {
			// not an array access

			if (!this._translateExpression(children[3])) {
				return false;
			}

			this._pushPopVariable(variable, false);
		}
		else {
			// array access, add variable to evaluated expression

			if (!this._translateExpression(children[6])) {
				return false;
			}

			this._pushPopVariable(variable, true);

			if (!this._translateExpression(children[3])) {
				return false;
			}

			this._outputWriter.println("\tadd");
			this._outputWriter.println("\tpop pointer 1");
			this._outputWriter.println("\tpop that 0");
		}

		return true;
	}

	private boolean _translateIfStatement(ParsedToken[] children) {
		int labelNumber = this._tempLabelNumber++;

		if (!this._translateExpression(children[2])) {
			return false;
		}

		this._outputWriter.printf("\tnot\n");
		this._outputWriter.printf("\tif-goto else_start_%d\n", labelNumber);

		if (!this._translateStatements(children[5])) {
			return false;
		}

		this._outputWriter.printf("\tgoto if_end_%d\n", labelNumber);
		this._outputWriter.printf("label else_start_%d\n", labelNumber);

		if (children.length == 11) {
			// there is an else block
			if (!this._translateStatements(children[9])) {
				return false;
			}
		}

		this._outputWriter.printf("label if_end_%d\n", labelNumber);

		return true;
	}

	private boolean _translateWhileStatement(ParsedToken[] children) {
		int labelNumber = this._tempLabelNumber++;

		this._outputWriter.printf("label while_start_%d\n", labelNumber);

		if (!this._translateExpression(children[2])) {
			return false;
		}

		this._outputWriter.printf("\tnot\n");
		this._outputWriter.printf("\tif-goto while_end_%d\n", labelNumber);

		if (!this._translateStatements(children[5])) {
			return false;
		}

		this._outputWriter.printf("\tgoto while_start_%d\n", labelNumber);
		this._outputWriter.printf("label while_end_%d\n", labelNumber);

		return true;
	}

	private boolean _translateDoStatement(ParsedToken[] children) {
		if (!this._translateSubroutineCall(children[1])) {
			return false;
		}

		// throw away result
		this._outputWriter.println("\tpop temp 0");

		return true;
	}

	private boolean _translateReturnStatement(ParsedToken[] children) {
		if (children.length == 2) {
			// no return value
			this._outputWriter.println("\tpush constant 0");
		}
		else {
			if (!this._translateExpression(children[1])) {
				return false;
			}
		}

		this._outputWriter.println("\treturn");

		return true;
	}

	private boolean _translateExpression(ParsedToken expression) {
		ParsedToken[] children = ((ParentToken)expression).getChildren();

		if (!this._translateTerm(children[0])) {
			return false;
		}

		for (int i = 1; i < children.length; i += 2) {
			ParsedToken term = children[i + 1];
			String operator = ((TerminalToken)children[i]).getContent();

			if (!this._translateTerm(term)) {
				return false;
			}

			switch (operator) {
				case "+":
					this._outputWriter.println("\tadd");
					break;

				case "-":
					this._outputWriter.println("\tsub");
					break;

				case "*":
					this._outputWriter.println("\tcall Math.multiply 2");
					break;

				case "/":
					this._outputWriter.println("\tcall Math.divide 2");
					break;

				case "&amp;":
					this._outputWriter.println("\tand");
					break;

				case "|":
					this._outputWriter.println("\tor");
					break;

				case "&lt;":
					this._outputWriter.println("\tlt");
					break;

				case "&gt;":
					this._outputWriter.println("\tgt");
					break;

				case "=":
					this._outputWriter.println("\teq");
					break;
			}
		}

		return true;
	}

	private boolean _translateTerm(ParsedToken term) {
		ParsedToken[] children = ((ParentToken)term).getChildren();
		ParsedToken firstChild = children[0];

		switch (firstChild.getTagName()) {
			case Token.INTEGER_CONSTANT:
				this._outputWriter.printf("\tpush constant %s\n", ((TerminalToken)firstChild).getContent());
				break;

			case Token.STRING_CONSTANT:
				String text = ((TerminalToken)firstChild).getContent();

				this._outputWriter.printf("\tpush constant %d\n", text.length());
				this._outputWriter.printf("\tcall String.new 1\n");

				for (char ch : text.toCharArray()) {
					this._outputWriter.printf("\tpush constant %d\n", (int)ch);
					this._outputWriter.printf("\tcall String.appendChar 2\n");
				}

				break;

			case Token.KEYWORD:
				String keyword = ((TerminalToken)firstChild).getContent();

				if (keyword.equals(Language.KEYWORD_FALSE) || keyword.equals(Language.KEYWORD_NULL)) {
					this._outputWriter.println("\tpush constant 0");
				}
				else if (keyword.equals(Language.KEYWORD_TRUE)) {
					this._outputWriter.println("\tpush constant 0");
					this._outputWriter.println("\tnot");
				}
				else {
					// this
					this._outputWriter.println("\tpush pointer 0");
				}

				break;

			case Token.SYMBOL:
				String symbol = ((TerminalToken)firstChild).getContent();

				if (symbol.equals("~") || symbol.equals("-")) {
					if (!this._translateTerm(children[1])) {
						return false;
					}

					this._outputWriter.printf("\t%s\n", symbol.equals("~") ? "not" : "neg");
				}
				else if (symbol.equals("(")) {
					if (!this._translateExpression(children[1])) {
						return false;
					}
				}

				break;

			case Token.SUBROUTINE_CALL:
				if (!this._translateSubroutineCall(firstChild)) {
					return false;
				}
				break;

			case Token.IDENTIFIER:
				String variableName = ((TerminalToken)firstChild).getContent();
				Symbol variable = this._getScopedSymbol(variableName);

				if (variable == null) {
					System.err.printf("Un-defined variable: %s\n", variableName);
					return false;
				}

				this._pushPopVariable(variable, true);

				if (children.length > 1) {
					if (!this._translateExpression(children[2])) {
						return false;
					}

					this._outputWriter.println("\tadd");
					this._outputWriter.println("\tpop pointer 1");
					this._outputWriter.println("\tpush that 0");
				}
				break;

			default:
				System.err.printf("Ouch: %s term\n", firstChild.getTagName());
				break;
		}

		return true;
	}

	private boolean _translateSubroutineCall(ParsedToken subroutineCall) {
		String fullSubroutineName = "";
		boolean isMethod = false;
		boolean inCurrentClass = false;
		Symbol variable = null;

		ParsedToken[] children = ((ParentToken)subroutineCall).getChildren();

		if (children.length == 4) {
			// subroutine is in the current class, must be a method

			String subroutineName = ((TerminalToken)children[0]).getContent();

			fullSubroutineName = this._moduleName + "." + subroutineName;
			isMethod = true;
			inCurrentClass = true;
		}
		else {
			// subroutine is in another class

			String identifierName = ((TerminalToken)children[0]).getContent();
			String subroutineName = ((TerminalToken)children[2]).getContent();

			Symbol subroutine = Translator._globalSymbols.get(identifierName + "." + subroutineName);

			if (subroutine == null && !Language.isBuiltInClass(identifierName)) {
				// full name does not correspond to a class function or constructor
				// it must be a method for an object variable; we need its type

				variable = this._getScopedSymbol(identifierName);

				if (variable == null) {
					// subroutine (not method) from user-defined class (hopefully)
					isMethod = false;
					inCurrentClass = false;
				}
				else {
					// change the identifier name to the name of the variable's class
					identifierName = variable.type;

					isMethod = true;
					inCurrentClass = false;
				}
			}
			else {
				// full name corresponds to a class, not allowed to be a method
				isMethod = false;
			}

			fullSubroutineName = identifierName + "." + subroutineName;
		}

		int expressionCount = 0;

		if (isMethod) {
			if (inCurrentClass) {
				// push "this" as "this"
				this._outputWriter.println("\tpush pointer 0");
			}
			else {
				// push the variable as "this"
				this._pushPopVariable(variable, true);
			}

			expressionCount = 1;
		}

		ParsedToken expressionList = children[children.length - 2];
		ParsedToken[] expressionListChildren = ((ParentToken)expressionList).getChildren();

		for (int i = 0; i < expressionListChildren.length; i += 2) {
			if (!this._translateExpression(expressionListChildren[i])) {
				return false;
			}

			++expressionCount;
		}

		this._outputWriter.printf("\tcall %s %d\n", fullSubroutineName, expressionCount);

		return true;
	}

	private void _pushPopVariable(Symbol variable, boolean isPush) {
		String opcode = isPush ? "push" : "pop";

		switch (variable.kind) {
			case Symbol.KIND_STATIC:
				this._outputWriter.printf("\t%s static %d\n", opcode, variable.index);
				break;

			case Symbol.KIND_FIELD:
				this._outputWriter.printf("\t%s this %d\n", opcode, variable.index);
				break;

			case Symbol.KIND_ARGUMENT:
				this._outputWriter.printf("\t%s argument %d\n", opcode, variable.index);
				break;

			case Symbol.KIND_LOCAL:
				this._outputWriter.printf("\t%s local %d\n", opcode, variable.index);
				break;
		}
	}

	private Symbol _getScopedSymbol(String name) {
		Symbol symbol = this._localSymbols.get(name);

		if (symbol == null) {
			return this._classSymbols.get(name);
		}

		return symbol;
	}
}
