package hr.fer.zemris.ppj.lab03;

import java.util.*;

/**
 * This is the main class for this lab exercise in which semantic analysis logic is implemented.
 * <p>
 * This class contains the <code>main</code> method which in this case takes no arguments from the command line.
 *
 * @author Pajser, Carli, Stena, Zuti
 */
public class SemantickiAnalizator {

	private static SymbolTableNode currentScope;
	private static List<Function> declaredNotDefinedFunctions = new ArrayList<>();
	private static List<Function> defined = new ArrayList<>();

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
						}
						break;
					case "BROJ":
						if(!isInteger(firstChild.getLexicalUnit())) error(node);
						node.setType("int");
						node.setlValue(false);
						break;
					case "ZNAK":
						if(!isCharacter(firstChild.getLexicalUnit())) error(node);
						node.setType("char");
						node.setlValue(false);
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
				} else if(firstLexeme.equals("<postfiks_izraz>")) {
					GenerativeTreeNode secondChild = children.get(1);
					GenerativeTreeNode thirdChild;
					String secondLexeme = secondChild.getLexeme();
					switch(secondLexeme) {
						case "L_UGL_ZAGRADA":
							check(firstChild);
							if(!firstChild.isArray()) error(node);
							thirdChild = children.get(2);
							check(thirdChild);
							if(!canBeCast(thirdChild.getType(), "int") || thirdChild.isArray()) error(node);
							node.setType(firstChild.getType());
							node.setArray(false);
							node.setFunctionArguments(firstChild.getFunctionArguments());
							node.setlValue(!firstChild.isConstant());
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
								check(firstChild);
								check(thirdChild);
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
						break;
					case "OP_INC":
					case "OP_DEC":
						secondChild = children.get(1);
						check(secondChild);
						if(!(secondChild.islValue() && canBeCast(secondChild.getType(), "int"))) error(node);
						node.setType("int");
						node.setArray(false);
						node.setlValue(false);
						break;
					case "<unarni_operator>":
						secondChild = children.get(1);
						check(secondChild);
						if(!canBeCast(secondChild.getType(), "int")) error(node);
						node.setType("int");
						node.setArray(false);
						node.setlValue(false);
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
				} else {
					GenerativeTreeNode thirdChild = children.get(2);
					check(firstChild);
					if(!canBeCast(firstChild.getType(), "int") || firstChild.isArray() || firstChild.isFunction()) error(node);
					check(thirdChild);
					if(!canBeCast(thirdChild.getType(), "int") || thirdChild.isArray() || thirdChild.isFunction()) error(node);
					node.setType("int");
					node.setlValue(false);
				} break;
			case "<izraz_pridruzivanja>":
				if(firstLexeme.equals("<log_ili_izraz>")) {
					check(firstChild);
					node.setType(firstChild.getType());
					node.setArray(firstChild.isArray());
					node.setFunction(firstChild.isFunction());
					node.setConstant(firstChild.isConstant());
					node.setlValue(firstChild.islValue());
				} else if(firstLexeme.equals("<postfiks_izraz>")) {
					GenerativeTreeNode thirdChild = children.get(2);
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
				} break;
			case "<izraz>":
				if(firstLexeme.equals("<izraz_pridruzivanja>")) {
					check(firstChild);
					node.setType(firstChild.getType());
					node.setArray(firstChild.isArray());
					node.setlValue(firstChild.islValue());
					node.setFunction(firstChild.isFunction());
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
				break;
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
					if(!canBeCast(secondChild.getType(), "int") || secondChild.isFunction() || secondChild.isArray()) error(node);
					check(children.get(4));
				} else if(children.size() == 7) {
					secondChild = children.get(2);
					check(secondChild);
					if(!canBeCast(secondChild.getType(), "int") || secondChild.isFunction() || secondChild.isArray()) error(node);
					check(children.get(4));
					check(children.get(6));
				} break;
			case "<naredba_petlje>":
				if(firstLexeme.equals("KR_WHILE")) {
					GenerativeTreeNode thirdChild = children.get(2);
					GenerativeTreeNode fifthChild = children.get(4);
					check(thirdChild);
					if(!canBeCast(thirdChild.getType(), "int")) error(node);
					fifthChild.setInLoop(true);
					check(fifthChild);
				} else if(firstLexeme.equals("KR_FOR")) {
					GenerativeTreeNode fourthChild = children.get(3);
					check(children.get(2));
					check(fourthChild);
					if(!canBeCast(fourthChild.getType(), "int") || fourthChild.isFunction() || fourthChild.isArray()) error(node);
					if(children.size() == 7){
						GenerativeTreeNode seventhChild = children.get(6);
						check(children.get(4));
						seventhChild.setInLoop(true);
						check(seventhChild);
					} else {
						GenerativeTreeNode sixthChild = children.get(5);
						sixthChild.setInLoop(true);
						check(sixthChild);
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
				for (NodeData argument : arguments) argument.setlValue(true);
				node.setFunctionArguments(fourthChild.getFunctionArguments());
				Function function = new Function(node.getType(), node.isArray(), functionName, arguments);
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
					check(firstChild);
					if(firstChild.isConstant()) error(node);
				} else {
					GenerativeTreeNode thirdChild = children.get(2);
					firstChild.setType(node.getType());
					firstChild.setArray(node.isArray());
					firstChild.setFunction(node.isFunction());
					firstChild.setConstant(node.isConstant());
					check(firstChild);
					check(thirdChild);

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
				} break;
			case "<izravni_deklarator>":
				if(children.size() == 1) {
					if(node.getType().equals("void")) error(node);
					if(currentScope.getLocalDeclarationData(firstChild.getLexicalUnit()) != null) error(node);
					node.setlValue(true);
					currentScope.addLocalDeclaration(firstChild.getLexicalUnit(), node.getData());
				} else if(children.get(1).getLexeme().equals("L_UGL_ZAGRADA")) {
					if(node.getType().equals("void")) error(node);
					if(currentScope.getLocalDeclarationData(firstChild.getLexicalUnit()) != null) error(node);
					try {
						int value = Integer.parseInt(children.get(2).getLexicalUnit());
						if (!(value > 0 && value <= 1024)) error(node);
						node.setArray(true);
						node.setNumberOfElements(value);
						currentScope.addLocalDeclaration(firstChild.getLexicalUnit(), node.getData());
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

	private static boolean scopeInLoop(SymbolTableNode currentScope) {
		if (currentScope == null) return false;
		if (currentScope.isInLoop()) return true;
		else return scopeInLoop(currentScope.getParent());
	}

}
