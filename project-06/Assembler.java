import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Assembler {
	private String _inputFilename;
	private String _outputFilename;

	public Assembler(String inputFilename, String outputFilename) {
		this._inputFilename = inputFilename;
		this._outputFilename = outputFilename;
	}

	public boolean run() {
		BufferedReader inputReader;
		PrintWriter outputWriter;

		try {
			inputReader = new BufferedReader(new FileReader(this._inputFilename));
		}
		catch (FileNotFoundException e) {
			System.err.printf("Input file '%s' not found\n", this._inputFilename);
			return false;
		}

		try {
			outputWriter = new PrintWriter(new BufferedWriter(new FileWriter(this._outputFilename)));
		}
		catch (IOException e) {
			System.err.printf("Could not create output file '%s'\n", this._outputFilename);
			this._closeReaderOrWriter(inputReader);
			return false;
		}

		boolean result = this._assemble(inputReader, outputWriter);

		this._closeReaderOrWriter(inputReader);
		this._closeReaderOrWriter(outputWriter);

		return result;
	}

	private boolean _assemble(BufferedReader inputReader, PrintWriter outputWriter) {
		ArrayList<Command> commands = new ArrayList<>();
		SymbolTable symbolTable = new SymbolTable();

		if (!this._assembleStage1(inputReader, commands, symbolTable)) {
			return false;
		}

		return this._assembleStage2(outputWriter, commands, symbolTable);
	}

	private boolean _assembleStage1(BufferedReader inputReader, ArrayList<Command> commands, SymbolTable symbolTable) {
		String inputLine = "";
		int inputLineNumber = 1;
		int instructionAddress = 0;

		while (true) {
			try {
				inputLine = inputReader.readLine();
			}
			catch (IOException e) {
				System.err.println("Error reading from input file");
				return false;
			}

			if (inputLine == null) {
				break;
			}

			++inputLineNumber;

			inputLine = inputLine.trim();

			if (inputLine.length() == 0) {
				continue;
			}

			Command command = Parser.parseLine(inputLine);

			if (command == null) {
				System.err.printf("Invalid command on line %d: %s\n", inputLineNumber, inputLine);
				return false;
			}

			if (command.type == "comment") {
				// nothing to do
			}
			else if (command.type == "label") {
				symbolTable.addLabel(command.labelName, instructionAddress);
			}
			else {
				commands.add(command);
				++instructionAddress;
			}
		}

		return true;
	}

	private boolean _assembleStage2(PrintWriter outputWriter, ArrayList<Command> commands, SymbolTable symbolTable) {
		for (Command command : commands) {
			if (command.type == "aInstruction" && command.addressVariableName != null) {
				int resolvedAddress = symbolTable.getSymbol(command.addressVariableName);

				if (resolvedAddress > -1) {
					command.addressValue = resolvedAddress;
				}
				else {
					symbolTable.addVariable(command.addressVariableName);
					command.addressValue = symbolTable.getSymbol(command.addressVariableName);
				}
			}

			if (command.type == "cInstruction") {
				outputWriter.printf("111%s%s%s\n", command.comp, command.dest, command.jump);
			}
			else {
				outputWriter.printf("0%s\n", Command.encodeAddress(command.addressValue));
			}
		}

		return true;
	}

	private void _closeReaderOrWriter(Closeable c) {
		try {
			c.close();
		}
		catch (IOException e){
		}
	}
}
