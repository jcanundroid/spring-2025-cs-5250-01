import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;

public class Parser {
	public static ParsedToken parse(ArrayList<Token> tokens) {
		return Parser._parseClass(new Tokens(tokens));
	}

	private static ParentToken _parseClass(Tokens tokens) {
		Token classKeyword = tokens.get();
		Token identifier = tokens.get();
		Token openBrace = tokens.get();

		if (!classKeyword.is(Token.KEYWORD, Language.KEYWORD_CLASS) ||
		    !identifier.is(Token.IDENTIFIER) ||
		    !openBrace.is(Token.SYMBOL, "{")
		) {
			System.err.println("Expected class declaration");
			return null;
		}

		ArrayList<ParsedToken> children = new ArrayList<>();

		children.add(new TerminalToken(classKeyword));
		children.add(new TerminalToken(identifier));
		children.add(new TerminalToken(openBrace));

		while (true) {
			Token keyword = tokens.peek();

			if (!keyword.is(Token.KEYWORD, Language.CLASS_VARIABLE_VISIBILITIES)) {
				break;
			}

			ParsedToken variable = Parser._parseVariable(tokens, true);

			if (variable == null) {
				return null;
			}

			children.add(variable);
		}

		while (true) {
			Token keyword = tokens.peek();

			if (!keyword.is(Token.KEYWORD, Language.CLASS_SUBROUTINE_TYPES)) {
				break;
			}

			ParsedToken subroutine = Parser._parseSubroutineDeclaration(tokens);

			if (subroutine == null) {
				return null;
			}

			children.add(subroutine);
		}

		Token closeBrace = tokens.get();

		if (!closeBrace.is(Token.SYMBOL, "}")) {
			System.err.println("Expected close brace for end of class");
			return null;
		}

		children.add(new TerminalToken(closeBrace));

		return new ParentToken(Token.CLASS, children);
	}

	private static ParsedToken _parseSubroutineDeclaration(Tokens tokens) {
		Token type = tokens.get();
		Token returnType = tokens.get();
		Token name = tokens.get();
		Token openParenthesis = tokens.get();

		if (!type.is(Token.KEYWORD, Language.CLASS_SUBROUTINE_TYPES) ||
		    !returnType.isType(Language.CLASS_SUBROUTINE_RETURN_TYPES) ||
		    !name.is(Token.IDENTIFIER) ||
		    !openParenthesis.is(Token.SYMBOL, "(")
		) {
			System.err.println("Expected subroutine");
			return null;
		}

		ArrayList<ParsedToken> children = new ArrayList<>();

		children.add(new TerminalToken(type));
		children.add(new TerminalToken(returnType));
		children.add(new TerminalToken(name));
		children.add(new TerminalToken(openParenthesis));

		Token token = tokens.peek();

		if (token.is(Token.SYMBOL, ")")) {
			children.add(new ParentToken(Token.PARAMETER_LIST));
		}
		else {
			ParsedToken parameterList = Parser._parseParameterList(tokens);

			if (parameterList == null) {
				return null;
			}

			children.add(parameterList);
		}

		children.add(new TerminalToken(tokens.get()));

		ParsedToken subroutineBody = Parser._parseSubroutineBody(tokens);

		if (subroutineBody == null) {
			return null;
		}

		children.add(subroutineBody);

		return new ParentToken(Token.SUBROUTINE_DECLARATION, children);
	}

	private static ParsedToken _parseSubroutineBody(Tokens tokens) {
		Token openBrace = tokens.get();

		if (!openBrace.is(Token.SYMBOL, "{")) {
			System.err.println("Expected open brace for start of subroutine body");
			return null;
		}

		ArrayList<ParsedToken> children = new ArrayList<>();

		children.add(new TerminalToken(openBrace));

		while (true) {
			Token keyword = tokens.peek();

			if (!keyword.is(Token.KEYWORD, Language.LOCAL_VARIABLE_VISIBILITIES)) {
				break;
			}

			ParsedToken variable = Parser._parseVariable(tokens, false);

			if (variable == null) {
				return null;
			}

			children.add(variable);
		}

		ParsedToken statements = Parser._parseStatements(tokens);

		if (statements == null) {
			return null;
		}

		children.add(statements);

		Token closeBrace = tokens.get();

		if (!closeBrace.is(Token.SYMBOL, "}")) {
			System.err.println("Expected close brace for end of subroutine body");
			return null;
		}

		children.add(new TerminalToken(closeBrace));

		return new ParentToken(Token.SUBROUTINE_BODY, children);
	}

