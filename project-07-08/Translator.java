import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Translator {
	private String _input;
	private String _outputFilename;
	private String _currentFunctionName;
	private int _tempLabelNumber;

	public Translator(String input, String outputFilename) {
		this._input = input;
		this._outputFilename = outputFilename;
		this._currentFunctionName = "";
		this._tempLabelNumber = 0;
	}

	public boolean run() {
		PrintWriter outputWriter;

		try {
			outputWriter = new PrintWriter(new BufferedWriter(new FileWriter(this._outputFilename)));
		}
		catch (IOException e) {
			System.err.printf("Could not create output file '%s'\n", this._outputFilename);
			return false;
		}

		ArrayList<String> inputFilenames = this._getInputFilenames(this._input);

		boolean result = this._translate(inputFilenames, outputWriter);

		this._closeReaderOrWriter(outputWriter);

		return result;
	}

	private boolean _translate(ArrayList<String> inputFilenames, PrintWriter outputWriter) {
		String sysVMFilename = null;

		for (int i = 0; i < inputFilenames.size(); ++i) {
			if (inputFilenames.get(i).toLowerCase().endsWith("sys.vm")) {
				sysVMFilename = inputFilenames.remove(i);
				inputFilenames.add(0, sysVMFilename);
				break;
			}
		}

		// initialize the VM by setting up the stack and calling Sys.init

		this._writeTranslation(
			Translations.getVMInitialization(),
			outputWriter
		);

		this._writeTranslation(
			Translations.getCall("Sys.init", 0, this._tempLabelNumber++),
			outputWriter
		);

		for (String inputFilename : inputFilenames) {
			if (!this._translateFile(inputFilename, outputWriter)) {
				return false;
			}
		}

		return true;
	}

	private boolean _translateFile(String inputFilename, PrintWriter outputWriter) {
		BufferedReader inputReader;

		try {
			inputReader = new BufferedReader(new FileReader(inputFilename));
		}
		catch (FileNotFoundException e) {
			System.err.printf("Input file '%s' not found\n", inputFilename);
			return false;
		}

		boolean result = true;

		String inputLine;
		int inputLineNumber = 1;

		String[] inputFilenameParts = inputFilename.split(File.separator);
		String inputFilenameLastPart = inputFilenameParts[inputFilenameParts.length - 1];
		String moduleName = inputFilenameLastPart.substring(0, inputFilenameLastPart.length() - 3);

		while (true) {
			try {
				inputLine = inputReader.readLine();
			}
			catch (IOException e) {
				System.err.printf("Error reading from input file '%s'\n", inputFilename);
				result = false;
				break;
			}

			if (inputLine == null) {
				break;
			}

			if (!this._handleLine(inputFilename, inputLineNumber, inputLine, moduleName, outputWriter)) {
				result = false;
				break;
			}

			++inputLineNumber;
		}

		this._closeReaderOrWriter(inputReader);

		return result;
	}

	private boolean _handleLine(String inputFilename, int inputLineNumber, String inputLine, String moduleName, PrintWriter outputWriter) {
		Command command = Parser.parseLine(inputLine);

		if (command == null) {
			System.err.printf("Invalid command on line %d of '%s': %s\n", inputLineNumber, inputFilename, inputLine);
			return false;
		}

		if (command.isEmpty) {
			// nothing to do
			return true;
		}

		outputWriter.printf("// %s (%d): %s\n", inputFilename, inputLineNumber, inputLine.trim());

		switch (command.opcode) {
			case "call":
				this._writeTranslation(
					Translations.getCall(command.functionName, command.variableOrArgumentCount, this._tempLabelNumber++),
					outputWriter
				);
				break;

			case "function":
				this._currentFunctionName = command.functionName;

				this._writeTranslation(
					Translations.getFunction(command.functionName, command.variableOrArgumentCount),
					outputWriter
				);
				break;

			case "return":
				this._writeTranslation(
					Translations.getReturn(),
					outputWriter
				);
				break;

			case "goto":
				this._writeTranslation(
					Translations.getGoto(this._currentFunctionName, command.labelName),
					outputWriter
				);
				break;

			case "if-goto":
				this._writeTranslation(
					Translations.getIfGoto(this._currentFunctionName, command.labelName),
					outputWriter
				);
				break;

			case "label":
				this._writeTranslation(
					Translations.getLabel(this._currentFunctionName, command.labelName),
					outputWriter
				);
				break;

			case "push":
				this._writeTranslation(
					Translations.getPush(moduleName, command.segmentName, command.segmentIndex),
					outputWriter
				);
				break;

			case "pop":
				this._writeTranslation(
					Translations.getPop(moduleName, command.segmentName, command.segmentIndex),
					outputWriter
				);
				break;

			case "add":
			case "sub":
			case "neg":
			case "eq":
			case "gt":
			case "lt":
			case "and":
			case "or":
			case "not":
				this._writeTranslation(
					Translations.getArithmeticLogicOperation(command.opcode, this._tempLabelNumber++),
					outputWriter
				);
				break;
		}

		return true;
	}

	private void _writeTranslation(ArrayList<String> translation, PrintWriter outputWriter) {
		for (String line : translation) {
			outputWriter.println(line);
		}
	}

	private ArrayList<String> _getInputFilenames(String input) {
		ArrayList<String> filenames = new ArrayList<>();

		if (input.toLowerCase().endsWith(".vm")) {
			filenames.add(input);
			return filenames;
		}

		File folder = new File(input);
		File[] folderItems = folder.listFiles();

		if (folderItems == null) {
			System.err.printf("Input '%s' is neither a .vm file nor a directory of .vm files\n", input);
			return null;
		}

		for (File folderItem : folderItems) {
			if (folderItem.isFile() && folderItem.getName().toLowerCase().endsWith(".vm")) {
				filenames.add(input + File.separator + folderItem.getName());
			}
		}

		if (filenames.size() == 0) {
			System.err.printf("Directory '%s' does not contain any .vm files\n", input);
			return null;
		}

		return filenames;
	}

	private void _closeReaderOrWriter(Closeable c) {
		try {
			c.close();
		}
		catch (IOException e){
		}
	}
}
