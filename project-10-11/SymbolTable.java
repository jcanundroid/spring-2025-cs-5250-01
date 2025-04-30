import java.util.HashMap;

public class SymbolTable {
	private HashMap<String, Symbol> _table;

	public SymbolTable() {
		this._table = new HashMap<>();
	}

	public void clear() {
		this._table.clear();
	}

	public boolean add(String name, String type, String kind, int index) {
		if (this._table.containsKey(name)) {
			return false;
		}

		Symbol symbol = new Symbol(type, kind, index);

		this._table.put(name, symbol);

		return true;
	}

	public Symbol get(String name) {
		return this._table.get(name);
	}

	public String toString() {
		String result = "";

		for (String key : this._table.keySet()) {
			result += String.format("%s: %s\n", key, this._table.get(key).toString());
		}

		return result;
	}
}