	private static ParsedToken _parseParameterList(Tokens tokens) {
		Token type = tokens.get();
		Token name = tokens.get();

		if (!type.isType(Language.CLASS_VARIABLE_TYPES) ||
		    !name.is(Token.IDENTIFIER)
		) {
			System.err.println("Expected parameter type and name");
			return null;
		}

		ArrayList<ParsedToken> children = new ArrayList<>();

		children.add(new TerminalToken(type));
		children.add(new TerminalToken(name));

		Token symbol = tokens.peek();

		if (!symbol.is(Token.SYMBOL, ")")) {
			while (true) {
				Token comma = tokens.get();

				type = tokens.get();
				name = tokens.get();

				if (!comma.is(Token.SYMBOL, ",") ||
				    !type.isType(Language.CLASS_VARIABLE_TYPES) ||
				    !name.is(Token.IDENTIFIER)
				) {
					System.err.println("Expected parameter type and name");
					return null;
				}

				children.add(new TerminalToken(comma));
				children.add(new TerminalToken(type));
				children.add(new TerminalToken(name));

				symbol = tokens.peek();

				if (symbol.is(Token.SYMBOL, ")")) {
					break;
				}
			}
		}

		return new ParentToken(Token.PARAMETER_LIST, children);
	}

	private static ParsedToken _parseStatements(Tokens tokens) {
		ArrayList<ParsedToken> children = new ArrayList<>();

		while (true) {
			Token keyword = tokens.peek();

			if (!keyword.is(Token.KEYWORD, Language.STATEMENT_STARTS)) {
				break;
			}

			ParsedToken statement = null;

			switch (keyword.value) {
				case Language.KEYWORD_LET:
					statement = Parser._parseLetStatement(tokens);
					break;

				case Language.KEYWORD_IF:
					statement = Parser._parseIfStatement(tokens);
					break;

				case Language.KEYWORD_WHILE:
					statement = Parser._parseWhileStatement(tokens);
					break;

				case Language.KEYWORD_DO:
					statement = Parser._parseDoStatement(tokens);
					break;

				case Language.KEYWORD_RETURN:
					statement = Parser._parseReturnStatement(tokens);
					break;
			}

			if (statement == null) {
				return null;
			}

			children.add(statement);
		}

		return new ParentToken(Token.STATEMENTS, children);
	}

	private static ParsedToken _parseLetStatement(Tokens tokens) {
		Token keyword = tokens.get();
		Token name = tokens.get();
		Token symbol1 = tokens.get();
		ParsedToken expression = Parser._parseExpression(tokens);
		Token symbol2 = tokens.get();

		boolean symbol1IsOpenBracket = symbol1.is(Token.SYMBOL, "[");
		boolean symbol1IsEquals = symbol1.is(Token.SYMBOL, "=");
		boolean symbol2IsCloseBracket = symbol2.is(Token.SYMBOL, "]");
		boolean symbol2IsSemicolon = symbol2.is(Token.SYMBOL, ";");

		if (!name.is(Token.IDENTIFIER) ||
		    (!symbol1IsOpenBracket && !symbol1IsEquals) ||
		    expression == null ||
		    (!symbol2IsCloseBracket && !symbol2IsSemicolon)
		) {
			System.err.println("Syntax error in let statement");
			return null;
		}

		ArrayList<ParsedToken> children = new ArrayList<>();

		children.add(new TerminalToken(keyword));
		children.add(new TerminalToken(name));
		children.add(new TerminalToken(symbol1));
		children.add(expression);
		children.add(new TerminalToken(symbol2));

		if (symbol1IsOpenBracket) {
			Token symbol3 = tokens.get();
			expression = Parser._parseExpression(tokens);
			Token symbol4 = tokens.get();

			if (!symbol3.is(Token.SYMBOL, "=") ||
			    expression == null ||
			    !symbol4.is(Token.SYMBOL, ";")
			) {
				System.err.println("Syntax error in let statement");
				return null;
			}

			children.add(new TerminalToken(symbol3));
			children.add(expression);
			children.add(new TerminalToken(symbol4));
		}

		return new ParentToken(Token.LET_STATEMENT, children);
	}

