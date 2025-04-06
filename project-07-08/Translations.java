import java.util.Arrays;
import java.util.ArrayList;

public class Translations {
	private static final String[] _VM_INITIALIZATION;
	private static final String[] _IF_GOTO_START;
	private static final String[] _CALL_PUSHES;
	private static final String[] _FUNCTION_PUSHES_START;
	private static final String[] _FUNCTION_PUSH;
	private static final String[] _RETURN;
	private static final String[] _PUSH_END;
	private static final String[] _POP_START;
	private static final String[] _ARITHMETIC_LOGIC_OPERATION_UNARY_START;
	private static final String[] _ARITHMETIC_LOGIC_OPERATION_BINARY_START;
	private static final String[] _ARITHMETIC_LOGIC_OPERATION_BINARY_END;

	static {
		_VM_INITIALIZATION = new String[] {
			// set SP to 256
			"@256",
			"D=A",
			"@SP",
			"M=D"
		};

		_CALL_PUSHES = new String[] {
			// the return address is in A
			// store it at SP + 0
			"D=A",
			"@SP",
			"A=M",
			"M=D",
			// store LCL at SP + 1
			"D=A",
			"@1",
			"D=D+A",
			"@R13",
			"M=D",
			"@LCL",
			"D=M",
			"@R13",
			"A=M",
			"M=D",
			// store ARG at SP + 2
			"D=A",
			"@1",
			"D=D+A",
			"@R13",
			"M=D",
			"@ARG",
			"D=M",
			"@R13",
			"A=M",
			"M=D",
			// store THIS at SP + 3
			"D=A",
			"@1",
			"D=D+A",
			"@R13",
			"M=D",
			"@THIS",
			"D=M",
			"@R13",
			"A=M",
			"M=D",
			// store THAT at SP + 4
			"D=A",
			"@1",
			"D=D+A",
			"@R13",
			"M=D",
			"@THAT",
			"D=M",
			"@R13",
			"A=M",
			"M=D",
			// set LCL = SP + 5 (instead of LC = SP since SP has not been updated yet)
			"@SP",
			"D=M",
			"@5",
			"D=D+A",
			"@LCL",
			"M=D",
			// load SP
			"@SP",
			"D=M"
			// handle the rest of the function declaration
		};

		_FUNCTION_PUSHES_START = new String[] {
			// store 0 at SP
			"@0",
			"D=A",
			"@SP",
			"A=M",
			"M=D"
		};

		_FUNCTION_PUSH = new String[] {
			// store 0 at SP + x
			"D=A",
			"@1",
			"D=D+A",
			"@R13",
			"M=D",
			"@0",
			"D=A",
			"@R13",
			"A=M",
			"M=D"
		};

		_RETURN = new String[] {
			// store FRAME (= LCL) in a temp register
			"@LCL",
			"D=M",
			"@R13",
			"M=D",
			// fetch the return address and store it in a temp register
			"@5",
			"A=D-A",
			"D=M",
			"@R14",
			"M=D",
			// set address pointed to by ARG to value at SP - 1
			"@SP",
			"D=M",
			"@1",
			"A=D-A",
			"D=M",
			"@ARG",
			"A=M",
			"M=D",
			// set SP to ARG + 1
			"@ARG",
			"D=M",
			"@1",
			"D=D+A",
			"@SP",
			"M=D",
			// fetch THAT from FRAME - 1
			"@R13",
			"D=M",
			"@1",
			"A=D-A",
			"D=M",
			"@THAT",
			"M=D",
			// fetch THIS from FRAME - 2
			"@R13",
			"D=M",
			"@2",
			"A=D-A",
			"D=M",
			"@THIS",
			"M=D",
			// fetch ARG from FRAME - 3
			"@R13",
			"D=M",
			"@3",
			"A=D-A",
			"D=M",
			"@ARG",
			"M=D",
			// fetch LCL from FRAME - 4
			"@R13",
			"D=M",
			"@4",
			"A=D-A",
			"D=M",
			"@LCL",
			"M=D",
			// jump to the return address
			"@R14",
			"A=M",
			"0;JMP"
		};

		_PUSH_END = new String[] {
			// the value to push is in D
			// store it at SP
			"@SP",
			"A=M",
			"M=D",
			// increment SP
			"D=A",
			"@1",
			"D=D+A",
			"@SP",
			"M=D"
		};

		_POP_START = new String[] {
			// decrement SP
			"@SP",
			"D=M",
			"@1",
			"D=D-A",
			"@SP",
			"M=D",
			// fetch the value at SP and store it in a temp register
			"A=D",
			"D=M",
			"@R13",
			"M=D"
			// the result will be stored in the correct location
		};

		_IF_GOTO_START = new String[] {
			// decrement SP
			"@SP",
			"D=M",
			"@1",
			"D=D-A",
			"@SP",
			"M=D",
			// fetch the argument for determining the outcome of the branch
			"A=M",
			"D=M"
			// perform the conditional jump based on D
		};

		_ARITHMETIC_LOGIC_OPERATION_UNARY_START = new String[] {
			// load SP
			"@SP",
			"D=M",
			// decrement to point at the argument for the operation
			"@1",
			"A=D-A",
			// fetch the argument in preparation for the operation
			"D=M"
			// the result will be stored in D and written to the correct (current) address
		};

		_ARITHMETIC_LOGIC_OPERATION_BINARY_START = new String[] {
			// load SP
			"@SP",
			"D=M",
			// decrement by two to point at the first argument for the operation
			"@2",
			"A=D-A",
			"D=A",
			// store the pointer to the first argument in a temp register
			"@R13",
			"M=D",
			// increment the pointer by one to point at the second argument for the operation
			"@1",
			"D=D+A",
			// store the pointer to the second argument in a temp register
			"@R14",
			"M=D",
			// fetch the first argument
			"@R13",
			"A=M",
			"D=M",
			// jump to the second argument in preparation for the operation
			"@R14",
			"A=M"
			// the result will be stored in D
		};

		_ARITHMETIC_LOGIC_OPERATION_BINARY_END = new String[] {
			// the result is stored in D
			// write the result to the correct address (SP - 2)
			"@R13",
			"A=M",
			"M=D",
			// decrement SP
			"@SP",
			"D=M",
			"@1",
			"D=D-A",
			"@SP",
			"M=D"
		};
	}

