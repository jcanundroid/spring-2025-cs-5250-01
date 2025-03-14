public class Parser {
	public static Command parseLine(String inputLine) {
		if (inputLine.length() < 2) {
			return null;
		}

		Command command = new Command();

		if (inputLine.startsWith("//")) {
			command.type = "comment";
			command.commentText = inputLine.substring(2);
		}
		else if (inputLine.startsWith("(") && inputLine.endsWith(")")) {
			command.type = "label";
			command.labelName = inputLine.substring(1, inputLine.length() - 1);
		}
		else if (inputLine.startsWith("@")) {
			String address = inputLine.substring(1);

			command.type = "aInstruction";

			if (address.matches("^[0-9]+$")) {
				command.addressValue = Integer.parseInt(address);
			}
			else {
				command.addressVariableName = address;
			}
		}
		else {
			if (!_parseCInstructionLine(command, inputLine)) {
				return null;
			}
		}

		return command;
	}

	private static boolean _parseCInstructionLine(Command command, String inputLine) {
		boolean containsEqualSign = inputLine.contains("=");
		boolean containsSemicolon = inputLine.contains(";");

		if (containsEqualSign && containsSemicolon) {
			return false;
		}
		else if (!containsEqualSign && !containsSemicolon) {
			return false;
		}

		if (containsEqualSign) {
			String[] parts = inputLine.split("=", 2);

			command.dest = Command.getDestEncoding(parts[0]);
			command.comp = Command.getCompEncoding(parts[1]);
			command.jump = "000";

			if (command.dest == null || command.comp == null) {
				return false;
			}
		}
		else {
			String[] parts = inputLine.split(";", 2);

			command.dest = "000";
			command.comp = Command.getCompEncoding(parts[0]);
			command.jump = Command.getJumpEncoding(parts[1]);

			if (command.comp == null || command.jump == null) {
				return false;
			}
		}

		command.type = "cInstruction";

		return true;
	}
}
