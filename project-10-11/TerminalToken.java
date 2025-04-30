public class TerminalToken extends ParsedToken {
	private String _content;

	public TerminalToken(String tagName, String content) {
		super(tagName);
		this._content = content;
	}

	public TerminalToken(Token token) {
		super(token.type);
		this._content = token.getPrintValue();
	}

	public String getContent() {
		return this._content;
	}

	public String toString(int indentLevel) {
		String prefix = ParsedToken.getIndentPrefix(indentLevel);
		String tagName = this.getTagName();

		return String.format("%s<%s> %s </%s>", prefix, tagName, this._content, tagName);
	}
}
