public class Main {
	public static void main(String[] args) {
		System.exit(_run(args));
	}

	private static int _run(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java Main <asm file>");
			return 1;
		}

		String asmFilename = args[0];

		if (!asmFilename.endsWith(".asm")) {
			System.err.println("Filename must end with '.asm'");
			return 1;
		}

		String hackFilename = asmFilename.substring(0, asmFilename.length() - 4) + ".hack";

		Assembler assembler = new Assembler(asmFilename, hackFilename);

		if (!assembler.run()) {
			return 1;
		}

		return 0;
	}
}
