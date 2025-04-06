import java.util.Arrays;
import java.util.ArrayList;

public class Parser {
	private static ArrayList<String> _segmentNames;

	static {
		_segmentNames = new ArrayList<String>(Arrays.asList(new String[] {
			"argument", "local", "static", "constant",
			"this", "that", "pointer", "temp"
		}));
	}

	public static Command parseLine(String inputLine) {
		Command command = new Command();

		inputLine = inputLine.trim();

		int commentStartIndex = inputLine.indexOf("//");

		if (commentStartIndex > -1) {
			inputLine = inputLine.substring(0, commentStartIndex).trim();
		}

		if (inputLine.length() == 0) {
			command.isEmpty = true;
			return command;
		}

		String[] commandParts = inputLine.split(" ");

		switch (commandParts[0].toLowerCase()) {
			case "add":
			case "sub":
			case "eq":
			case "gt":
			case "lt":
			case "and":
			case "or":
				if (commandParts.length != 1) {
					return null;
				}

				break;

			case "neg":
			case "not":
				if (commandParts.length != 1) {
					return null;
				}

				break;

			case "push":
			case "pop":
				if (commandParts.length != 3) {
					return null;
				}

				if (!_segmentNames.contains(commandParts[1].toLowerCase())) {
					return null;
				}

				command.segmentName = commandParts[1].toLowerCase();

				try {
					command.segmentIndex = Integer.parseUnsignedInt(commandParts[2], 10);
				}
				catch (NumberFormatException e) {
					return null;
				}

				break;

			case "function":
			case "call":
				if (commandParts.length != 3) {
					return null;
				}

				command.functionName = commandParts[1];

				try {
					command.variableOrArgumentCount = Integer.parseInt(commandParts[2], 10);
				}
				catch (NumberFormatException e) {
					return null;
				}

				break;

			case "goto":
			case "if-goto":
			case "label":
				if (commandParts.length != 2) {
					return null;
				}

				command.labelName = commandParts[1];

				break;

			case "return":
				if (commandParts.length != 1) {
					return null;
				}

				break;

			default:
				return null;
		}

		command.opcode = commandParts[0].toLowerCase();

		return command;
	}
}
