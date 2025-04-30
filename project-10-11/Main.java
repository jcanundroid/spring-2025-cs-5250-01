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
		boolean isInputFile = input.toLowerCase().endsWith(".jack");

		String vmFilename = isInputFile ?
			input.substring(0, input.length() - 3) + ".vm" :
			input + ".vm";

		Compiler compiler = new Compiler(input, vmFilename);

		if (!compiler.run()) {
			return 1;
		}

		return 0;
	}
}
