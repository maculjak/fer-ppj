package hr.fer.zemris.ppj.lab04;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * This is the main class for this lab exercise in which code generation logic is implemented.
 * <p>
 * Before generating the code, semantic analysis is performed.
 * <p>
 * This class contains the <code>main</code> method which in this case takes no arguments from the command line.
 *
 * @author Pajser, Čarli, Stena, Žuti
 */
public class GeneratorKoda {
	
	private static SymbolTableNode currentScope;
	private static List<Function> declaredNotDefinedFunctions = new ArrayList<>();
	private static List<Function> defined = new ArrayList<>();
	private static StringBuilder constantBuilder = new StringBuilder();
	
	private static int labelIndex = 0;
	private static int mislavIndex = 0;
	private static int bogdanIndex = 0;
	private static int duleIndex = 0;
	private static int savicIndex = 0;
	
	private static boolean DO_NOT_PUSH = false;
	
	private static Function global = new Function();
	private static HashSet<String> globalVariables = new HashSet<>();
	
	/**
	 * This method is the starting point of this program's execution.
	 *
	 * @param args - command line arguments (not used)
	 */
	public static void main(String[] args) {
		EntryFileParser efp = new EntryFileParser();
		GenerativeTree generativeTree = efp.getGenerativeTree();

		currentScope = new SymbolTableNode();
		check(generativeTree.getRoot());
		NodeData mainData = getDeclaration("main");

		if (mainData == null) {
			System.out.println("main");
			System.exit(1);
		}

		if (!mainData.getType().equals("int") || !mainData.isFunction()
				|| mainData.getFunctionArguments().size() > 0) {
			System.out.println("main");
			System.exit(1);
		}

		if (!declaredNotDefinedFunctions.isEmpty()) {
			System.out.println("funkcija");
			System.exit(1);
		}

		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("a.frisc")));
			bw.write(formatLine("MOVE 40000, R7"));
			if (!(global.getFirstInstruction() == null)) {
				bw.write(formatLine(global.getFirstInstruction()));
				bw.write(global.getCode());
			}
			bw.write(formatLine("CALL F_MAIN"));
			bw.write(formatLine("HALT"));

			for(int i = 0; i < defined.size(); i++) {
				Function func = defined.get(i);
				if (func.getType().equals("void")) func.ret();
				bw.write(formatLine(func.getFirstInstruction().trim(), labelFromName(func.getName())));
				bw.write(func.getCode());
			}
			bw.write(constantBuilder.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	/**
	 * This method checks all semantic rules for a given <code>node</code>.
	 *
	 * @param node - a generative tree node to have its semantic rules checked
	 */
	private static void check(GenerativeTreeNode node) {
		List<GenerativeTreeNode> children = node.getChildren();

		NodeData nodeData = node.getData();
		String nonTerminal = nodeData.getLexeme();

		GenerativeTreeNode firstChild = children.get(0);
		NodeData firstChildData = firstChild.getData();
		String firstLexeme = firstChildData.getLexeme();

		Function parent = defined.stream()
				.filter(f -> f.getName().equals(findParentFunctionName(currentScope)))
				.findFirst()
				.orElse(global);

		switch(nonTerminal) {
			case "<primarni_izraz>":
				switch (firstLexeme) {
					case "IDN":
						NodeData declaration = getDeclaration(firstChild.getLexicalUnit());
						if(declaration == null) error(node);
						else {
							node.setType(declaration.getType());
							node.setArray(declaration.isArray());
							node.setFunction(declaration.isFunction());
							node.setConstant(declaration.isConstant());
							node.setlValue(declaration.islValue());
							node.setDefined(declaration.isDefined());
							node.setNumberOfElements(declaration.getNumberOfElements());
							node.setName(declaration.getName());
							node.setFunctionArguments(declaration.getFunctionArguments());
							String variableName = firstChild.getLexicalUnit();
							if (node.isArray()) {
								if (parent.isParameter(variableName)) {
									parent.loadR(0, parent.getStackPointerOffset(variableName));
									parent.pushR(0);
								} else if (!globalVariables.contains(variableName + 0)){
									parent.moveLabel("L_" + parent.getName().toUpperCase() + "_" + variableName + 0, 0);
									parent.pushR(0);
								}
							} else {
								if (parent.isLocalVariableOrParameter(variableName)) {
									parent.loadR(0, parent.getStackPointerOffset(variableName));
									if (!DO_NOT_PUSH) parent.pushR(0);
								} else if (globalVariables.contains(variableName)) {
									parent.loadR(0, "G_" + variableName);
									if (!DO_NOT_PUSH) parent.pushR(0);
								}
							}
							DO_NOT_PUSH = false;

							if (node.isFunction()) {
								parent.call(firstChild.getLexicalUnit());
								if (node.getFunctionArguments().size() > 0)	parent.addR(7, node.getFunctionArguments().size() * 4);
								if (!node.getType().equals("void")) parent.pushR(6);
							}
						}
						break;
					case "BROJ":
						if(!isInteger(firstChild.getLexicalUnit())) error(node);
						node.setType("int");
						node.setlValue(false);
						node.setName(firstChild.getLexicalUnit());
						int number = Integer.parseInt(firstChild.getLexicalUnit());

						if (number > Math.pow(2, 19) - 1 || number < - Math.pow(2, 19)) {
							appendConstant("C_" + labelIndex, number);
							parent.loadR(0, "C_" + labelIndex++);
							parent.pushR(0);

						} else {
							parent.move(number, 0);
							parent.pushR(0);
						}
						break;
					case "ZNAK":
						if(!isCharacter(firstChild.getLexicalUnit())) error(node);
						node.setType("char");
						node.setlValue(false);
						int value = firstChild.getLexicalUnit().charAt(1);
						parent.move(value, 0);
						parent.pushR(0);
						break;
					case "NIZ_ZNAKOVA":
						if(!isString(firstChild.getLexicalUnit())) error(node);
						node.setType("char");
						node.setConstant(true);
						node.setlValue(false);
						node.setArray(true);
						break;
					case "L_ZAGRADA":
						GenerativeTreeNode secondChild = children.get(1);
						check(secondChild);
						node.setType(secondChild.getType());
						node.setArray(secondChild.isArray());
						node.setConstant(secondChild.isConstant());
						node.setFunction(secondChild.isFunction());
						node.setlValue(secondChild.islValue());
						break;
					default:
						error(node);
				} break;
			case "<postfiks_izraz>":
				if(firstLexeme.equals("<primarni_izraz>")) {
					check(firstChild);
					node.setType(firstChild.getType());
					node.setArray(firstChild.isArray());
					node.setFunction(firstChild.isFunction());
					node.setFunctionArguments(firstChild.getFunctionArguments());
					node.setlValue(firstChild.islValue());
					node.setConstant(firstChild.isConstant());
					node.setName(firstChild.getName());
				} else if(firstLexeme.equals("<postfiks_izraz>")) {
					GenerativeTreeNode secondChild = children.get(1);
					GenerativeTreeNode thirdChild;
					String secondLexeme = secondChild.getLexeme();
					boolean temp = DO_NOT_PUSH;
					switch(secondLexeme) {
						case "L_UGL_ZAGRADA":
							thirdChild = children.get(2);
							check(thirdChild);
							if(!canBeCast(thirdChild.getType(), "int") || thirdChild.isArray()) error(node);
							check(firstChild);
							if(!firstChild.isArray()) error(node);
							node.setType(firstChild.getType());
							node.setArray(false);
							node.setFunctionArguments(firstChild.getFunctionArguments());
							node.setlValue(!firstChild.isConstant());
							String arrayName = firstChild.getChildren().get(0).getChildren().get(0).getLexicalUnit();
							if (globalVariables.contains(arrayName + 0)) {
								parent.moveLabel("G_" + arrayName + 0, 1);
								parent.popR(0);
								parent.shlR(0, 2);
								parent.subR(1, 0, 1);
								parent.loadR(0, "R1");
								if (!temp) parent.pushR(0);
								DO_NOT_PUSH = false;
							} else {
								parent.popR(0);
								parent.popR(1);
								parent.shlR(1, 2);
								parent.subR(0, 1, 0);
								if (temp) parent.pushR(0);
								parent.loadR(0, "R0");
								if (!temp) parent.pushR(0);
								DO_NOT_PUSH = false;
							}


							break;
						case "L_ZAGRADA":
							thirdChild = children.get(2);
							if(thirdChild.getLexeme().equals("D_ZAGRADA")) {
								check(firstChild);
								if(!(firstChild.isFunction() && firstChild.getFunctionArguments().size() == 0)) error(node);
								node.setType(firstChild.getType());
								node.setArray(firstChild.isArray());
								node.setFunction(false);
								node.setFunctionArguments(firstChild.getFunctionArguments());
								node.setlValue(false);
							} else {
								check(thirdChild);
								check(firstChild);
								List<NodeData> arguments = thirdChild.getFunctionArguments();
								List<NodeData> parameters = firstChild.getFunctionArguments();
								int argSize = arguments.size();
								int paramSize = parameters.size();
								if (argSize != paramSize) error(node);
								for (int i = 0; i < paramSize; i++) {
									if (!canBeCast(arguments.get(i).getType(), parameters.get(i).getType())
											|| parameters.get(i).isArray()
											&& arguments.get(i).isArray() && !parameters.get(i).isConstant() && arguments.get(i).isConstant()
											|| parameters.get(i).isFunction()
											|| arguments.get(i).isArray() != parameters.get(i).isArray()) error(node);
								}

								node.setType(firstChild.getType());
								node.setArray(firstChild.isArray());
								node.setFunction(false);
								node.setlValue(false);
							} break;
						case "OP_INC":
						case "OP_DEC":
							check(firstChild);
							if(!(firstChild.islValue() && canBeCast(firstChild.getType(), "int"))
									|| firstChild.isConstant()
									|| firstChild.isFunction()
									|| firstChild.isArray()) error(node);
							node.setType("int");
							node.setArray(false);
							node.setlValue(false);
							String variable = firstChild.getChildren().get(0).getChildren().get(0).getLexicalUnit();
							parent.loadR(0, "R7");
							if (secondLexeme.equals("OP_INC")) parent.addR(0, 1);
							else parent.subR(0, 1);
							if (globalVariables.contains((variable))) {
								parent.storeR(0, "G_" + variable);
							} else {
								parent.storeR(0, parent.getStackPointerOffset(variable));
							}
							break;
						default:
							error(node);
							break;
					}
				} break;
			case "<lista_argumenata>":
				if(firstLexeme.equals("<izraz_pridruzivanja>")) {
					check(firstChild);
					node.addFunctionArgument(firstChildData);
				} else if(firstLexeme.equals("<lista_argumenata>")) {
					GenerativeTreeNode thirdChild = children.get(2);
					check(firstChild);
					check(thirdChild);
					node.setFunctionArguments(firstChild.getFunctionArguments());
					node.addFunctionArgument(thirdChild.getData());
				} break;
			case "<unarni_izraz>":
				GenerativeTreeNode secondChild;
				switch(firstLexeme) {
					case "<postfiks_izraz>":
						check(firstChild);
						node.setType(firstChild.getType());
						node.setArray(firstChild.isArray());
						node.setFunction(firstChild.isFunction());
						node.setConstant(firstChild.isConstant());
						node.setlValue(firstChild.islValue());
						node.setName(firstChild.getName());
						break;
					case "OP_INC":
					case "OP_DEC":
						secondChild = children.get(1);
						check(secondChild);
						if(!(secondChild.islValue() && canBeCast(secondChild.getType(), "int"))) error(node);
						node.setType("int");
						node.setArray(false);
						node.setlValue(false);
						String variable = secondChild.getChildren().get(0).getChildren().get(0).getChildren().get(0).getLexicalUnit();
						parent.popR(0);
						if (firstLexeme.equals("OP_INC")) parent.addR(0, 1);
						else parent.subR(0, 1);
						if (globalVariables.contains(variable)) {
							parent.storeR(0, "G_" + variable);
						} else {
							parent.storeR(0, parent.getStackPointerOffset(variable));
						}
						parent.pushR(0);
						break;
					case "<unarni_operator>":
						secondChild = children.get(1);
						check(secondChild);
						if(!canBeCast(secondChild.getType(), "int")) error(node);
						node.setType("int");
						node.setArray(false);
						node.setlValue(false);
						parent.popR(0);
						if (firstChild.getChildren().get(0).getLexeme().equals("MINUS")) {
							parent.xorR(0, -1);
							parent.addR(0, 1);
						}
						parent.pushR(0);

						break;
				} break;
			case "<unarni_operator>":
				// No checks.
				break;
			case "<cast_izraz>":
				if(firstLexeme.equals("<unarni_izraz>")) {
					check(firstChild);
					node.setType(firstChild.getType());
					node.setArray(firstChild.isArray());
					node.setFunction(firstChild.isFunction());
					node.setConstant(firstChild.isConstant());
					node.setlValue(firstChild.islValue());
					node.setName(firstChild.getName());

				} else if(firstLexeme.equals("L_ZAGRADA")) {
					secondChild = children.get(1);
					GenerativeTreeNode fourthChild = children.get(3);
					check(secondChild);
					check(fourthChild);
					if(!(secondChild.getType().equals("int") || !secondChild.getType().equals("char")
							|| fourthChild.getType().equals("int") || secondChild.getType().equals("char"))
							|| (secondChild.isArray() != fourthChild.isArray()) || fourthChild.isFunction()
							|| secondChild.isArray() && fourthChild.isArray() && secondChild.isConstant() && !fourthChild.isConstant()) error(node);
					node.setType(secondChild.getType());
					node.setArray(secondChild.isArray());
					node.setFunction(secondChild.isFunction());
					node.setConstant(secondChild.isConstant());
				} break;
			case "<ime_tipa>":
				if(firstLexeme.equals("<specifikator_tipa>")) {
					check(firstChild);
					node.setType(firstChild.getType());
					node.setConstant(firstChild.isConstant());
					node.setFunction(firstChild.isFunction());
					node.setArray(firstChild.isArray());
				} else if(firstLexeme.equals("KR_CONST")) {
					secondChild = children.get(1);
					check(secondChild);
					if(secondChild.getType().equals("void")) error(node);
					node.setType(secondChild.getType());
					node.setArray(secondChild.isArray());
					node.setConstant(true);
				} break;
			case "<specifikator_tipa>":
				switch(firstLexeme) {
					case "KR_VOID":
						node.setType("void");
						break;
					case "KR_CHAR":
						node.setType("char");
						break;
					case "KR_INT":
						node.setType("int");
						break;
				} break;
			case "<multiplikativni_izraz>":
			case "<aditivni_izraz>":
			case "<odnosni_izraz>":
			case "<jednakosni_izraz>":
			case "<bin_i_izraz>":
			case "<bin_xili_izraz>":
			case "<bin_ili_izraz>":
			case "<log_i_izraz>":
			case "<log_ili_izraz>":
				if(!firstLexeme.equals(nonTerminal)) {
					check(firstChild);
					node.setType(firstChild.getType());
					node.setArray(firstChild.isArray());
					node.setFunction(firstChild.isFunction());
					node.setConstant(firstChild.isConstant());
					node.setlValue(firstChild.islValue());
					node.setName(firstChild.getName());
				} else {
					GenerativeTreeNode thirdChild = children.get(2);
					check(firstChild);
					int k = mislavIndex++;
					int j = bogdanIndex++;

					if (nonTerminal.equals("<log_i_izraz>")) {
						parent.popR(0);
						parent.andR(0, 0, 0);
						parent.cmp0(0);
						parent.jeq("MISLAV" + k);
					} else if (nonTerminal.equals("<log_ili_izraz>")) {
						parent.popR(0);
						parent.cmp1(0);
						parent.jeq("MISLAV" + k);
					}
					if(!canBeCast(firstChild.getType(), "int") || firstChild.isArray() || firstChild.isFunction()) error(node);
					check(thirdChild);
					if (nonTerminal.equals("<log_i_izraz>") || nonTerminal.equals("<log_ili_izraz>")) {
						parent.setLabel("MISLAV" + k);
						parent.popR(0);
					}
					if(!canBeCast(thirdChild.getType(), "int") || thirdChild.isArray() || thirdChild.isFunction()) error(node);
					node.setType("int");
					node.setlValue(false);
					if (!nonTerminal.equals("<log_i_izraz>") && !nonTerminal.equals("<log_ili_izraz>")) {
						parent.popR(0);
						parent.popR(1);
					}
					switch (nonTerminal) {
						case "<aditivni_izraz>":
							if (children.get(1).getLexeme().equals("PLUS")) parent.addR(0, 1, 0);
							else parent.subR(1, 0, 0);
							break;
						case "<bin_ili_izraz>":
							parent.orR(0, 1, 0);
							break;
						case "<bin_i_izraz>":
							parent.andR(0, 1, 0);
							break;
						case "<bin_xili_izraz>":
							parent.xorR(0, 1, 0);
							break;
						case "<odnosni_izraz>":
						case "<jednakosni_izraz>":
							parent.cmpR(1, 0);
							parent.j(children.get(1).getLexicalUnit(), "MISLAV" + k);
							parent.move(0, 0);
							parent.jp("BOGDAN" + j);
							parent.setLabel("MISLAV" + k);
							parent.move(1, 0);
							parent.setLabel("BOGDAN" + j);
							break;
						case "<multiplikativni_izraz>":
							switch (children.get(1).getLexeme()) {
								case "OP_PUTA":
									parent.mulR();
									break;
								case "OP_MOD":
									parent.modR();
									break;
								case "OP_DIV":
									parent.divR();
							}

					}
					parent.pushR(0);
				}

				break;
			case "<izraz_pridruzivanja>":
				if(firstLexeme.equals("<log_ili_izraz>")) {
					check(firstChild);
					node.setType(firstChild.getType());
					node.setArray(firstChild.isArray());
					node.setFunction(firstChild.isFunction());
					node.setConstant(firstChild.isConstant());
					node.setlValue(firstChild.islValue());
					node.setName(firstChild.getName());
				} else if(firstLexeme.equals("<postfiks_izraz>")) {
					GenerativeTreeNode thirdChild = children.get(2);
					DO_NOT_PUSH = true;
					check(firstChild);
					if(!firstChild.islValue()) error(node);
					check(thirdChild);
					if(!canBeCast(thirdChild.getType(), firstChild.getType())
							|| firstChild.isConstant()
							|| firstChild.isArray() != thirdChild.isArray()
							|| firstChild.isArray() && thirdChild.isArray() && !firstChild.isConstant() && thirdChild.isConstant()) error(node);
					node.setType(firstChild.getType());
					node.setArray(firstChild.isArray());
					node.setFunction(firstChild.isFunction());
					node.setConstant(firstChild.isConstant());
					node.setlValue(false);
					parent.popR(0);


					String variable = firstChild.getChildren().get(0).getChildren().get(0).getLexicalUnit();
					int index = -1;
					if (firstChild.getChildren().get(0).getChildren().get(0).isArray()) {
						variable = firstChild.getChildren().get(0).getChildren().get(0).getChildren().get(0).getLexicalUnit();
						index = Integer.parseInt(firstChild.getChildren().get(2).getName());
					}
					int offset = parent.getStackPointerOffset(variable);

					if (offset == -1) {
						parent.storeR(0, "G_" + variable + (index == -1 ? "" : index));
					} else {
						if (parent.isVariableInMemory(variable)) {
							parent.popR(3);
							parent.storeR(0, "R3");
						}
						else parent.storeR(0, offset);
					}

				} break;
			case "<izraz>":
				if(firstLexeme.equals("<izraz_pridruzivanja>")) {
					check(firstChild);
					node.setType(firstChild.getType());
					node.setArray(firstChild.isArray());
					node.setlValue(firstChild.islValue());
					node.setFunction(firstChild.isFunction());
					node.setName(firstChild.getName());
				} else if(firstLexeme.equals("<izraz>")) {
					GenerativeTreeNode thirdChild = children.get(2);
					check(firstChild);
					check(thirdChild);
					node.setType(thirdChild.getType());
					node.setArray(thirdChild.isArray());
					node.setFunction(thirdChild.isFunction());
					node.setlValue(false);
				} break;
			case "<slozena_naredba>":
				// symbol table node creation and new scope set
				SymbolTableNode newScope = new SymbolTableNode();
				newScope.setParent(currentScope);
				currentScope = newScope;
				currentScope.setParentFunctionName(node.getName());
				currentScope.setParentFunctionType(node.getType());
				currentScope.setInLoop(node.isInLoop());

				int numberOfArguments = node.getFunctionArguments().size();
				for(int i = 0; i < numberOfArguments; i++)
					currentScope.setLocalDeclarationData(node.getFunctionArguments().get(i).getName(), node.getFunctionArguments().get(i));
				
				secondChild = children.get(1);
				if(secondChild.getLexeme().equals("<lista_naredbi>")) check(secondChild);
				else if(secondChild.getLexeme().equals("<lista_deklaracija>")) {
					check(secondChild);
					node.setInLoop(secondChild.isInLoop()); // Suspicious
					check(children.get(2));
				}

				// after checking the node, return to a wider scope (parent node)
				currentScope = currentScope.getParent();
				break;
			case "<lista_naredbi>":
				if(firstLexeme.equals("<naredba>")) {
					check(firstChild);
					node.setInLoop(firstChild.isInLoop());
				} else if(firstLexeme.equals("<lista_naredbi>")) {
					secondChild = children.get(1);
					check(firstChild);
					check(secondChild);
				} break;
			case "<naredba>":
				firstChild.setInLoop(node.isInLoop());
				firstChild.setFunction(node.isFunction());
				check(firstChild);
			case "<izraz_naredba>":
				if(firstLexeme.equals("TOCKAZAREZ")) {
					node.setType("int");
				} else if(firstLexeme.equals("<izraz>")) {
					firstChild.setInLoop(node.isInLoop());
					check(firstChild);
					node.setType(firstChild.getType());
					node.setFunction(firstChild.isFunction());
					node.setArray(firstChild.isArray());
				} break;
			case "<naredba_grananja>":
				if(children.size() == 5) {
					secondChild = children.get(2);
					check(secondChild);
					parent.popR(0);
					parent.cmp0(0);
					int k = mislavIndex++;
					parent.jeq("MISLAV" + k);
					if(!canBeCast(secondChild.getType(), "int") || secondChild.isFunction() || secondChild.isArray()) error(node);
					check(children.get(4));
					parent.getFunctionBuilder().append("MISLAV").append(k).append("\n");
				} else if(children.size() == 7) {
					secondChild = children.get(2);
					check(secondChild);
					parent.popR(0);
					parent.cmp0(0);
					int k = mislavIndex++;
					int j = bogdanIndex++;
					parent.jeq("MISLAV" + k);
					if(!canBeCast(secondChild.getType(), "int") || secondChild.isFunction() || secondChild.isArray()) error(node);
					check(children.get(4));
					parent.jp("BOGDAN" + j);
					parent.setLabel("MISLAV" + k);
					check(children.get(6));
					parent.getFunctionBuilder().append("BOGDAN").append(j).append("\n");
				} break;
			case "<naredba_petlje>":
				int k = mislavIndex++;
				int j = bogdanIndex++;
				int m = duleIndex++;
				int n = savicIndex++;
				if(firstLexeme.equals("KR_WHILE")) {
					GenerativeTreeNode thirdChild = children.get(2);
					GenerativeTreeNode fifthChild = children.get(4);

					parent.setLabel("MISLAV" + k);
					check(thirdChild);
					parent.popR(0);
					parent.cmp0(0);
					parent.j("==", "BOGDAN" + j);
					if(!canBeCast(thirdChild.getType(), "int")) error(node);
					fifthChild.setInLoop(true);
					check(fifthChild);
					parent.jp("MISLAV" + k);
					parent.getFunctionBuilder().append("BOGDAN").append(j).append("\n");
				} else if(firstLexeme.equals("KR_FOR")) {
					GenerativeTreeNode fourthChild = children.get(3);
					check(children.get(2));
					parent.setLabel("MISLAV" + k);
					check(fourthChild);
					parent.popR(0);
					parent.cmp0(0);
					parent.j("==", "BOGDAN" + j);
					parent.jp("DULE" + m);
					if(!canBeCast(fourthChild.getType(), "int") || fourthChild.isFunction() || fourthChild.isArray()) error(node);
					if(children.size() == 7){
						GenerativeTreeNode seventhChild = children.get(6);

						parent.setLabel("DULE" + m);
						check(seventhChild);
						parent.jp("SAVIC" + n);
						parent.setLabel("SAVIC" + n);
						int spo = parent.getStackPointerOffset();
						check(children.get(4));
						if (spo != parent.getStackPointerOffset()) {
							parent.addR(7, parent.getStackPointerOffset() - spo);
							parent.setStackPointerOffset(spo);
						}
						seventhChild.setInLoop(true);
						parent.jp("MISLAV" + k);
						parent.getFunctionBuilder().append("BOGDAN").append(j).append("\n");
					} else {
						GenerativeTreeNode sixthChild = children.get(5);
						sixthChild.setInLoop(true);
						parent.setLabel("DULE" + m);
						check(sixthChild);
						parent.jp("MISLAV" + k);
						parent.getFunctionBuilder().append("BOGDAN").append(j).append("\n");
					}
				} break;
			case "<naredba_skoka>":
				switch(firstLexeme) {
					case "KR_CONTINUE":
					case "KR_BREAK":
						if(!scopeInLoop(currentScope)) {
							if (!node.isInLoop()) error(node);
						}
						break;
					case "KR_RETURN":
						secondChild = children.get(1);
						if(secondChild.getLexeme().equals("<izraz>")) {
							check(secondChild);
							if (currentScope.isReturnTypeArray() != secondChild.isArray()) error(node);
							if (!canBeCast(secondChild.getType(), findParentFunctionType(currentScope))
									|| secondChild.isFunction()) error(node);

							assert parent != null;
							if (secondChild.isFunction()) parent.pushR(6);
							parent.popR(6);
							parent.ret();
						} else if (secondChild.getLexeme().equals("TOCKAZAREZ")) {
							if (currentScope.isReturnTypeArray()) error(node);
							if (!(findParentFunctionType(currentScope).equals("void")) && secondChild.getType() == null) error(node);

						}
				} break;
			case "<prijevodna_jedinica>":
				if(firstLexeme.equals("<vanjska_deklaracija>")) {
					check(firstChild);
				} else if(firstLexeme.equals("<prijevodna_jedinica>")) {
					check(firstChild);
					check(children.get(1));
				} break;
			case "<vanjska_deklaracija>":
				check(firstChild);
				break;
			case "<definicija_funkcije>":
				GenerativeTreeNode fourthChild = children.get(3);
				GenerativeTreeNode sixthChild = children.get(5);

				check(firstChild);
				if(firstChild.isConstant()) error(node);
				String functionName = children.get(1).getLexicalUnit();
				boolean hasArguments = !fourthChild.getLexeme().equals("KR_VOID");

				NodeData functionData = getDeclaration(functionName);
				if(functionData != null && (functionData.isDefined() || !functionData.isFunction())) error(node);

				if(!hasArguments) {
					if(functionData != null) {
						if(!(functionData.getFunctionArguments().isEmpty()
								&& functionData.getType().equals(firstChild.getData().getType())))
							error(node);
					}
				} else {
					check(fourthChild);
					if(functionData != null) {
						if(!(functionData.getFunctionArguments().equals(fourthChild.getFunctionArguments())
								&& functionData.getType().equals(firstChild.getData().getType())))
							error(node);
					}
				}

				node.setFunction(true);
				node.setDefined(true);
				node.setType(firstChild.getType());
				node.setArray(firstChild.isArray());
				List<NodeData> arguments = new ArrayList<>(fourthChild.getFunctionArguments());
				node.setFunctionArguments(fourthChild.getFunctionArguments());
				String parentFunctionName = findParentFunctionName(currentScope);
				Function function = new Function(node.getType(), node.isArray(), functionName, arguments);
				for (NodeData argument : arguments) {
					argument.setlValue(true);
					function.addStackPointerOffset(argument.getName());
					if (argument.isArray()) {
						function.addLocalVariableToMemory(argument.getName());
					}
				}

				currentScope.addLocalDeclaration(functionName, node.getData());
				currentScope.setParentFunctionType(node.getType());
				currentScope.setParentFunctionName(functionName);
				while (declaredNotDefinedFunctions.remove(function));
				defined.add(function);


				if(hasArguments)
					sixthChild.setFunctionArguments(fourthChild.getFunctionArguments());
				
				sixthChild.setType(fourthChild.getType());
				sixthChild.setName(fourthChild.getLexicalUnit());
				sixthChild.setArray(fourthChild.isArray());
				sixthChild.setFunction(fourthChild.isFunction());

				check(sixthChild);
				break;
			case "<lista_parametara>":
				if(children.size() == 1) {
					check(firstChild);
					node.addFunctionArgument(firstChildData);
				} else if (children.size() == 3) {
					check(firstChild);
					GenerativeTreeNode thirdChild = children.get(2);
					check(thirdChild);
					
					if(firstChild.getFunctionArgumentNames().contains(thirdChild.getName())) error(node);
					
					node.setFunctionArguments(firstChild.getFunctionArguments());
					node.addFunctionArgument(thirdChild.getData());
				} break;
			case "<deklaracija_parametra>":
				if(children.size() == 2) {
					check(firstChild);
					if(firstChild.getType().equals("void")) error(node);

					node.setType(firstChild.getType());
					node.setArray(firstChild.isArray());
					node.setConstant(firstChild.isConstant());
					node.setName(children.get(1).getLexicalUnit());
				} else {
					check(firstChild);
					if(firstChild.getType().equals("void")) error(node);
					node.setArray(true);
					node.setType(firstChild.getType());
					node.setConstant(firstChild.isConstant());
					node.setName(children.get(1).getLexicalUnit());
				} break;
			case "<lista_deklaracija>":
				if (children.size() == 1) check(firstChild);
				else if (children.size() == 2) {
					check(firstChild);
					check(children.get(1));
				} break;
			case "<deklaracija>":
				secondChild = children.get(1);
				check(firstChild);
				secondChild.setType(firstChild.getType());
				secondChild.setFunction(firstChild.isFunction());
				secondChild.setArray(firstChild.isArray());
				secondChild.setConstant(firstChild.isConstant());
				check(children.get(1));
				break;
			case "<lista_init_deklaratora>":
				if (children.size() == 1) {
					firstChild.setType(node.getType());
					firstChild.setArray(node.isArray());
					firstChild.setFunction(node.isFunction());
					firstChild.setConstant(node.isConstant());
					check(firstChild);
				} else if (children.size() == 3) {
					firstChild.setType(node.getType());
					check(firstChild);
					GenerativeTreeNode thirdChild = children.get(2);
					thirdChild.setType(node.getType());
					thirdChild.setArray(node.isArray());
					thirdChild.setFunction(node.isFunction());
					thirdChild.setConstant(node.isConstant());
					check(thirdChild);
				} break;
			case "<init_deklarator>":
				if(children.size() == 1) {
					firstChild.setType(node.getType());
					firstChild.setArray(node.isArray());
					firstChild.setConstant(node.isConstant());
					firstChild.setFunction(node.isFunction());
					parent.move(0, 0);
					parent.pushR(0);
					check(firstChild);
					parentFunctionName = findParentFunctionName(currentScope);
					if (firstChild.isArray()) {
						parent.popR(0);
						String arrayName = firstChild.getChildren().get(0).getLexicalUnit();
						for (int i = firstChild.getNumberOfElements() - 1; i >= 0; i--) {
							parent.move(0, 0);
							if (parentFunctionName == null) {
								parent.storeR(0, "G_" + arrayName + i);
								appendConstant("G_" + arrayName + i);
							} else {
								parent.storeR(0, "L_MAIN_" + arrayName + i);
								parent.addLocalVariableToMemory("L_" + parent.getName().toUpperCase() + "_" + arrayName + i);
								appendConstant("L_" + parent.getName().toUpperCase() + "_" + arrayName + i);
							}
						}
					}

					if(firstChild.isConstant()) error(node);
				} else {
					GenerativeTreeNode thirdChild = children.get(2);
					firstChild.setType(node.getType());
					firstChild.setArray(node.isArray());
					firstChild.setFunction(node.isFunction());
					firstChild.setConstant(node.isConstant());
					check(firstChild);
					check(thirdChild);


					parentFunctionName = findParentFunctionName(currentScope);
					if (parentFunctionName == null) {

					} else {
						parent.addLocalVariable(firstChild.getChildren().get(0).getLexicalUnit());
					}

					if (firstChild.getType().equals("char") || firstChild.getType().equals("int")) {
						if(firstChild.isArray()) {
							if (thirdChild.getNumberOfElements() == 0
									|| !(thirdChild.getNumberOfElements() <= firstChild.getNumberOfElements())) error(node);
							List<NodeData> initArgs = thirdChild.getFunctionArguments();
							for (NodeData arg : initArgs) {
								if (!canBeCast(arg.getType(), firstChild.getType()) || arg.isFunction() != firstChild.isFunction() || arg.isArray()) error(node);
							}
						} else {
							if(!canBeCast(thirdChild.getType(), node.getType()) || thirdChild.isFunction() != node.isFunction()) error(node);
						}
					} else error(node);

					if (parentFunctionName == null && !firstChild.isArray()) {
						parent.popR(0);
						parent.storeR(0, "G_" + firstChild.getChildren().get(0).getLexicalUnit());
						appendConstant("G_" + firstChild.getChildren().get(0).getLexicalUnit());
					} else if (firstChild.isArray()) {
						if (parentFunctionName == null) {
							for (int i = firstChild.getNumberOfElements() - 1; i >= 0; i--) {
								parent.popR(0);
								parent.storeR(0, "G_" + firstChild.getChildren().get(0).getLexicalUnit() + i);
								appendConstant("G_" + firstChild.getChildren().get(0).getLexicalUnit() + i);
							}
						} else {
							for (int i = firstChild.getNumberOfElements() - 1; i >= 0; i--) {
								parent.popR(0);
								parent.storeR(0, "L_" + parentFunctionName.toUpperCase() + "_" + firstChild.getChildren().get(0).getLexicalUnit() + i);
								appendConstant("L_" + parentFunctionName.toUpperCase() + "_" + firstChild.getChildren().get(0).getLexicalUnit() + i);
							}
						}
					} else if (parentFunctionName != null) {
						for(int i = firstChild.getNumberOfElements() - 1; i >= 0; i--) {
							parent.popR(0);
							parent.storeR(0, "L_" + firstChild.getChildren().get(0).getLexicalUnit().toUpperCase() + i);
							appendConstant("L_" + parentFunctionName + "_" + firstChild.getChildren().get(0).getLexicalUnit().toUpperCase() + i);
						}
					}
				} break;
			case "<izravni_deklarator>":
				if(children.size() == 1) {
					if(node.getType().equals("void")) error(node);
					if(currentScope.getLocalDeclarationData(firstChild.getLexicalUnit()) != null) error(node);
					node.setlValue(true);
					currentScope.addLocalDeclaration(firstChild.getLexicalUnit(), node.getData());
					if (currentScope.getParentFunctionName() == null) {
						globalVariables.add(firstChild.getLexicalUnit());
					} else {
						parent.addLocalVariable(firstChild.getLexicalUnit());
					}
				} else if(children.get(1).getLexeme().equals("L_UGL_ZAGRADA")) {
					if(node.getType().equals("void")) error(node);
					if(currentScope.getLocalDeclarationData(firstChild.getLexicalUnit()) != null) error(node);
					try {
						int value = Integer.parseInt(children.get(2).getLexicalUnit());
						if (!(value > 0 && value <= 1024)) error(node);
						node.setArray(true);
						node.setNumberOfElements(value);
						currentScope.addLocalDeclaration(firstChild.getLexicalUnit(), node.getData());
						if (currentScope.getParentFunctionName() == null) {
							for (int i = 0; i < node.getNumberOfElements(); i++) {
								globalVariables.add(firstChild.getLexicalUnit() + i);
							}
						}
					} catch (NumberFormatException e) {
						error(node);
					}
				} else if(children.get(2).getLexeme().equals("KR_VOID")) {
					String name = firstChild.getLexicalUnit();
					NodeData declaration = currentScope.getLocalDeclarationData(name);
					if (declaration != null) {
						if (!declaration.isFunction()) error(node);
						if (!declaration.getFunctionArguments().isEmpty()) error(node);
						if (!declaration.getType().equals(node.getType())) error(node);
						if (declaration.isArray() != node.isArray()) error(node);
						if (declaration.isConstant() != node.isConstant()) error(node);
					}
					node.setFunction(true);
					node.setFunctionArguments(new ArrayList<>());
					
					currentScope.addLocalDeclaration(name, node.getData());
					Function f = new Function(node.getType(), node.isArray(), name, new ArrayList<>());
					if (!defined.contains(f)) declaredNotDefinedFunctions.add(f);
				} else if (children.get(2).getLexeme().equals("<lista_parametara>")) {
					GenerativeTreeNode thirdChild = children.get(2);
					check(thirdChild);
					String name = firstChild.getLexicalUnit();
					NodeData declaration = currentScope.getLocalDeclarationData(name);
					if (declaration != null) {
						if (!declaration.isFunction()) error(node);
						if (!declaration.getFunctionArgumentTypes().equals(thirdChild.getFunctionArgumentTypes())) error(node);
						if (!declaration.getType().equals(node.getType())) error(node);
						if (declaration.isArray() != node.isArray()) error(node);
						if (declaration.isConstant() != node.isConstant()) error(node);
					} else {
						declaration = new NodeData();
					}
					node.setFunction(true);
					node.setFunctionArguments(thirdChild.getFunctionArguments());
					declaration.setFunctionArguments(thirdChild.getFunctionArguments());
					declaration.setFunction(true);
					declaration.setType(node.getType());
					declaration.setArray(node.isArray());
					declaration.setlValue(node.islValue());
					declaration.setName(name);
					
					currentScope.addLocalDeclaration(name, declaration);
					Function f = new Function(node.getType(), node.isArray(), name, node.getFunctionArguments());
					if (!defined.contains(f)) declaredNotDefinedFunctions.add(f);
				} break;
			case "<inicijalizator>":
				if (children.size() == 1) {
					check(firstChild);
					int numberOfElements;
					if((numberOfElements = sizeOfGeneratedString(firstChild)) != -1) {
						node.setNumberOfElements(numberOfElements);
						
						List<NodeData> args = node.getFunctionArguments();
						for(int i = 0; i < args.size(); i++) {
							args.get(i).setType("char");
						}
					} else {
						node.setType(firstChild.getType());
						node.setFunction(firstChild.isFunction());
						node.setArray(firstChild.isArray());
						node.setConstant(firstChild.isConstant());
					}
				} else if (children.size() == 3) {
					secondChild = children.get(1);
					check(secondChild);
					node.setNumberOfElements(secondChild.getNumberOfElements());
					node.setFunctionArguments(secondChild.getFunctionArguments());
				} break;
			case "<lista_izraza_pridruzivanja>":
				if(children.size() == 1) {
					check(firstChild);
					node.addFunctionArgument(firstChildData);
					node.setNumberOfElements(1);
				} else if(children.size() == 3) {
					GenerativeTreeNode thirdChild = children.get(2);
					check(firstChild);
					check(thirdChild);
					node.setFunctionArguments(firstChild.getFunctionArguments());
					node.addFunctionArgument(thirdChild.getData());
					node.setNumberOfElements(firstChild.getNumberOfElements() + 1);
				} break;
			default:
				error(node);
		}


	}

	/**
	 * This method checks if the given variable or function name is declared in any scope, from current to global and
	 * then returns appropriate data node if such declaration exists.
	 *
	 * @param name - variable or function name whose declaration should be checked for a data node
	 * @return local declaration data if the variable or function is declared in appropriate scopes, <code>null</code> otherwise
	 */
	private static NodeData getDeclaration(String name) {
		SymbolTableNode temp = currentScope;
		while(temp != null) {
			if(temp.hasLocalDeclaration(name)) {
				return temp.getLocalDeclarationData(name);
			}
			temp = temp.getParent();
		}

		return null;
	}
	
	private static void error(GenerativeTreeNode node) {
		StringBuilder errorBuilder = new StringBuilder();
		errorBuilder.append(node.getLexeme()).append(" ::= ");
		List<GenerativeTreeNode> children = node.getChildren();

		for(GenerativeTreeNode child : children) {
			if(child.getLexicalUnit().isEmpty()) {
				errorBuilder.append(child.getLexeme()).append(" ");
			} else {
				errorBuilder.append(String.format("%s(%d,%s) ", child.getLexeme(),
						child.getLineNumber(), child.getLexicalUnit()));
			}
		}

		System.out.println(errorBuilder);
		System.exit(1);
	}
	
	private static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException e) {
			if (input.startsWith("0x")) {
				try {
					Integer.parseInt(input.substring(3));
					return true;
				} catch (NumberFormatException exc) {
					return false;
				}
			}
			return false;
		}
	}
	
	private static boolean isCharacter(String input) {
		if(input.charAt(1) == '\\') {
			if (input.length() == 3) return false;
			char escapedChar = input.charAt(2);
			return escapedChar == 'n' || escapedChar == 't' || escapedChar == '0' || escapedChar == '\''
					|| escapedChar == '\"' || escapedChar == '\\';
		}

		return true;
	}
	
	private static boolean isString(String input) {
		for(int i = 1; i < input.length()-1; i++) {
			if (input.charAt(i) == '"') return false;
			if(input.charAt(i) == '\\') {
				char escapedChar = input.charAt(++i);
				if (escapedChar == '\"' && i >= input.length() - 1) return false;
				if(escapedChar != 'n' && escapedChar != 't' && escapedChar != '0' && escapedChar != '\''
						&& escapedChar != '\"' && escapedChar != '\\') return false;
			}
		}
		
		return true;
	}
	
	public static boolean canBeCast(String type1, String type2) {
		if (type1 == null || type2 == null) return false;
		if(type1.equals(type2)) return true;
		else return type1.equals("char") && type2.equals("int");
	}
	
	private static int sizeOfGeneratedString(GenerativeTreeNode node) {
		List<GenerativeTreeNode> children = node.getChildren();
		if (children.size() == 0) {
			if (!node.getLexeme().equals("NIZ_ZNAKOVA")) return -1;
			String lexicalUnit = node.getLexicalUnit();
			int lexicalUnitLength = lexicalUnit.length();
			int length = lexicalUnitLength - 2;
			for (int i = 1; i < lexicalUnitLength - 1; i++) {
				if (lexicalUnit.charAt(i) == '\\') {
					length--;
					if (lexicalUnit.charAt(i + 1) == '\\') i++;
				}
			}
			
			return length + 1;
			
		}
		else return sizeOfGeneratedString(children.get(0));
	}

	private static String findParentFunctionType(SymbolTableNode currentScope) {
		if (currentScope == null) return null;
		if (currentScope.getParentFunctionType() == null) return findParentFunctionType(currentScope.getParent());
		else return currentScope.getParentFunctionType();
	}

	private static String findParentFunctionName(SymbolTableNode currentScope) {
		if (currentScope == null) return null;
		if (currentScope.getParentFunctionName() == null || currentScope.getParentFunctionName().equals("") || currentScope.getParentFunctionName().equals("void")) return findParentFunctionName(currentScope.getParent());
		else return currentScope.getParentFunctionName();
	}
	
	private static boolean scopeInLoop(SymbolTableNode currentScope) {
		if (currentScope == null) return false;
		if (currentScope.isInLoop()) return true;
		else return scopeInLoop(currentScope.getParent());
	}

	private static String formatLine(String instruction, String label){
		StringBuilder spaceBuilder = new StringBuilder();

		int labelSize = label == null ? 0 : label.length();

		for (int i = 0; i < 16 - labelSize; i++) spaceBuilder.append(" ");

		return (label == null ? "\t\t\t\t" : label + spaceBuilder.toString()) + instruction + "\n";
	}
	
	private static String labelFromName(String name) {
		return "F_" + name.toUpperCase();
	}

	private static String formatLine(String instruction) {
		return formatLine(instruction, null);
	}

	private static void appendConstant(String constant, int number) {
		constantBuilder.append(formatLine("DW %D " + number, "C_" + labelIndex));
	}

	private static void appendConstant(String label) {
		constantBuilder.append(formatLine("DW %D 0", label));
	}
	
}
