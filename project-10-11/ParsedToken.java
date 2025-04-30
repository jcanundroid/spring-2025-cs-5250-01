public abstract class ParsedToken {
	private String _tagName;

	public ParsedToken(String tagName) {
		this._tagName = tagName;
	}

	public static String getIndentPrefix(int indentLevel) {
		return new String(new char[indentLevel]).replace("\0", "  ");
	}

	public String getTagName() {
		return this._tagName;
	}

	public boolean is(String tagName) {
		return this._tagName.equals(tagName);
	}

	public String toString() {
		return this.toString(0);
	}

	public abstract String toString(int indentLevel);
}
