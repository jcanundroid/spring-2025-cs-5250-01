import java.util.HashMap;

public class SymbolTable {
	private HashMap<String, Integer> _table;
	private int _variableAddress;

	public SymbolTable() {
		this._table = new HashMap<>();
		this._variableAddress = 16;

		this._table.put("SP", 0x0000);
		this._table.put("LCL", 0x0001);
		this._table.put("ARG", 0x0002);
		this._table.put("THIS", 0x0003);
		this._table.put("THAT", 0x0004);
		this._table.put("SCREEN", 0x4000);
		this._table.put("KBD", 0x6000);

		for (Integer i = 0; i < 16; ++i) {
			this._table.put("R" + i.toString(), 0x0000 + i);
		}
	}

	public int getSymbol(String name) {
		Integer value = this._table.get(name);
		return value == null ? -1 : value.intValue();
	}

	public void addLabel(String name, int address) {
		this._addSymbol(name, address);
	}

	public void addVariable(String name) {
		this._addSymbol(name, this._variableAddress++);
	}

	private void _addSymbol(String name, int address) {
		this._table.put(name, address);
	}
}
