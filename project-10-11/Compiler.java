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

public class Compiler {
	private String _input;
	private String _outputFilename;

	public Compiler(String input, String outputFilename) {
		this._input = input;
		this._outputFilename = outputFilename;
	}

	public boolean run() {
		ArrayList<String> inputFilenames = this._getInputFilenames(this._input);

		if (inputFilenames == null) {
			return false;
		}

		return this._compile(inputFilenames);
	}

	private boolean _compile(ArrayList<String> inputFilenames) {
		for (String inputFilename : inputFilenames) {
			if (!this._compileFile(inputFilename)) {
				return false;
			}
		}

		return true;
	}

	private boolean _compileFile(String inputFilename) {
		String inputFilenameBase = inputFilename.substring(0, inputFilename.length() - 5);
		BufferedReader inputReader = this._createReader(inputFilename);
		PrintWriter tokensOutputWriter = this._createWriter(inputFilenameBase + "T.xml");
		PrintWriter parsedOutputWriter = this._createWriter(inputFilenameBase + ".xml");
		PrintWriter outputWriter = this._createWriter(inputFilenameBase + ".vm");

		if (inputReader == null || tokensOutputWriter == null || parsedOutputWriter == null || outputWriter == null) {
			this._closeReaderOrWriter(inputReader);
			this._closeReaderOrWriter(tokensOutputWriter);
			this._closeReaderOrWriter(parsedOutputWriter);
			this._closeReaderOrWriter(outputWriter);

			return false;
		}

		System.err.println(inputFilename);

		String[] inputFilenameParts = inputFilename.split(File.separator);
		String inputFilenameLastPart = inputFilenameParts[inputFilenameParts.length - 1];
		String moduleName = inputFilenameLastPart.substring(0, inputFilenameLastPart.length() - 5);

		boolean result = true;

		ArrayList<Token> tokens = Tokenizer.tokenize(inputReader);

		if (tokens == null) {
			result = false;
		}
		else {
			Tokenizer.writeTokens(tokens, tokensOutputWriter);

			ParsedToken classDefinition = Parser.parse(tokens);

			if (classDefinition == null) {
				System.err.println("Parser error");
				result = false;
			}
			else {
				parsedOutputWriter.println(classDefinition);

				Translator translator = new Translator(outputWriter, moduleName);

				result = translator.translate(classDefinition);

				if (!result) {
					System.err.println("ouch");
				}
			}
		}

		this._closeReaderOrWriter(inputReader);
		this._closeReaderOrWriter(tokensOutputWriter);
		this._closeReaderOrWriter(parsedOutputWriter);
		this._closeReaderOrWriter(outputWriter);

		return result;
	}

	private ArrayList<String> _getInputFilenames(String input) {
		ArrayList<String> filenames = new ArrayList<>();

		if (input.toLowerCase().endsWith(".jack")) {
			filenames.add(input);
			return filenames;
		}

		File folder = new File(input);
		File[] folderItems = folder.listFiles();

		if (folderItems == null) {
			System.err.printf("Input '%s' is neither a .jack file nor a directory of .jack files\n", input);
			return null;
		}

		for (File folderItem : folderItems) {
			if (folderItem.isFile() && folderItem.getName().toLowerCase().endsWith(".jack")) {
				filenames.add(input + File.separator + folderItem.getName());
			}
		}

		if (filenames.size() == 0) {
			System.err.printf("Directory '%s' does not contain any .jack files\n", input);
			return null;
		}

		return filenames;
	}

	private BufferedReader _createReader(String filename) {
		try {
			return new BufferedReader(new FileReader(filename));
		}
		catch (IOException e) {
			System.err.printf("Input file '%s' not found\n", filename);
			return null;
		}
	}

	private PrintWriter _createWriter(String filename) {
		try {
			return new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		}
		catch (IOException e) {
			System.err.printf("Could not create output file '%s'\n", filename);
			return null;
		}
	}

	private void _closeReaderOrWriter(Closeable c) {
		if (c == null) {
			return;
		}

		try {
			c.close();
		}
		catch (IOException e){
		}
	}
}
