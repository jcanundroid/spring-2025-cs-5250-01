import java.util.HashMap;

public class Command {
	public String type;
	public String commentText;
	public String labelName;
	public int addressValue;
	public String addressVariableName;
	public String comp;
	public String dest;
	public String jump;

	private static HashMap<String, String> _compMap;
	private static HashMap<String, String> _destMap;
	private static HashMap<String, String> _jumpMap;

	static {
		_compMap = new HashMap<>();
		_destMap = new HashMap<>();
		_jumpMap = new HashMap<>();

		_compMap.put("0",   "0101010");
		_compMap.put("1",   "0111111");
		_compMap.put("-1",  "0111010");
		_compMap.put("D",   "0001100");
		_compMap.put("A",   "0110000");
		_compMap.put("!D",  "0001101");
		_compMap.put("!A",  "0110001");
		_compMap.put("-D",  "0001111");
		_compMap.put("-A",  "0110011");
		_compMap.put("D+1", "0011111");
		_compMap.put("A+1", "0110111");
		_compMap.put("D-1", "0001110");
		_compMap.put("A-1", "0110010");
		_compMap.put("D+A", "0000010");
		_compMap.put("D-A", "0010011");
		_compMap.put("A-D", "0000111");
		_compMap.put("D&A", "0000000");
		_compMap.put("D|A", "0010101");
		_compMap.put("M",   "1110000");
		_compMap.put("!M",  "1110001");
		_compMap.put("-M",  "1110011");
		_compMap.put("M+1", "1110111");
		_compMap.put("M-1", "1110010");
		_compMap.put("D+M", "1000010");
		_compMap.put("D-M", "1010011");
		_compMap.put("M-D", "1000111");
		_compMap.put("D&M", "1000000");
		_compMap.put("D|M", "1010101");

		_destMap.put("null", "000");
		_destMap.put("M",    "001");
		_destMap.put("D",    "010");
		_destMap.put("MD",   "011");
		_destMap.put("A",    "100");
		_destMap.put("AM",   "101");
		_destMap.put("AD",   "110");
		_destMap.put("AMD",  "111");

		_jumpMap.put("null", "000");
		_jumpMap.put("JGT",  "001");
		_jumpMap.put("JEQ",  "010");
		_jumpMap.put("JGE",  "011");
		_jumpMap.put("JLT",  "100");
		_jumpMap.put("JNE",  "101");
		_jumpMap.put("JLE",  "110");
		_jumpMap.put("JMP",  "111");
	}

	public static String getCompEncoding(String comp) {
		return _compMap.get(comp);
	}

	public static String getDestEncoding(String dest) {
		return _destMap.get(dest);
	}

	public static String getJumpEncoding(String jump) {
		return _jumpMap.get(jump);
	}

	public static String encodeAddress(int address) {
		return String.format("%15s", Integer.toBinaryString(address)).replace(" ", "0");
	}
}