	private static ParsedToken _parseIfStatement(Tokens tokens) {
		Token keyword = tokens.get();
		Token openParenthesis = tokens.get();
		ParsedToken expression = Parser._parseExpression(tokens);
		Token closeParenthesis = tokens.get();
		Token openBrace = tokens.get();
		ParsedToken statements = Parser._parseStatements(tokens);
		Token closeBrace = tokens.get();

		if (!openParenthesis.is(Token.SYMBOL, "(") ||
		    expression == null ||
		    !closeParenthesis.is(Token.SYMBOL, ")") ||
		    !openBrace.is(Token.SYMBOL, "{") ||
		    statements == null ||
   		    !closeBrace.is(Token.SYMBOL, "}")
   		) {
   			System.err.println("Syntax error in if statement");
   			return null;
   		}

   		ArrayList<ParsedToken> children = new ArrayList<>(Arrays.asList(new ParsedToken[] {
   			new TerminalToken(keyword),
   			new TerminalToken(openParenthesis),
   			expression,
   			new TerminalToken(closeParenthesis),
   			new TerminalToken(openBrace),
   			statements,
   			new TerminalToken(closeBrace)
   		}));

   		Token token = tokens.peek();

   		if (token.is(Token.KEYWORD, Language.KEYWORD_ELSE)) {
			keyword = tokens.get();
			openBrace = tokens.get();
			statements = Parser._parseStatements(tokens);
			closeBrace = tokens.get();

			if (!openBrace.is(Token.SYMBOL, "{") ||
			    statements == null ||
   			    !closeBrace.is(Token.SYMBOL, "}")
   			) {
   				System.err.println("Syntax error in if-else statement");
   				return null;
   			}

   			children.add(new TerminalToken(keyword));
   			children.add(new TerminalToken(openBrace));
   			children.add(statements);
   			children.add(new TerminalToken(closeBrace));
   		}

		return new ParentToken(Token.IF_STATEMENT, children);
	}

	private static ParsedToken _parseWhileStatement(Tokens tokens) {
		Token keyword = tokens.get();
		Token openParenthesis = tokens.get();
		ParsedToken expression = Parser._parseExpression(tokens);
		Token closeParenthesis = tokens.get();
		Token openBrace = tokens.get();
		ParsedToken statements = Parser._parseStatements(tokens);
		Token closeBrace = tokens.get();

		if (!openParenthesis.is(Token.SYMBOL, "(") ||
		    expression == null ||
		    !closeParenthesis.is(Token.SYMBOL, ")") ||
		    !openBrace.is(Token.SYMBOL, "{") ||
		    statements == null ||
   		    !closeBrace.is(Token.SYMBOL, "}")
   		) {
   			System.err.println("Syntax error in while statement");
   			return null;
   		}

   		ArrayList<ParsedToken> children = new ArrayList<>(Arrays.asList(new ParsedToken[] {
   			new TerminalToken(keyword),
   			new TerminalToken(openParenthesis),
   			expression,
   			new TerminalToken(closeParenthesis),
   			new TerminalToken(openBrace),
   			statements,
   			new TerminalToken(closeBrace)
   		}));

		return new ParentToken(Token.WHILE_STATEMENT, children);
	}

	private static ParsedToken _parseDoStatement(Tokens tokens) {
		Token keyword = tokens.get();
		ParsedToken subroutineCall = Parser._parseSubroutineCall(tokens);
		Token semicolon = tokens.get();

		if (subroutineCall == null ||
			!semicolon.is(Token.SYMBOL, ";")
		) {
			System.err.println("Syntax error in do statement");
			return null;
		}

		ArrayList<ParsedToken> children = new ArrayList<>(Arrays.asList(new ParsedToken[] {
			new TerminalToken(keyword),
			subroutineCall,
			new TerminalToken(semicolon)
		}));

		return new ParentToken(Token.DO_STATEMENT, children);
	}

