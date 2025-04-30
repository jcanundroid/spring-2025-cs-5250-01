import java.util.ArrayList;

public class Tokens {
	private ArrayList<Token> _tokens;

	public Tokens(ArrayList<Token> tokens) {
		this._tokens = tokens;
	}

	public Token get() {
		return this._tokens.size() < 1 ? new Token() : this._tokens.remove(0);
	}

	public Token peek() {
		return this._tokens.size() < 1 ? new Token() : this._tokens.get(0);
	}

	public Token peekFurther() {
		return this._tokens.size() < 2 ? new Token() : this._tokens.get(1);
	}
}
