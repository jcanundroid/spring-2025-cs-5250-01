import java.util.Arrays;
import java.util.ArrayList;

public class ParentToken extends ParsedToken {
	private ArrayList<ParsedToken> _children;
	private boolean _shouldPrintParentTags;

	public ParentToken(String tagName) {
		this(tagName, new ArrayList<ParsedToken>(), true);
	}

	public ParentToken(String tagName, ArrayList<ParsedToken> children) {
		this(tagName, children, true);
	}

	public ParsedToken[] getChildren() {
		return this._children.toArray(new ParsedToken[this._children.size()]);
	}

	public ParentToken(String tagName, ArrayList<ParsedToken> children, boolean shouldPrintParentTags) {
		super(tagName);
		this._children = children;
		this._shouldPrintParentTags = shouldPrintParentTags;
	}

	public String toString(int indentLevel) {
		String prefix = ParsedToken.getIndentPrefix(indentLevel);
		String tagName = this.getTagName();

		String result = "";

		if (this._shouldPrintParentTags) {
			result = String.format("%s<%s>", prefix, tagName);
			++indentLevel;
		}

		for (ParsedToken child : this._children) {
			result += String.format("\n%s", child.toString(indentLevel));
		}

		if (this._shouldPrintParentTags) {
			result += String.format("\n%s</%s>", prefix, tagName);
		}
		else {
			result = result.substring(1);
		}

		return result;
	}
}