	public static ArrayList<String> getVMInitialization() {
		return new ArrayList<String>(Arrays.asList(_VM_INITIALIZATION));
	}

	public static ArrayList<String> getCall(String functionName, int argumentCount, int tempLabelNumber) {
		ArrayList<String> result = new ArrayList<>();

		result.add(String.format("@%s_RETURN_%d", functionName, tempLabelNumber));

		Translations._addTranslationBlock(result, Translations._CALL_PUSHES);

		// since _FUNCTION_PUSHES has not updated SP yet, ARG = SP - n

		result.add(String.format("@%d", argumentCount));
		result.add("D=D-A");
		result.add("@ARG");
		result.add("M=D");
		result.add("@SP");
		result.add("D=M");
		result.add("@5");
		result.add("D=D+A");
		result.add("@SP");
		result.add("M=D");
		result.add(String.format("@%s", functionName));
		result.add("0;JMP");
		result.add(String.format("(%s_RETURN_%d)", functionName, tempLabelNumber));

		return result;
	}

	public static ArrayList<String> getFunction(String functionName, int variableCount) {
		ArrayList<String> result = new ArrayList<>();

		result.add(String.format("(%s)", functionName));

		if (variableCount == 0) {
			return result;
		}

		Translations._addTranslationBlock(result, Translations._FUNCTION_PUSHES_START);

		for (int i = 0; i < variableCount - 1; ++i) {
			Translations._addTranslationBlock(result, Translations._FUNCTION_PUSH);
		}

		result.add("@SP");
		result.add("D=M");
		result.add(String.format("@%d", variableCount));
		result.add("D=D+A");
		result.add("@SP");
		result.add("M=D");

		return result;
	}

	public static ArrayList<String> getReturn() {
		return new ArrayList<String>(Arrays.asList(Translations._RETURN));
	}

	public static ArrayList<String> getGoto(String functionName, String labelName) {
		return new ArrayList<String>(Arrays.asList(new String[] {
			String.format("@%s$%s", functionName, labelName),
			"0;JMP"
		}));
	}

	public static ArrayList<String> getIfGoto(String functionName, String labelName) {
		ArrayList<String> result = new ArrayList<>();

		Translations._addTranslationBlock(result, Translations._IF_GOTO_START);

		result.add(String.format("@%s$%s", functionName, labelName));
		result.add(String.format("D;JNE"));

		return result;
	}

	public static ArrayList<String> getPush(String moduleName, String segmentName, int segmentIndex) {
		ArrayList<String> result = new ArrayList<>();

		switch (segmentName) {
			case "argument":
				result.add("@ARG");
				result.add("D=M");
				result.add(String.format("@%d", segmentIndex));
				result.add("A=D+A");
				result.add("D=M");
				break;

			case "local":
				result.add("@LCL");
				result.add("D=M");
				result.add(String.format("@%d", segmentIndex));
				result.add("A=D+A");
				result.add("D=M");
				break;

			case "static":
				result.add(String.format("@%s.%d", moduleName, segmentIndex));
				result.add("D=M");
				break;

			case "constant":
				result.add(String.format("@%d", segmentIndex));
				result.add("D=A");
				break;

			case "this":
				result.add("@THIS");
				result.add("D=M");
				result.add(String.format("@%d", segmentIndex));
				result.add("A=D+A");
				result.add("D=M");
				break;

			case "that":
				result.add("@THAT");
				result.add("D=M");
				result.add(String.format("@%d", segmentIndex));
				result.add("A=D+A");
				result.add("D=M");
				break;

			case "pointer":
				result.add(segmentIndex == 0 ? "@THIS" : "@THAT");
				result.add("D=M");
				break;

			case "temp":
				result.add(String.format("@R%d", 5 + segmentIndex));
				result.add("D=M");
				break;
		}

		Translations._addTranslationBlock(result, Translations._PUSH_END);

		return result;
	}