	private static ParsedToken _parseReturnStatement(Tokens tokens) {
		Token keyword = tokens.get();
		Token token = tokens.peek();

		ArrayList<ParsedToken> children = new ArrayList<>();

		children.add(new TerminalToken(keyword));

		if (token.is(Token.SYMBOL, ";")) {
			children.add(new TerminalToken(tokens.get()));
		}
		else {
			ParsedToken expression = Parser._parseExpression(tokens);
			Token semicolon = tokens.get();

			if (expression == null ||
			    !semicolon.is(Token.SYMBOL, ";")
			) {
				System.err.println("Syntax error in return statement");
				return null;
			}

			children.add(expression);
			children.add(new TerminalToken(semicolon));
		}

		return new ParentToken(Token.RETURN_STATEMENT, children);
	}

	private static ParsedToken _parseExpression(Tokens tokens) {
		ParsedToken term = Parser._parseTerm(tokens);

		if (term == null) {
			System.err.println("Syntax error in expression");
			return null;
		}

		ArrayList<ParsedToken> children = new ArrayList<>();

		children.add(term);

		while (true) {
			Token token = tokens.peek();

			if (!token.is(Token.SYMBOL, Language.OPERATORS)) {
				break;
			}

			Token operator = tokens.get();
			term = Parser._parseTerm(tokens);

			if (term == null) {
				System.err.println("Syntax error in expression");
				return null;
			}

			children.add(new TerminalToken(operator));
			children.add(term);
		}

		return new ParentToken(Token.EXPRESSION, children);
	}

	private static ParsedToken _parseExpressionList(Tokens tokens) {
		ParsedToken expression = Parser._parseExpression(tokens);

		if (expression == null) {
			System.err.println("Syntax error in expression list");
			return null;
		}

		ArrayList<ParsedToken> children = new ArrayList<>();

		children.add(expression);

		while (true) {
			Token token = tokens.peek();

			if (!token.is(Token.SYMBOL, ",")) {
				break;
			}

			Token comma = tokens.get();
			expression = Parser._parseExpression(tokens);

			if (expression == null) {
				System.err.println("Syntax error in expression list");
				return null;
			}

			children.add(new TerminalToken(comma));
			children.add(expression);
		}

		return new ParentToken(Token.EXPRESSION_LIST, children);
	}

	private static ParsedToken _parseTerm(Tokens tokens) {
		Token token = tokens.peek();

		ArrayList<ParsedToken> children = new ArrayList<>();

		if (token.is(Token.INTEGER_CONSTANT) ||
		    token.is(Token.STRING_CONSTANT) ||
		    token.is(Token.KEYWORD, Language.KEYWORD_CONSTANTS)
		) {
			children.add(new TerminalToken(tokens.get()));
		}
		else if (token.is(Token.SYMBOL, "(")) {
			Token openParenthesis = tokens.get();
			ParsedToken expression = Parser._parseExpression(tokens);
			Token closeParenthesis = tokens.get();

			if (expression == null ||
			    !closeParenthesis.is(Token.SYMBOL, ")")
			) {
				System.err.println("Syntax error in expression-type term");
				return null;
			}

			children.add(new TerminalToken(openParenthesis));
			children.add(expression);
			children.add(new TerminalToken(closeParenthesis));
		}
		else if (token.is(Token.SYMBOL, Language.UNARY_OPERATORS)) {
			Token unaryOperator = tokens.get();
			ParsedToken term = Parser._parseTerm(tokens);

			if (term == null) {
				System.err.println("Syntax error in sub-term");
				return null;
			}

			children.add(new TerminalToken(unaryOperator));
			children.add(term);
		}
		else if (token.is(Token.IDENTIFIER)) {
			token = tokens.peekFurther();

			if (token.is(Token.SYMBOL, "(") ||
			    token.is(Token.SYMBOL, ".")
			) {
				ParsedToken subroutineCall = Parser._parseSubroutineCall(tokens);

				if (subroutineCall == null) {
					System.err.println("Syntax error in subroutine-call-type term");
					return null;
				}

				children.add(subroutineCall);
			}
			else if (token.is(Token.SYMBOL, "[")) {
				Token identifier = tokens.get();
				Token openBracket = tokens.get();
				ParsedToken expression = Parser._parseExpression(tokens);
				Token closeBracket = tokens.get();

				if (expression == null ||
				    !closeBracket.is(Token.SYMBOL, "]")
				) {
					System.err.println("Syntax error in array-index-type term");
					return null;
				}

				children.add(new TerminalToken(identifier));
				children.add(new TerminalToken(openBracket));
				children.add(expression);
				children.add(new TerminalToken(closeBracket));
			}
			else {
				children.add(new TerminalToken(tokens.get()));
			}
		}
		else {
			System.err.println("Syntax error in term");
			return null;
		}

		return new ParentToken(Token.TERM, children);
	}

