public class Main {
	public static void main(String[] args) {
		System.exit(_run(args));
	}

	private static int _run(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java Main <input>");
			return 1;
		}

		String input = args[0];
		boolean isInputFile = input.toLowerCase().endsWith(".vm");

		String asmFilename = isInputFile ?
			input.substring(0, input.length() - 3) + ".asm" :
			input + ".asm";

		Translator translator = new Translator(input, asmFilename);

		if (!translator.run()) {
			return 1;
		}

		return 0;
	}
}