	public static ArrayList<String> getPop(String moduleName, String segmentName, int segmentIndex) {
		ArrayList<String> result = new ArrayList<>();

		Translations._addTranslationBlock(result, Translations._POP_START);

		switch (segmentName) {
			case "argument":
				result.add("@ARG");
				result.add("D=M");
				result.add(String.format("@%d", segmentIndex));
				result.add("D=D+A");
				result.add("@R14");
				result.add("M=D");
				result.add("@R13");
				result.add("D=M");
				result.add("@R14");
				result.add("A=M");
				result.add("M=D");
				break;

			case "local":
				result.add("@LCL");
				result.add("D=M");
				result.add(String.format("@%d", segmentIndex));
				result.add("D=D+A");
				result.add("@R14");
				result.add("M=D");
				result.add("@R13");
				result.add("D=M");
				result.add("@R14");
				result.add("A=M");
				result.add("M=D");
				break;

			case "static":
				result.add(String.format("@%s.%d", moduleName, segmentIndex));
				result.add("M=D");
				break;

			case "constant":
				// nothing to do
				break;

			case "this":
				result.add("@THIS");
				result.add("D=M");
				result.add(String.format("@%d", segmentIndex));
				result.add("D=D+A");
				result.add("@R14");
				result.add("M=D");
				result.add("@R13");
				result.add("D=M");
				result.add("@R14");
				result.add("A=M");
				result.add("M=D");
				break;

			case "that":
				result.add("@THAT");
				result.add("D=M");
				result.add(String.format("@%d", segmentIndex));
				result.add("D=D+A");
				result.add("@R14");
				result.add("M=D");
				result.add("@R13");
				result.add("D=M");
				result.add("@R14");
				result.add("A=M");
				result.add("M=D");
				break;

			case "pointer":
				result.add(segmentIndex == 0 ? "@THIS" : "@THAT");
				result.add("M=D");
				break;

			case "temp":
				result.add(String.format("@R%d", 5 + segmentIndex));
				result.add("M=D");
				break;
		}

		return result;
	}

	public static ArrayList<String> getLabel(String functionName, String labelName) {
		return new ArrayList<String>(Arrays.asList(new String[] {
			String.format("(%s$%s)", functionName, labelName)
		}));
	}

	public static ArrayList<String> getArithmeticLogicOperation(String opcode, int tempLabelNumber) {
		ArrayList<String> result = new ArrayList<>();

		if (opcode.equals("neg") || opcode.equals("not")) {
			Translations._addTranslationBlock(result, Translations._ARITHMETIC_LOGIC_OPERATION_UNARY_START);

			result.add(opcode.equals("neg") ?
				"M=-D" :
				"M=!D"
			);
		}
		else {
			Translations._addTranslationBlock(result, Translations._ARITHMETIC_LOGIC_OPERATION_BINARY_START);

			if (opcode.equals("add")) {
				result.add("D=D+M");
			}
			else if (opcode.equals("and")) {
				result.add("D=D&M");
			}
			else if (opcode.equals("or")) {
				result.add("D=D|M");
			}
			else {
				result.add("D=D-M");

				if (!opcode.equals("sub")) {
					String operation = opcode.toUpperCase();

					result.add(String.format("@%s_TRUE_%d", operation, tempLabelNumber));
					result.add(String.format("D;J%s", operation));
					result.add(String.format("(%s_FALSE_%d)", operation, tempLabelNumber));
					result.add("@0");
					result.add("D=A");
					result.add(String.format("@%s_DONE_%d", operation, tempLabelNumber));
					result.add("0;JMP");
					result.add(String.format("(%s_TRUE_%d)", operation, tempLabelNumber));
					result.add("@0");
					result.add("D=A");
					result.add("@1");
					result.add("D=D-A");
					result.add(String.format("(%s_DONE_%d)", operation, tempLabelNumber));
				}
			}

			Translations._addTranslationBlock(result, Translations._ARITHMETIC_LOGIC_OPERATION_BINARY_END);
		}

		return result;
	}

	private static void _addTranslationBlock(ArrayList<String> output, String[] block) {
		for (String line : block) {
			output.add(line);
		}
	}
}