	private static ParsedToken _parseSubroutineCall(Tokens tokens) {
		Token identifier = tokens.get();
		Token symbol = tokens.get();

		if (!identifier.is(Token.IDENTIFIER)) {
			System.err.println("Expected identifier for subroutine call");
			return null;
		}

		ArrayList<ParsedToken> children = new ArrayList<>();

		children.add(new TerminalToken(identifier));

		if (symbol.is(Token.SYMBOL, "(")) {
			children.add(new TerminalToken(symbol));
		}
		else if (symbol.is(Token.SYMBOL, ".")) {
			Token subroutineName = tokens.get();
			Token openParenthesis = tokens.get();

			if (!subroutineName.is(Token.IDENTIFIER) ||
			    !openParenthesis.is(Token.SYMBOL, "(")
			) {
				System.err.println("Syntax error in subroutine call");
				return null;
			}

			children.add(new TerminalToken(symbol));
			children.add(new TerminalToken(subroutineName));
			children.add(new TerminalToken(openParenthesis));
		}
		else  {
			System.err.println("Syntax error in subroutine call");
			return null;
		}

		Token token = tokens.peek();

		if (token.is(Token.SYMBOL, ")")) {
			children.add(new ParentToken(Token.EXPRESSION_LIST));
			children.add(new TerminalToken(tokens.get()));
		}
		else {
			ParsedToken expressionList = Parser._parseExpressionList(tokens);
			Token closeParenthesis = tokens.get();

			if (expressionList == null ||
			    !closeParenthesis.is(Token.SYMBOL, ")")
			) {
				System.err.println("Syntax error in expression list of subroutine call");
				return null;
			}

			children.add(expressionList);
			children.add(new TerminalToken(closeParenthesis));
		}

		return new ParentToken(Token.SUBROUTINE_CALL, children, false);
	}

	private static ParsedToken _parseVariable(Tokens tokens, boolean forClass) {
		String[] visibilities = forClass ?
			Language.CLASS_VARIABLE_VISIBILITIES :
			Language.LOCAL_VARIABLE_VISIBILITIES;

		String parentTokenType = forClass ?
			Token.CLASS_VARIABLE_DECLARATION :
			Token.VARIABLE_DECLARATION;

		Token visibility = tokens.get();
		Token type = tokens.get();
		Token name = tokens.get();

		if (!visibility.is(Token.KEYWORD, visibilities) ||
			!type.isType(Language.CLASS_VARIABLE_TYPES) ||
			!name.is(Token.IDENTIFIER)
		) {
			System.err.println("Expected class variable");
			return null;
		}

		ArrayList<ParsedToken> children = new ArrayList<>();

		children.add(new TerminalToken(visibility));
		children.add(new TerminalToken(type));
		children.add(new TerminalToken(name));

		while (true) {
			Token token = tokens.get();

			if (token.is(Token.SYMBOL, ";")) {
				children.add(new TerminalToken(token));
				break;
			}

			if (!token.is(Token.SYMBOL, ",")) {
				System.err.println("Expected comma in class variable list");
				return null;
			}

			children.add(new TerminalToken(token));

			token = tokens.get();

			if (!token.is(Token.IDENTIFIER)) {
				System.err.println("Expected identifier in class variable list");
				return null;
			}

			children.add(new TerminalToken(token));
		}

		return new ParentToken(parentTokenType, children);
	}
}
