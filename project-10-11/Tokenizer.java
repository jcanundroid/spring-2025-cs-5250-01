import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Tokenizer {
	private enum State {
		DEFAULT,
		LINE_COMMENT,
		BLOCK_COMMENT,
		MAYBE_START_COMMENT,
		MAYBE_END_BLOCK_COMMENT,
		STRING,
		INTEGER,
		IDENTIFIER
	};

	public static ArrayList<Token> tokenize(BufferedReader inputReader) {
		ArrayList<Token> result = new ArrayList<>();

		String inputLine;
		int inputLineNumber = 1;

		State state = State.DEFAULT;

		int markedStartPos = 0;

		while (true) {
			try {
				inputLine = inputReader.readLine();
			}
			catch (IOException e) {
				System.err.println("Error reading from input file");
				return null;
			}

			if (inputLine == null) {
				break;
			}

			for (int charPos = 0; charPos < inputLine.length(); ++charPos) {
				// for convenience, not always valid
				String markedValue = inputLine.substring(markedStartPos, charPos);

				char ch = inputLine.charAt(charPos);

				switch (state) {
					case DEFAULT:
						// reset to avoid problems with substring indices
						markedStartPos = 0;

						if (ch == '/') {
							state = State.MAYBE_START_COMMENT;
						}
						else if (ch == '"') {
							state = State.STRING;
							markedStartPos = charPos + 1;
						}
						else if (Tokenizer._isIntegerChar(ch)) {
							state = State.INTEGER;
							markedStartPos = charPos;
						}
						else if (Tokenizer._isIdentifierChar(ch, true)) {
							state = State.IDENTIFIER;
							markedStartPos = charPos;
						}
						else if (Tokenizer._isSymbol(ch)) {
							result.add(new Token(Token.SYMBOL, ch));
						}
						else if (Tokenizer._isWhitespaceChar(ch)) {
							// nothing to do
						}
						else {
							System.err.printf("Invalid character on line %d\n", inputLineNumber);
							return null;
						}
						break;

					case LINE_COMMENT:
						// nothing to do
						break;

					case BLOCK_COMMENT:
						if (ch == '*') {
							state = State.MAYBE_END_BLOCK_COMMENT;
						}
						break;

					case MAYBE_START_COMMENT:
						if (ch == '/') {
							state = State.LINE_COMMENT;
						}
						else if (ch == '*') {
							state = State.BLOCK_COMMENT;
						}
						else {
							result.add(new Token(Token.SYMBOL, '/'));
							state = State.DEFAULT;
							--charPos;
						}
						break;

					case MAYBE_END_BLOCK_COMMENT:
						if (ch == '/') {
							state = State.DEFAULT;
						}
						else {
							state = State.BLOCK_COMMENT;
						}
						break;

					case STRING:
						if (ch == '"') {
							result.add(new Token(Token.STRING_CONSTANT, markedValue));
							state = State.DEFAULT;
						}
						break;

					case INTEGER:
						if (!Tokenizer._isIntegerChar(ch)) {
							result.add(new Token(Token.INTEGER_CONSTANT, markedValue));
							state = State.DEFAULT;
							--charPos;
						}
						break;

					case IDENTIFIER:
						if (!Tokenizer._isIdentifierChar(ch, false)) {
							String type = Tokenizer._isKeyword(markedValue) ?
								Token.KEYWORD :
								Token.IDENTIFIER;

							result.add(new Token(type, markedValue));
							state = State.DEFAULT;
							--charPos;
						}
						break;
				}
			}

			if (state == State.LINE_COMMENT) {
				state = State.DEFAULT;
			}
			else if (state == State.MAYBE_END_BLOCK_COMMENT) {
				state = State.BLOCK_COMMENT;
			}
			else if (state != State.DEFAULT && state != State.BLOCK_COMMENT) {
				String error = state == State.STRING ?
					"Unterminated string" :
					"Syntax error";

				System.err.printf("%s on line %d\n", error, inputLineNumber);
				return null;
			}

			inputLineNumber++;
		}

		if (state == State.BLOCK_COMMENT) {
			System.err.println("Unterminated block comment at end of file");
			return null;
		}

		return result;
	}

	public static void writeTokens(ArrayList<Token> tokens, PrintWriter outputWriter) {
		outputWriter.println("<tokens>");

		for (Token token : tokens) {
			outputWriter.printf("<%s> %s </%s>\n", token.type, token.getPrintValue(), token.type);
		}

		outputWriter.println("</tokens>");
	}

	private static boolean _isWhitespaceChar(char ch) {
		return ch == '\t' || ch == ' ';
	}

	private static boolean _isIntegerChar(char ch) {
		return ch >= '0' && ch <= '9';
	}

	private static boolean _isIdentifierChar(char ch, boolean isFirst) {
		return
			(ch == '_') ||
			(ch >= 'a' && ch <= 'z') ||
			(ch >= 'A' && ch <= 'Z') ||
			(!isFirst && Tokenizer._isIntegerChar(ch));
	}

	private static boolean _isSymbol(char ch) {
		for (char symbol : Language.SYMBOLS) {
			if (ch == symbol) {
				return true;
			}
		}

		return false;
	}

	private static boolean _isKeyword(String s) {
		for (String keyword : Language.KEYWORDS) {
			if (s.equals(keyword)) {
				return true;
			}
		}

		return false;
	}
}
