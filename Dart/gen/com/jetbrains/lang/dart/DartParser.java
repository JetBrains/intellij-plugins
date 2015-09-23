// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartGeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class DartParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == ADDITIVE_EXPRESSION) {
      r = additiveExpression(b, 0);
    }
    else if (t == ADDITIVE_OPERATOR) {
      r = additiveOperator(b, 0);
    }
    else if (t == ARGUMENT_LIST) {
      r = argumentList(b, 0);
    }
    else if (t == ARGUMENTS) {
      r = arguments(b, 0);
    }
    else if (t == ARRAY_ACCESS_EXPRESSION) {
      r = arrayAccessExpression(b, 0);
    }
    else if (t == AS_EXPRESSION) {
      r = asExpression(b, 0);
    }
    else if (t == ASSERT_STATEMENT) {
      r = assertStatement(b, 0);
    }
    else if (t == ASSIGN_EXPRESSION) {
      r = assignExpression(b, 0);
    }
    else if (t == ASSIGNMENT_OPERATOR) {
      r = assignmentOperator(b, 0);
    }
    else if (t == AWAIT_EXPRESSION) {
      r = awaitExpression(b, 0);
    }
    else if (t == BITWISE_EXPRESSION) {
      r = bitwiseExpression(b, 0);
    }
    else if (t == BITWISE_OPERATOR) {
      r = bitwiseOperator(b, 0);
    }
    else if (t == BLOCK) {
      r = block(b, 0);
    }
    else if (t == BREAK_STATEMENT) {
      r = breakStatement(b, 0);
    }
    else if (t == CALL_EXPRESSION) {
      r = callExpression(b, 0);
    }
    else if (t == CASCADE_REFERENCE_EXPRESSION) {
      r = cascadeReferenceExpression(b, 0);
    }
    else if (t == CATCH_PART) {
      r = catchPart(b, 0);
    }
    else if (t == CLASS_BODY) {
      r = classBody(b, 0);
    }
    else if (t == CLASS_DEFINITION) {
      r = classDefinition(b, 0);
    }
    else if (t == CLASS_MEMBERS) {
      r = classMembers(b, 0);
    }
    else if (t == COMPARE_EXPRESSION) {
      r = compareExpression(b, 0);
    }
    else if (t == COMPONENT_NAME) {
      r = componentName(b, 0);
    }
    else if (t == CONTINUE_STATEMENT) {
      r = continueStatement(b, 0);
    }
    else if (t == DEFAULT_CASE) {
      r = defaultCase(b, 0);
    }
    else if (t == DEFAULT_FORMAL_NAMED_PARAMETER) {
      r = defaultFormalNamedParameter(b, 0);
    }
    else if (t == DO_WHILE_STATEMENT) {
      r = doWhileStatement(b, 0);
    }
    else if (t == ENUM_CONSTANT_DECLARATION) {
      r = enumConstantDeclaration(b, 0);
    }
    else if (t == ENUM_DEFINITION) {
      r = enumDefinition(b, 0);
    }
    else if (t == EQUALITY_OPERATOR) {
      r = equalityOperator(b, 0);
    }
    else if (t == EXPORT_STATEMENT) {
      r = exportStatement(b, 0);
    }
    else if (t == EXPRESSION) {
      r = expression(b, 0);
    }
    else if (t == EXPRESSION_LIST) {
      r = expressionList(b, 0);
    }
    else if (t == FACTORY_CONSTRUCTOR_DECLARATION) {
      r = factoryConstructorDeclaration(b, 0);
    }
    else if (t == FIELD_FORMAL_PARAMETER) {
      r = fieldFormalParameter(b, 0);
    }
    else if (t == FIELD_INITIALIZER) {
      r = fieldInitializer(b, 0);
    }
    else if (t == FINALLY_PART) {
      r = finallyPart(b, 0);
    }
    else if (t == FOR_IN_PART) {
      r = forInPart(b, 0);
    }
    else if (t == FOR_LOOP_PARTS) {
      r = forLoopParts(b, 0);
    }
    else if (t == FOR_LOOP_PARTS_IN_BRACES) {
      r = forLoopPartsInBraces(b, 0);
    }
    else if (t == FOR_STATEMENT) {
      r = forStatement(b, 0);
    }
    else if (t == FORMAL_PARAMETER_LIST) {
      r = formalParameterList(b, 0);
    }
    else if (t == FUNCTION_BODY) {
      r = functionBody(b, 0);
    }
    else if (t == FUNCTION_DECLARATION_WITH_BODY) {
      r = functionDeclarationWithBody(b, 0);
    }
    else if (t == FUNCTION_DECLARATION_WITH_BODY_OR_NATIVE) {
      r = functionDeclarationWithBodyOrNative(b, 0);
    }
    else if (t == FUNCTION_EXPRESSION) {
      r = functionExpression(b, 0);
    }
    else if (t == FUNCTION_EXPRESSION_BODY) {
      r = functionExpressionBody(b, 0);
    }
    else if (t == FUNCTION_SIGNATURE) {
      r = functionSignature(b, 0);
    }
    else if (t == FUNCTION_TYPE_ALIAS) {
      r = functionTypeAlias(b, 0);
    }
    else if (t == GETTER_DECLARATION) {
      r = getterDeclaration(b, 0);
    }
    else if (t == HIDE_COMBINATOR) {
      r = hideCombinator(b, 0);
    }
    else if (t == ID) {
      r = id(b, 0);
    }
    else if (t == IF_NULL_EXPRESSION) {
      r = ifNullExpression(b, 0);
    }
    else if (t == IF_STATEMENT) {
      r = ifStatement(b, 0);
    }
    else if (t == IMPORT_STATEMENT) {
      r = importStatement(b, 0);
    }
    else if (t == INCOMPLETE_DECLARATION) {
      r = incompleteDeclaration(b, 0);
    }
    else if (t == INITIALIZERS) {
      r = initializers(b, 0);
    }
    else if (t == INTERFACES) {
      r = interfaces(b, 0);
    }
    else if (t == IS_EXPRESSION) {
      r = isExpression(b, 0);
    }
    else if (t == LABEL) {
      r = label(b, 0);
    }
    else if (t == LIBRARY_COMPONENT_REFERENCE_EXPRESSION) {
      r = libraryComponentReferenceExpression(b, 0);
    }
    else if (t == LIBRARY_ID) {
      r = libraryId(b, 0);
    }
    else if (t == LIBRARY_NAME_ELEMENT) {
      r = libraryNameElement(b, 0);
    }
    else if (t == LIBRARY_REFERENCE_LIST) {
      r = libraryReferenceList(b, 0);
    }
    else if (t == LIBRARY_STATEMENT) {
      r = libraryStatement(b, 0);
    }
    else if (t == LIST_LITERAL_EXPRESSION) {
      r = listLiteralExpression(b, 0);
    }
    else if (t == LITERAL_EXPRESSION) {
      r = literalExpression(b, 0);
    }
    else if (t == LOGIC_AND_EXPRESSION) {
      r = logicAndExpression(b, 0);
    }
    else if (t == LOGIC_OR_EXPRESSION) {
      r = logicOrExpression(b, 0);
    }
    else if (t == LONG_TEMPLATE_ENTRY) {
      r = longTemplateEntry(b, 0);
    }
    else if (t == MAP_LITERAL_ENTRY) {
      r = mapLiteralEntry(b, 0);
    }
    else if (t == MAP_LITERAL_EXPRESSION) {
      r = mapLiteralExpression(b, 0);
    }
    else if (t == METADATA) {
      r = metadata(b, 0);
    }
    else if (t == METHOD_DECLARATION) {
      r = methodDeclaration(b, 0);
    }
    else if (t == MIXIN_APPLICATION) {
      r = mixinApplication(b, 0);
    }
    else if (t == MIXINS) {
      r = mixins(b, 0);
    }
    else if (t == MULTIPLICATIVE_EXPRESSION) {
      r = multiplicativeExpression(b, 0);
    }
    else if (t == MULTIPLICATIVE_OPERATOR) {
      r = multiplicativeOperator(b, 0);
    }
    else if (t == NAMED_ARGUMENT) {
      r = namedArgument(b, 0);
    }
    else if (t == NAMED_CONSTRUCTOR_DECLARATION) {
      r = namedConstructorDeclaration(b, 0);
    }
    else if (t == NAMED_FORMAL_PARAMETERS) {
      r = namedFormalParameters(b, 0);
    }
    else if (t == NEW_EXPRESSION) {
      r = newExpression(b, 0);
    }
    else if (t == NORMAL_FORMAL_PARAMETER) {
      r = normalFormalParameter(b, 0);
    }
    else if (t == ON_PART) {
      r = onPart(b, 0);
    }
    else if (t == OPERATOR_DECLARATION) {
      r = operatorDeclaration(b, 0);
    }
    else if (t == PARAMETER_NAME_REFERENCE_EXPRESSION) {
      r = parameterNameReferenceExpression(b, 0);
    }
    else if (t == PARENTHESIZED_EXPRESSION) {
      r = parenthesizedExpression(b, 0);
    }
    else if (t == PART_OF_STATEMENT) {
      r = partOfStatement(b, 0);
    }
    else if (t == PART_STATEMENT) {
      r = partStatement(b, 0);
    }
    else if (t == PREFIX_EXPRESSION) {
      r = prefixExpression(b, 0);
    }
    else if (t == PREFIX_OPERATOR) {
      r = prefixOperator(b, 0);
    }
    else if (t == REDIRECTION) {
      r = redirection(b, 0);
    }
    else if (t == REFERENCE_EXPRESSION) {
      r = referenceExpression(b, 0);
    }
    else if (t == RELATIONAL_OPERATOR) {
      r = relationalOperator(b, 0);
    }
    else if (t == RETHROW_STATEMENT) {
      r = rethrowStatement(b, 0);
    }
    else if (t == RETURN_STATEMENT) {
      r = returnStatement(b, 0);
    }
    else if (t == RETURN_TYPE) {
      r = returnType(b, 0);
    }
    else if (t == SETTER_DECLARATION) {
      r = setterDeclaration(b, 0);
    }
    else if (t == SHIFT_EXPRESSION) {
      r = shiftExpression(b, 0);
    }
    else if (t == SHIFT_OPERATOR) {
      r = shiftOperator(b, 0);
    }
    else if (t == SHORT_TEMPLATE_ENTRY) {
      r = shortTemplateEntry(b, 0);
    }
    else if (t == SHOW_COMBINATOR) {
      r = showCombinator(b, 0);
    }
    else if (t == SIMPLE_FORMAL_PARAMETER) {
      r = simpleFormalParameter(b, 0);
    }
    else if (t == STATEMENTS) {
      r = statements(b, 0);
    }
    else if (t == STRING_LITERAL_EXPRESSION) {
      r = stringLiteralExpression(b, 0);
    }
    else if (t == SUFFIX_EXPRESSION) {
      r = suffixExpression(b, 0);
    }
    else if (t == SUPER_CALL_OR_FIELD_INITIALIZER) {
      r = superCallOrFieldInitializer(b, 0);
    }
    else if (t == SUPER_EXPRESSION) {
      r = superExpression(b, 0);
    }
    else if (t == SUPERCLASS) {
      r = superclass(b, 0);
    }
    else if (t == SWITCH_CASE) {
      r = switchCase(b, 0);
    }
    else if (t == SWITCH_STATEMENT) {
      r = switchStatement(b, 0);
    }
    else if (t == SYMBOL_LITERAL_EXPRESSION) {
      r = symbolLiteralExpression(b, 0);
    }
    else if (t == TERNARY_EXPRESSION) {
      r = ternaryExpression(b, 0);
    }
    else if (t == THIS_EXPRESSION) {
      r = thisExpression(b, 0);
    }
    else if (t == THROW_EXPRESSION) {
      r = throwExpression(b, 0);
    }
    else if (t == TRY_STATEMENT) {
      r = tryStatement(b, 0);
    }
    else if (t == TYPE) {
      r = type(b, 0);
    }
    else if (t == TYPE_ARGUMENTS) {
      r = typeArguments(b, 0);
    }
    else if (t == TYPE_LIST) {
      r = typeList(b, 0);
    }
    else if (t == TYPE_PARAMETER) {
      r = typeParameter(b, 0);
    }
    else if (t == TYPE_PARAMETERS) {
      r = typeParameters(b, 0);
    }
    else if (t == URI_ELEMENT) {
      r = uriElement(b, 0);
    }
    else if (t == USER_DEFINABLE_OPERATOR) {
      r = userDefinableOperator(b, 0);
    }
    else if (t == VALUE_EXPRESSION) {
      r = valueExpression(b, 0);
    }
    else if (t == VAR_ACCESS_DECLARATION) {
      r = varAccessDeclaration(b, 0);
    }
    else if (t == VAR_DECLARATION_LIST) {
      r = varDeclarationList(b, 0);
    }
    else if (t == VAR_DECLARATION_LIST_PART) {
      r = varDeclarationListPart(b, 0);
    }
    else if (t == VAR_INIT) {
      r = varInit(b, 0);
    }
    else if (t == WHILE_STATEMENT) {
      r = whileStatement(b, 0);
    }
    else if (t == YIELD_EACH_STATEMENT) {
      r = yieldEachStatement(b, 0);
    }
    else if (t == YIELD_STATEMENT) {
      r = yieldStatement(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return dartUnit(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(ADDITIVE_EXPRESSION, ARRAY_ACCESS_EXPRESSION, ASSIGN_EXPRESSION, AS_EXPRESSION,
      AWAIT_EXPRESSION, BITWISE_EXPRESSION, CALL_EXPRESSION, CASCADE_REFERENCE_EXPRESSION,
      COMPARE_EXPRESSION, EXPRESSION, FUNCTION_EXPRESSION, IF_NULL_EXPRESSION,
      IS_EXPRESSION, LIBRARY_COMPONENT_REFERENCE_EXPRESSION, LIST_LITERAL_EXPRESSION, LITERAL_EXPRESSION,
      LOGIC_AND_EXPRESSION, LOGIC_OR_EXPRESSION, MAP_LITERAL_EXPRESSION, MULTIPLICATIVE_EXPRESSION,
      NEW_EXPRESSION, PARAMETER_NAME_REFERENCE_EXPRESSION, PARENTHESIZED_EXPRESSION, PREFIX_EXPRESSION,
      REFERENCE_EXPRESSION, SHIFT_EXPRESSION, STRING_LITERAL_EXPRESSION, SUFFIX_EXPRESSION,
      SUPER_EXPRESSION, SYMBOL_LITERAL_EXPRESSION, TERNARY_EXPRESSION, THIS_EXPRESSION,
      THROW_EXPRESSION, VALUE_EXPRESSION),
  };

  /* ********************************************************** */
  // additiveOperator multiplicativeExpressionWrapper
  public static boolean additiveExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpression")) return false;
    if (!nextTokenIs(b, "<additive expression>", PLUS, MINUS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, "<additive expression>");
    r = additiveOperator(b, l + 1);
    r = r && multiplicativeExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, ADDITIVE_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // multiplicativeExpressionWrapper additiveExpression*
  static boolean additiveExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = multiplicativeExpressionWrapper(b, l + 1);
    r = r && additiveExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // additiveExpression*
  private static boolean additiveExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpressionWrapper_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!additiveExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "additiveExpressionWrapper_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '+' | '-'
  public static boolean additiveOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveOperator")) return false;
    if (!nextTokenIs(b, "<additive operator>", PLUS, MINUS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<additive operator>");
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    exit_section_(b, l, m, ADDITIVE_OPERATOR, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // argumentListPart (',' argumentListPart)*
  public static boolean argumentList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<argument list>");
    r = argumentListPart(b, l + 1);
    r = r && argumentList_1(b, l + 1);
    exit_section_(b, l, m, ARGUMENT_LIST, r, false, argument_list_recover_parser_);
    return r;
  }

  // (',' argumentListPart)*
  private static boolean argumentList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!argumentList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "argumentList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' argumentListPart
  private static boolean argumentList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && argumentListPart(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // namedArgument | expression
  static boolean argumentListPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentListPart")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = namedArgument(b, l + 1);
    if (!r) r = expression(b, l + 1);
    exit_section_(b, l, m, null, r, false, argument_list_part_recover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(')' | ',')
  static boolean argument_list_part_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_part_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !argument_list_part_recover_0(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  // ')' | ','
  private static boolean argument_list_part_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_part_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(')')
  static boolean argument_list_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !argument_list_recover_0(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  // (')')
  private static boolean argument_list_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '(' argumentList? ')'
  public static boolean arguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && arguments_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, ARGUMENTS, r);
    return r;
  }

  // argumentList?
  private static boolean arguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments_1")) return false;
    argumentList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '[' expression? ']'
  static boolean arrayAccess(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccess")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, LBRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, arrayAccess_1(b, l + 1));
    r = p && consumeToken(b, RBRACKET) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // expression?
  private static boolean arrayAccess_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccess_1")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // arrayAccess
  public static boolean arrayAccessExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccessExpression")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, null);
    r = arrayAccess(b, l + 1);
    exit_section_(b, l, m, ARRAY_ACCESS_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ('async' '*'? | 'sync' '*'?)? '=>' expression
  static boolean arrowBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody")) return false;
    if (!nextTokenIs(b, "", EXPRESSION_BODY_DEF, ASYNC, SYNC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = arrowBody_0(b, l + 1);
    r = r && consumeToken(b, EXPRESSION_BODY_DEF);
    p = r; // pin = 2
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // ('async' '*'? | 'sync' '*'?)?
  private static boolean arrowBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0")) return false;
    arrowBody_0_0(b, l + 1);
    return true;
  }

  // 'async' '*'? | 'sync' '*'?
  private static boolean arrowBody_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arrowBody_0_0_0(b, l + 1);
    if (!r) r = arrowBody_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'async' '*'?
  private static boolean arrowBody_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASYNC);
    r = r && arrowBody_0_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '*'?
  private static boolean arrowBody_0_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0_0_0_1")) return false;
    consumeToken(b, MUL);
    return true;
  }

  // 'sync' '*'?
  private static boolean arrowBody_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SYNC);
    r = r && arrowBody_0_0_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '*'?
  private static boolean arrowBody_0_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0_0_1_1")) return false;
    consumeToken(b, MUL);
    return true;
  }

  /* ********************************************************** */
  // <<arrowBodyWrapper>> ';'
  static boolean arrowBodyWithSemi(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBodyWithSemi")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = arrowBodyWrapper(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'as' type
  public static boolean asExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "asExpression")) return false;
    if (!nextTokenIs(b, AS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, null);
    r = consumeToken(b, AS);
    r = r && type(b, l + 1);
    exit_section_(b, l, m, AS_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'assert' '(' expressionWithRecoverUntilParen ')' ';'
  public static boolean assertStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assertStatement")) return false;
    if (!nextTokenIs(b, ASSERT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, ASSERT);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, LPAREN));
    r = p && report_error_(b, expressionWithRecoverUntilParen(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, ASSERT_STATEMENT, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // assignmentOperator ternaryExpressionWrapper
  public static boolean assignExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignExpression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _LEFT_, "<assign expression>");
    r = assignmentOperator(b, l + 1);
    p = r; // pin = 1
    r = r && ternaryExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, ASSIGN_EXPRESSION, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ternaryExpressionWrapper assignExpression*
  static boolean assignExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ternaryExpressionWrapper(b, l + 1);
    r = r && assignExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // assignExpression*
  private static boolean assignExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignExpressionWrapper_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!assignExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "assignExpressionWrapper_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '=' | '*=' | '/=' | '~/=' | '%=' | '+=' | '-=' | '<<=' | <<gtGtEq>> | '&=' | '^=' | '|=' | '??='
  public static boolean assignmentOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<assignment operator>");
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, MUL_EQ);
    if (!r) r = consumeToken(b, DIV_EQ);
    if (!r) r = consumeToken(b, INT_DIV_EQ);
    if (!r) r = consumeToken(b, REM_EQ);
    if (!r) r = consumeToken(b, PLUS_EQ);
    if (!r) r = consumeToken(b, MINUS_EQ);
    if (!r) r = consumeToken(b, LT_LT_EQ);
    if (!r) r = gtGtEq(b, l + 1);
    if (!r) r = consumeToken(b, AND_EQ);
    if (!r) r = consumeToken(b, XOR_EQ);
    if (!r) r = consumeToken(b, OR_EQ);
    if (!r) r = consumeToken(b, QUEST_QUEST_EQ);
    exit_section_(b, l, m, ASSIGNMENT_OPERATOR, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<isInsideSyncOrAsyncFunction>> 'await' prefixExpression
  public static boolean awaitExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<await expression>");
    r = isInsideSyncOrAsyncFunction(b, l + 1);
    r = r && consumeToken(b, AWAIT);
    r = r && prefixExpression(b, l + 1);
    exit_section_(b, l, m, AWAIT_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // multiplicativeOperator |
  //                            additiveOperator |
  //                            shiftOperator|
  //                            relationalOperator|
  //                            '==' |
  //                            bitwiseOperator
  static boolean binaryOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binaryOperator")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = multiplicativeOperator(b, l + 1);
    if (!r) r = additiveOperator(b, l + 1);
    if (!r) r = shiftOperator(b, l + 1);
    if (!r) r = relationalOperator(b, l + 1);
    if (!r) r = consumeToken(b, EQ_EQ);
    if (!r) r = bitwiseOperator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // bitwiseOperator shiftExpressionWrapper
  public static boolean bitwiseExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseExpression")) return false;
    if (!nextTokenIs(b, "<bitwise expression>", AND, XOR, OR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, "<bitwise expression>");
    r = bitwiseOperator(b, l + 1);
    r = r && shiftExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, BITWISE_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // shiftExpressionWrapper bitwiseExpression*
  static boolean bitwiseExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = shiftExpressionWrapper(b, l + 1);
    r = r && bitwiseExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // bitwiseExpression*
  private static boolean bitwiseExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseExpressionWrapper_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!bitwiseExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "bitwiseExpressionWrapper_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '&' | '^' | '|'
  public static boolean bitwiseOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseOperator")) return false;
    if (!nextTokenIs(b, "<bitwise operator>", AND, XOR, OR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<bitwise operator>");
    r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, XOR);
    if (!r) r = consumeToken(b, OR);
    exit_section_(b, l, m, BITWISE_OPERATOR, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '{' statements '}'
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, statements(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, BLOCK, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ('async' '*'? | 'sync' '*'?)? block
  static boolean blockBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody")) return false;
    if (!nextTokenIs(b, "", ASYNC, SYNC, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockBody_0(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('async' '*'? | 'sync' '*'?)?
  private static boolean blockBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0")) return false;
    blockBody_0_0(b, l + 1);
    return true;
  }

  // 'async' '*'? | 'sync' '*'?
  private static boolean blockBody_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockBody_0_0_0(b, l + 1);
    if (!r) r = blockBody_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'async' '*'?
  private static boolean blockBody_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASYNC);
    r = r && blockBody_0_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '*'?
  private static boolean blockBody_0_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0_0_0_1")) return false;
    consumeToken(b, MUL);
    return true;
  }

  // 'sync' '*'?
  private static boolean blockBody_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SYNC);
    r = r && blockBody_0_0_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '*'?
  private static boolean blockBody_0_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0_0_1_1")) return false;
    consumeToken(b, MUL);
    return true;
  }

  /* ********************************************************** */
  // 'break' referenceExpression? ';'
  public static boolean breakStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "breakStatement")) return false;
    if (!nextTokenIs(b, BREAK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, BREAK);
    p = r; // pin = 1
    r = r && report_error_(b, breakStatement_1(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, BREAK_STATEMENT, r, p, null);
    return r || p;
  }

  // referenceExpression?
  private static boolean breakStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "breakStatement_1")) return false;
    referenceExpression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // arguments
  public static boolean callExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callExpression")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, null);
    r = arguments(b, l + 1);
    exit_section_(b, l, m, CALL_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (callExpression | arrayAccessExpression | qualifiedReferenceExpression)*
  static boolean callOrArrayAccessOrQualifiedRefExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callOrArrayAccessOrQualifiedRefExpression")) return false;
    int c = current_position_(b);
    while (true) {
      if (!callOrArrayAccessOrQualifiedRefExpression_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "callOrArrayAccessOrQualifiedRefExpression", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // callExpression | arrayAccessExpression | qualifiedReferenceExpression
  private static boolean callOrArrayAccessOrQualifiedRefExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callOrArrayAccessOrQualifiedRefExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = callExpression(b, l + 1);
    if (!r) r = arrayAccessExpression(b, l + 1);
    if (!r) r = qualifiedReferenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '..' << cascadeStopper >> (arrayAccess | refOrThisOrSuperOrParenExpression callOrArrayAccessOrQualifiedRefExpression) << varInitWrapper >>
  public static boolean cascadeReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cascadeReferenceExpression")) return false;
    if (!nextTokenIs(b, DOT_DOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT_DOT);
    r = r && cascadeStopper(b, l + 1);
    r = r && cascadeReferenceExpression_2(b, l + 1);
    r = r && varInitWrapper(b, l + 1);
    exit_section_(b, m, CASCADE_REFERENCE_EXPRESSION, r);
    return r;
  }

  // arrayAccess | refOrThisOrSuperOrParenExpression callOrArrayAccessOrQualifiedRefExpression
  private static boolean cascadeReferenceExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cascadeReferenceExpression_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arrayAccess(b, l + 1);
    if (!r) r = cascadeReferenceExpression_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // refOrThisOrSuperOrParenExpression callOrArrayAccessOrQualifiedRefExpression
  private static boolean cascadeReferenceExpression_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cascadeReferenceExpression_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = refOrThisOrSuperOrParenExpression(b, l + 1);
    r = r && callOrArrayAccessOrQualifiedRefExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'catch' '(' componentName (',' componentName)? ')'
  public static boolean catchPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchPart")) return false;
    if (!nextTokenIs(b, CATCH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, CATCH);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, LPAREN));
    r = p && report_error_(b, componentName(b, l + 1)) && r;
    r = p && report_error_(b, catchPart_3(b, l + 1)) && r;
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, CATCH_PART, r, p, null);
    return r || p;
  }

  // (',' componentName)?
  private static boolean catchPart_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchPart_3")) return false;
    catchPart_3_0(b, l + 1);
    return true;
  }

  // ',' componentName
  private static boolean catchPart_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchPart_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '{' classMembers '}'
  public static boolean classBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classBody")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, classMembers(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, CLASS_BODY, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // metadata* 'abstract'? 'class' componentName typeParameters? (mixinApplication | standardClassDeclarationTail)
  public static boolean classDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDefinition")) return false;
    if (!nextTokenIs(b, "<class definition>", AT, ABSTRACT, CLASS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<class definition>");
    r = classDefinition_0(b, l + 1);
    r = r && classDefinition_1(b, l + 1);
    r = r && consumeToken(b, CLASS);
    r = r && componentName(b, l + 1);
    p = r; // pin = 4
    r = r && report_error_(b, classDefinition_4(b, l + 1));
    r = p && classDefinition_5(b, l + 1) && r;
    exit_section_(b, l, m, CLASS_DEFINITION, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean classDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDefinition_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "classDefinition_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'abstract'?
  private static boolean classDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDefinition_1")) return false;
    consumeToken(b, ABSTRACT);
    return true;
  }

  // typeParameters?
  private static boolean classDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDefinition_4")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // mixinApplication | standardClassDeclarationTail
  private static boolean classDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDefinition_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mixinApplication(b, l + 1);
    if (!r) r = standardClassDeclarationTail(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // factoryConstructorDeclaration
  //                                 | namedConstructorDeclaration
  //                                 | operatorDeclaration
  //                                 | getterOrSetterDeclaration
  //                                 | methodDeclaration
  //                                 | varDeclarationListWithSemicolon
  //                                 | incompleteDeclaration
  static boolean classMemberDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMemberDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = factoryConstructorDeclaration(b, l + 1);
    if (!r) r = namedConstructorDeclaration(b, l + 1);
    if (!r) r = operatorDeclaration(b, l + 1);
    if (!r) r = getterOrSetterDeclaration(b, l + 1);
    if (!r) r = methodDeclaration(b, l + 1);
    if (!r) r = varDeclarationListWithSemicolon(b, l + 1);
    if (!r) r = incompleteDeclaration(b, l + 1);
    exit_section_(b, l, m, null, r, false, class_member_recover_parser_);
    return r;
  }

  /* ********************************************************** */
  // classMemberDefinition*
  public static boolean classMembers(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMembers")) return false;
    Marker m = enter_section_(b, l, _NONE_, "<class members>");
    int c = current_position_(b);
    while (true) {
      if (!classMemberDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "classMembers", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, CLASS_MEMBERS, true, false, simple_scope_recover_parser_);
    return true;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> | '@' | 'abstract' | 'class' | 'const' | 'export' | 'external' | 'factory' | 'final' | 'get' | 'import' | 'library' | 'operator' | 'part' | 'set' | 'static' | 'typedef' | 'var' | 'void' | '}' )
  static boolean class_member_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_member_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !class_member_recover_0(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  // <<nonStrictID>> | '@' | 'abstract' | 'class' | 'const' | 'export' | 'external' | 'factory' | 'final' | 'get' | 'import' | 'library' | 'operator' | 'part' | 'set' | 'static' | 'typedef' | 'var' | 'void' | '}'
  private static boolean class_member_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_member_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FACTORY);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, OPERATOR);
    if (!r) r = consumeToken(b, PART);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, TYPEDEF);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    if (!r) r = consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // showCombinator | hideCombinator
  static boolean combinator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "combinator")) return false;
    if (!nextTokenIs(b, "", HIDE, SHOW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = showCombinator(b, l + 1);
    if (!r) r = hideCombinator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (relationalOperator | equalityOperator) bitwiseExpressionWrapper
  public static boolean compareExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compareExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, "<compare expression>");
    r = compareExpression_0(b, l + 1);
    r = r && bitwiseExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, COMPARE_EXPRESSION, r, false, null);
    return r;
  }

  // relationalOperator | equalityOperator
  private static boolean compareExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compareExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = relationalOperator(b, l + 1);
    if (!r) r = equalityOperator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // bitwiseExpressionWrapper compareExpression*
  static boolean compareExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compareExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = bitwiseExpressionWrapper(b, l + 1);
    r = r && compareExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // compareExpression*
  private static boolean compareExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compareExpressionWrapper_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!compareExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "compareExpressionWrapper_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean componentName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "componentName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<component name>");
    r = nonStrictID(b, l + 1);
    exit_section_(b, l, m, COMPONENT_NAME, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'continue' referenceExpression? ';'
  public static boolean continueStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "continueStatement")) return false;
    if (!nextTokenIs(b, CONTINUE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, CONTINUE);
    p = r; // pin = 1
    r = r && report_error_(b, continueStatement_1(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, CONTINUE_STATEMENT, r, p, null);
    return r || p;
  }

  // referenceExpression?
  private static boolean continueStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "continueStatement_1")) return false;
    referenceExpression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // topLevelDefinition*
  static boolean dartUnit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dartUnit")) return false;
    int c = current_position_(b);
    while (true) {
      if (!topLevelDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "dartUnit", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* finalConstVarOrTypeAndComponentName
  static boolean declaredIdentifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declaredIdentifier")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = declaredIdentifier_0(b, l + 1);
    r = r && finalConstVarOrTypeAndComponentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean declaredIdentifier_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declaredIdentifier_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "declaredIdentifier_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // label* 'default' ':' statements
  public static boolean defaultCase(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultCase")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<default case>");
    r = defaultCase_0(b, l + 1);
    r = r && consumeToken(b, DEFAULT);
    p = r; // pin = 2
    r = r && report_error_(b, consumeToken(b, COLON));
    r = p && statements(b, l + 1) && r;
    exit_section_(b, l, m, DEFAULT_CASE, r, p, null);
    return r || p;
  }

  // label*
  private static boolean defaultCase_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultCase_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!label(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "defaultCase_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // normalFormalParameter (('=' | ':') expression)?
  public static boolean defaultFormalNamedParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultFormalNamedParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<default formal named parameter>");
    r = normalFormalParameter(b, l + 1);
    r = r && defaultFormalNamedParameter_1(b, l + 1);
    exit_section_(b, l, m, DEFAULT_FORMAL_NAMED_PARAMETER, r, false, default_formal_parameter_recover_parser_);
    return r;
  }

  // (('=' | ':') expression)?
  private static boolean defaultFormalNamedParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultFormalNamedParameter_1")) return false;
    defaultFormalNamedParameter_1_0(b, l + 1);
    return true;
  }

  // ('=' | ':') expression
  private static boolean defaultFormalNamedParameter_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultFormalNamedParameter_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = defaultFormalNamedParameter_1_0_0(b, l + 1);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '=' | ':'
  private static boolean defaultFormalNamedParameter_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultFormalNamedParameter_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(')' | ',' | ']' | '}')
  static boolean default_formal_parameter_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_formal_parameter_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !default_formal_parameter_recover_0(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  // ')' | ',' | ']' | '}'
  private static boolean default_formal_parameter_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_formal_parameter_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, RBRACKET);
    if (!r) r = consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'do' statement 'while' '(' expressionWithRecoverUntilParen ')' ';'
  public static boolean doWhileStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doWhileStatement")) return false;
    if (!nextTokenIs(b, DO)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, DO);
    p = r; // pin = 1
    r = r && report_error_(b, statement(b, l + 1));
    r = p && report_error_(b, consumeToken(b, WHILE)) && r;
    r = p && report_error_(b, consumeToken(b, LPAREN)) && r;
    r = p && report_error_(b, expressionWithRecoverUntilParen(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, DO_WHILE_STATEMENT, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // componentName
  public static boolean enumConstantDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumConstantDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<enum constant declaration>");
    r = componentName(b, l + 1);
    exit_section_(b, l, m, ENUM_CONSTANT_DECLARATION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'enum' componentName '{' enumConstantDeclaration (',' enumConstantDeclaration)* ','? '}'
  public static boolean enumDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition")) return false;
    if (!nextTokenIs(b, "<enum definition>", AT, ENUM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<enum definition>");
    r = enumDefinition_0(b, l + 1);
    r = r && consumeToken(b, ENUM);
    r = r && componentName(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, consumeToken(b, LBRACE));
    r = p && report_error_(b, enumConstantDeclaration(b, l + 1)) && r;
    r = p && report_error_(b, enumDefinition_5(b, l + 1)) && r;
    r = p && report_error_(b, enumDefinition_6(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, ENUM_DEFINITION, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean enumDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDefinition_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (',' enumConstantDeclaration)*
  private static boolean enumDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_5")) return false;
    int c = current_position_(b);
    while (true) {
      if (!enumDefinition_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDefinition_5", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' enumConstantDeclaration
  private static boolean enumDefinition_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && enumConstantDeclaration(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean enumDefinition_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_6")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // '==' | '!='
  public static boolean equalityOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityOperator")) return false;
    if (!nextTokenIs(b, "<equality operator>", NEQ, EQ_EQ)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<equality operator>");
    r = consumeToken(b, EQ_EQ);
    if (!r) r = consumeToken(b, NEQ);
    exit_section_(b, l, m, EQUALITY_OPERATOR, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'export' uriElement combinator* ';'
  public static boolean exportStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportStatement")) return false;
    if (!nextTokenIs(b, "<export statement>", AT, EXPORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<export statement>");
    r = exportStatement_0(b, l + 1);
    r = r && consumeToken(b, EXPORT);
    r = r && uriElement(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, exportStatement_3(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, EXPORT_STATEMENT, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean exportStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportStatement_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exportStatement_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // combinator*
  private static boolean exportStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportStatement_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!combinator(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exportStatement_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // assignExpressionWrapper
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<expression>");
    r = assignExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, EXPRESSION, r, false, expression_recover_parser_);
    return r;
  }

  /* ********************************************************** */
  // expression | statement
  static boolean expressionInParentheses(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionInParentheses")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = expression(b, l + 1);
    if (!r) r = statement(b, l + 1);
    exit_section_(b, l, m, null, r, false, parenthesesRecovery_parser_);
    return r;
  }

  /* ********************************************************** */
  // expression (',' expression)*
  public static boolean expressionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<expression list>");
    r = expression(b, l + 1);
    r = r && expressionList_1(b, l + 1);
    exit_section_(b, l, m, EXPRESSION_LIST, r, false, null);
    return r;
  }

  // (',' expression)*
  private static boolean expressionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!expressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expressionList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' expression
  private static boolean expressionList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expression
  static boolean expressionWithRecoverUntilParen(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionWithRecoverUntilParen")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = expression(b, l + 1);
    exit_section_(b, l, m, null, r, false, not_paren_recover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> | <<parenthesizedExpressionWrapper>> | '!' | '!=' | '%' | '%=' |
  //                                  '&&' | '&' | '&=' | '(' | ')' | '*' | '*=' | '+' | '++' | '+=' | ',' | '-' | '--' | '-=' | '.' | '..' | '/' |
  //                                  '/=' | ':' | ';' | '<' | '<<' | '<<=' | '<=' | '=' | '==' | '=>' | '>' | <<gtGt>> | <<gtEq>> | <<gtGtEq>> |
  //                                  '@' | '[' | ']' | '^' | '^=' | '?.' | '??=' | '??' | '?' |
  //                                  'abstract' | 'as' | 'assert' | 'async' | 'break' | 'case' | 'catch' | 'class' | 'const' |
  //                                  'continue' | 'default' | 'deferred' | 'do' | 'else' | 'export' | 'external' | 'factory' | 'final' | 'finally' | 'for' |
  //                                  'get' | 'hide' | 'if' | 'import' | 'is' | 'library' | 'native' | 'new' | 'on' | 'operator' | 'part' |
  //                                  'rethrow' | 'return' | 'set' | 'show' | 'static' | 'super' | 'switch' | 'sync' | 'this' | 'throw' | 'try' |
  //                                  'typedef' | 'var' | 'void' | 'while' | '{' | '|' | '|=' | '||' | '}' | '~' | '~/' | '~/=' | CLOSING_QUOTE |
  //                                   FALSE | HEX_NUMBER | LONG_TEMPLATE_ENTRY_END | LONG_TEMPLATE_ENTRY_START | NULL | NUMBER |
  //                                   OPEN_QUOTE | RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING | REGULAR_STRING_PART |
  //                                   SHORT_TEMPLATE_ENTRY_START | TRUE)
  static boolean expression_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !expression_recover_0(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  // <<nonStrictID>> | <<parenthesizedExpressionWrapper>> | '!' | '!=' | '%' | '%=' |
  //                                  '&&' | '&' | '&=' | '(' | ')' | '*' | '*=' | '+' | '++' | '+=' | ',' | '-' | '--' | '-=' | '.' | '..' | '/' |
  //                                  '/=' | ':' | ';' | '<' | '<<' | '<<=' | '<=' | '=' | '==' | '=>' | '>' | <<gtGt>> | <<gtEq>> | <<gtGtEq>> |
  //                                  '@' | '[' | ']' | '^' | '^=' | '?.' | '??=' | '??' | '?' |
  //                                  'abstract' | 'as' | 'assert' | 'async' | 'break' | 'case' | 'catch' | 'class' | 'const' |
  //                                  'continue' | 'default' | 'deferred' | 'do' | 'else' | 'export' | 'external' | 'factory' | 'final' | 'finally' | 'for' |
  //                                  'get' | 'hide' | 'if' | 'import' | 'is' | 'library' | 'native' | 'new' | 'on' | 'operator' | 'part' |
  //                                  'rethrow' | 'return' | 'set' | 'show' | 'static' | 'super' | 'switch' | 'sync' | 'this' | 'throw' | 'try' |
  //                                  'typedef' | 'var' | 'void' | 'while' | '{' | '|' | '|=' | '||' | '}' | '~' | '~/' | '~/=' | CLOSING_QUOTE |
  //                                   FALSE | HEX_NUMBER | LONG_TEMPLATE_ENTRY_END | LONG_TEMPLATE_ENTRY_START | NULL | NUMBER |
  //                                   OPEN_QUOTE | RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING | REGULAR_STRING_PART |
  //                                   SHORT_TEMPLATE_ENTRY_START | TRUE
  private static boolean expression_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    if (!r) r = parenthesizedExpressionWrapper(b, l + 1);
    if (!r) r = consumeToken(b, NOT);
    if (!r) r = consumeToken(b, NEQ);
    if (!r) r = consumeToken(b, REM);
    if (!r) r = consumeToken(b, REM_EQ);
    if (!r) r = consumeToken(b, AND_AND);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, AND_EQ);
    if (!r) r = consumeToken(b, LPAREN);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, MUL);
    if (!r) r = consumeToken(b, MUL_EQ);
    if (!r) r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, PLUS_PLUS);
    if (!r) r = consumeToken(b, PLUS_EQ);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, MINUS_MINUS);
    if (!r) r = consumeToken(b, MINUS_EQ);
    if (!r) r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, DOT_DOT);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, DIV_EQ);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, LT_LT);
    if (!r) r = consumeToken(b, LT_LT_EQ);
    if (!r) r = consumeToken(b, LT_EQ);
    if (!r) r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, EQ_EQ);
    if (!r) r = consumeToken(b, EXPRESSION_BODY_DEF);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = gtGt(b, l + 1);
    if (!r) r = gtEq(b, l + 1);
    if (!r) r = gtGtEq(b, l + 1);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, LBRACKET);
    if (!r) r = consumeToken(b, RBRACKET);
    if (!r) r = consumeToken(b, XOR);
    if (!r) r = consumeToken(b, XOR_EQ);
    if (!r) r = consumeToken(b, QUEST_DOT);
    if (!r) r = consumeToken(b, QUEST_QUEST_EQ);
    if (!r) r = consumeToken(b, QUEST_QUEST);
    if (!r) r = consumeToken(b, QUEST);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, AS);
    if (!r) r = consumeToken(b, ASSERT);
    if (!r) r = consumeToken(b, ASYNC);
    if (!r) r = consumeToken(b, BREAK);
    if (!r) r = consumeToken(b, CASE);
    if (!r) r = consumeToken(b, CATCH);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, DEFAULT);
    if (!r) r = consumeToken(b, DEFERRED);
    if (!r) r = consumeToken(b, DO);
    if (!r) r = consumeToken(b, ELSE);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FACTORY);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, FINALLY);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, HIDE);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, IS);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, NATIVE);
    if (!r) r = consumeToken(b, NEW);
    if (!r) r = consumeToken(b, ON);
    if (!r) r = consumeToken(b, OPERATOR);
    if (!r) r = consumeToken(b, PART);
    if (!r) r = consumeToken(b, RETHROW);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, SHOW);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, SUPER);
    if (!r) r = consumeToken(b, SWITCH);
    if (!r) r = consumeToken(b, SYNC);
    if (!r) r = consumeToken(b, THIS);
    if (!r) r = consumeToken(b, THROW);
    if (!r) r = consumeToken(b, TRY);
    if (!r) r = consumeToken(b, TYPEDEF);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, LBRACE);
    if (!r) r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, OR_EQ);
    if (!r) r = consumeToken(b, OR_OR);
    if (!r) r = consumeToken(b, RBRACE);
    if (!r) r = consumeToken(b, BIN_NOT);
    if (!r) r = consumeToken(b, INT_DIV);
    if (!r) r = consumeToken(b, INT_DIV_EQ);
    if (!r) r = consumeToken(b, CLOSING_QUOTE);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, HEX_NUMBER);
    if (!r) r = consumeToken(b, LONG_TEMPLATE_ENTRY_END);
    if (!r) r = consumeToken(b, LONG_TEMPLATE_ENTRY_START);
    if (!r) r = consumeToken(b, NULL);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, OPEN_QUOTE);
    if (!r) r = consumeToken(b, RAW_SINGLE_QUOTED_STRING);
    if (!r) r = consumeToken(b, RAW_TRIPLE_QUOTED_STRING);
    if (!r) r = consumeToken(b, REGULAR_STRING_PART);
    if (!r) r = consumeToken(b, SHORT_TEMPLATE_ENTRY_START);
    if (!r) r = consumeToken(b, TRUE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'const')* 'factory' componentName ('.' componentName)? formalParameterList factoryTail?
  public static boolean factoryConstructorDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration")) return false;
    if (!nextTokenIs(b, "<factory constructor declaration>", AT, CONST, EXTERNAL, FACTORY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<factory constructor declaration>");
    r = factoryConstructorDeclaration_0(b, l + 1);
    r = r && factoryConstructorDeclaration_1(b, l + 1);
    r = r && consumeToken(b, FACTORY);
    p = r; // pin = 3
    r = r && report_error_(b, componentName(b, l + 1));
    r = p && report_error_(b, factoryConstructorDeclaration_4(b, l + 1)) && r;
    r = p && report_error_(b, formalParameterList(b, l + 1)) && r;
    r = p && factoryConstructorDeclaration_6(b, l + 1) && r;
    exit_section_(b, l, m, FACTORY_CONSTRUCTOR_DECLARATION, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean factoryConstructorDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "factoryConstructorDeclaration_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('external' | 'const')*
  private static boolean factoryConstructorDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!factoryConstructorDeclaration_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "factoryConstructorDeclaration_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external' | 'const'
  private static boolean factoryConstructorDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, CONST);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('.' componentName)?
  private static boolean factoryConstructorDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_4")) return false;
    factoryConstructorDeclaration_4_0(b, l + 1);
    return true;
  }

  // '.' componentName
  private static boolean factoryConstructorDeclaration_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // factoryTail?
  private static boolean factoryConstructorDeclaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_6")) return false;
    factoryTail(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // varFactoryDeclaration ';' | functionBodyOrNative | ';'
  static boolean factoryTail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryTail")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = factoryTail_0(b, l + 1);
    if (!r) r = functionBodyOrNative(b, l + 1);
    if (!r) r = consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // varFactoryDeclaration ';'
  private static boolean factoryTail_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryTail_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = varFactoryDeclaration(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // metadata* finalConstVarVoidOrType? 'this' '.' referenceExpression formalParameterList?
  public static boolean fieldFormalParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<field formal parameter>");
    r = fieldFormalParameter_0(b, l + 1);
    r = r && fieldFormalParameter_1(b, l + 1);
    r = r && consumeToken(b, THIS);
    r = r && consumeToken(b, DOT);
    r = r && referenceExpression(b, l + 1);
    r = r && fieldFormalParameter_5(b, l + 1);
    exit_section_(b, l, m, FIELD_FORMAL_PARAMETER, r, false, null);
    return r;
  }

  // metadata*
  private static boolean fieldFormalParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fieldFormalParameter_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // finalConstVarVoidOrType?
  private static boolean fieldFormalParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter_1")) return false;
    finalConstVarVoidOrType(b, l + 1);
    return true;
  }

  // formalParameterList?
  private static boolean fieldFormalParameter_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter_5")) return false;
    formalParameterList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ('this' '.')? referenceExpression '=' expression
  public static boolean fieldInitializer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldInitializer")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<field initializer>");
    r = fieldInitializer_0(b, l + 1);
    r = r && referenceExpression(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, consumeToken(b, EQ));
    r = p && expression(b, l + 1) && r;
    exit_section_(b, l, m, FIELD_INITIALIZER, r, p, null);
    return r || p;
  }

  // ('this' '.')?
  private static boolean fieldInitializer_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldInitializer_0")) return false;
    fieldInitializer_0_0(b, l + 1);
    return true;
  }

  // 'this' '.'
  private static boolean fieldInitializer_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldInitializer_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, THIS);
    r = r && consumeToken(b, DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'final' type componentName |
  //                                                 'final'      componentName |
  //                                                 'const' type componentName |
  //                                                 'const'      componentName |
  //                                                 'var'        componentName |
  //                                                         type componentName
  static boolean finalConstVarOrTypeAndComponentName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarOrTypeAndComponentName_0(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_1(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_2(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_3(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_4(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'final' type componentName
  private static boolean finalConstVarOrTypeAndComponentName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FINAL);
    r = r && type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'final'      componentName
  private static boolean finalConstVarOrTypeAndComponentName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FINAL);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'const' type componentName
  private static boolean finalConstVarOrTypeAndComponentName_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONST);
    r = r && type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'const'      componentName
  private static boolean finalConstVarOrTypeAndComponentName_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONST);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'var'        componentName
  private static boolean finalConstVarOrTypeAndComponentName_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VAR);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // type componentName
  private static boolean finalConstVarOrTypeAndComponentName_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'final' type? | 'const' type? | 'var' | 'void' | type
  static boolean finalConstVarVoidOrType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarVoidOrType_0(b, l + 1);
    if (!r) r = finalConstVarVoidOrType_1(b, l + 1);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    if (!r) r = type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'final' type?
  private static boolean finalConstVarVoidOrType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FINAL);
    r = r && finalConstVarVoidOrType_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // type?
  private static boolean finalConstVarVoidOrType_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_0_1")) return false;
    type(b, l + 1);
    return true;
  }

  // 'const' type?
  private static boolean finalConstVarVoidOrType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONST);
    r = r && finalConstVarVoidOrType_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // type?
  private static boolean finalConstVarVoidOrType_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_1_1")) return false;
    type(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'final' | 'const'
  static boolean finalOrConst(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalOrConst")) return false;
    if (!nextTokenIs(b, "", CONST, FINAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, CONST);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'finally' block
  public static boolean finallyPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finallyPart")) return false;
    if (!nextTokenIs(b, FINALLY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, FINALLY);
    p = r; // pin = 1
    r = r && block(b, l + 1);
    exit_section_(b, l, m, FINALLY_PART, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (varAccessDeclaration | componentName) 'in' expression
  public static boolean forInPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInPart")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<for in part>");
    r = forInPart_0(b, l + 1);
    r = r && consumeToken(b, IN);
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, FOR_IN_PART, r, false, null);
    return r;
  }

  // varAccessDeclaration | componentName
  private static boolean forInPart_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInPart_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varAccessDeclaration(b, l + 1);
    if (!r) r = componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // forInPart
  //               | varDeclarationList (';' expression? (';' expressionList?)?)?
  //               | expressionList? (';' expression? (';' expressionList?)?)?
  public static boolean forLoopParts(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<for loop parts>");
    r = forInPart(b, l + 1);
    if (!r) r = forLoopParts_1(b, l + 1);
    if (!r) r = forLoopParts_2(b, l + 1);
    exit_section_(b, l, m, FOR_LOOP_PARTS, r, false, for_loops_parts_recover_parser_);
    return r;
  }

  // varDeclarationList (';' expression? (';' expressionList?)?)?
  private static boolean forLoopParts_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varDeclarationList(b, l + 1);
    r = r && forLoopParts_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (';' expression? (';' expressionList?)?)?
  private static boolean forLoopParts_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1")) return false;
    forLoopParts_1_1_0(b, l + 1);
    return true;
  }

  // ';' expression? (';' expressionList?)?
  private static boolean forLoopParts_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forLoopParts_1_1_0_1(b, l + 1);
    r = r && forLoopParts_1_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression?
  private static boolean forLoopParts_1_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1_0_1")) return false;
    expression(b, l + 1);
    return true;
  }

  // (';' expressionList?)?
  private static boolean forLoopParts_1_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1_0_2")) return false;
    forLoopParts_1_1_0_2_0(b, l + 1);
    return true;
  }

  // ';' expressionList?
  private static boolean forLoopParts_1_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forLoopParts_1_1_0_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean forLoopParts_1_1_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1_0_2_0_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // expressionList? (';' expression? (';' expressionList?)?)?
  private static boolean forLoopParts_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forLoopParts_2_0(b, l + 1);
    r = r && forLoopParts_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean forLoopParts_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_0")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // (';' expression? (';' expressionList?)?)?
  private static boolean forLoopParts_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1")) return false;
    forLoopParts_2_1_0(b, l + 1);
    return true;
  }

  // ';' expression? (';' expressionList?)?
  private static boolean forLoopParts_2_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forLoopParts_2_1_0_1(b, l + 1);
    r = r && forLoopParts_2_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression?
  private static boolean forLoopParts_2_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1_0_1")) return false;
    expression(b, l + 1);
    return true;
  }

  // (';' expressionList?)?
  private static boolean forLoopParts_2_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1_0_2")) return false;
    forLoopParts_2_1_0_2_0(b, l + 1);
    return true;
  }

  // ';' expressionList?
  private static boolean forLoopParts_2_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forLoopParts_2_1_0_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean forLoopParts_2_1_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1_0_2_0_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' forLoopParts ')'
  public static boolean forLoopPartsInBraces(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopPartsInBraces")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && forLoopParts(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, FOR_LOOP_PARTS_IN_BRACES, r);
    return r;
  }

  /* ********************************************************** */
  // 'await'? 'for' forLoopPartsInBraces statement
  public static boolean forStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement")) return false;
    if (!nextTokenIs(b, "<for statement>", AWAIT, FOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<for statement>");
    r = forStatement_0(b, l + 1);
    r = r && consumeToken(b, FOR);
    p = r; // pin = 2
    r = r && report_error_(b, forLoopPartsInBraces(b, l + 1));
    r = p && statement(b, l + 1) && r;
    exit_section_(b, l, m, FOR_STATEMENT, r, p, null);
    return r || p;
  }

  // 'await'?
  private static boolean forStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_0")) return false;
    consumeToken(b, AWAIT);
    return true;
  }

  /* ********************************************************** */
  // !')'
  static boolean for_loops_parts_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_loops_parts_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !consumeToken(b, RPAREN);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '(' normalFormalParameter (',' normalFormalParameter)* (',' namedFormalParameters)? ')'
  //                       | '(' namedFormalParameters? ')'
  public static boolean formalParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = formalParameterList_0(b, l + 1);
    if (!r) r = formalParameterList_1(b, l + 1);
    exit_section_(b, m, FORMAL_PARAMETER_LIST, r);
    return r;
  }

  // '(' normalFormalParameter (',' normalFormalParameter)* (',' namedFormalParameters)? ')'
  private static boolean formalParameterList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && normalFormalParameter(b, l + 1);
    r = r && formalParameterList_0_2(b, l + 1);
    r = r && formalParameterList_0_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' normalFormalParameter)*
  private static boolean formalParameterList_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_0_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!formalParameterList_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "formalParameterList_0_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' normalFormalParameter
  private static boolean formalParameterList_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && normalFormalParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' namedFormalParameters)?
  private static boolean formalParameterList_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_0_3")) return false;
    formalParameterList_0_3_0(b, l + 1);
    return true;
  }

  // ',' namedFormalParameters
  private static boolean formalParameterList_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && namedFormalParameters(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' namedFormalParameters? ')'
  private static boolean formalParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && formalParameterList_1_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // namedFormalParameters?
  private static boolean formalParameterList_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_1_1")) return false;
    namedFormalParameters(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // arrowBodyWithSemi | <<blockBodyWrapper>>
  public static boolean functionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<function body>");
    r = arrowBodyWithSemi(b, l + 1);
    if (!r) r = blockBodyWrapper(b, l + 1);
    exit_section_(b, l, m, FUNCTION_BODY, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'native' functionBody
  //                                | functionNative
  //                                | functionBody
  static boolean functionBodyOrNative(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBodyOrNative")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionBodyOrNative_0(b, l + 1);
    if (!r) r = functionNative(b, l + 1);
    if (!r) r = functionBody(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'native' functionBody
  private static boolean functionBodyOrNative_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBodyOrNative_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NATIVE);
    r = r && functionBody(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // returnType componentName formalParameterList | componentName formalParameterList
  static boolean functionDeclarationPrivate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationPrivate")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionDeclarationPrivate_0(b, l + 1);
    if (!r) r = functionDeclarationPrivate_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // returnType componentName formalParameterList
  private static boolean functionDeclarationPrivate_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationPrivate_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnType(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // componentName formalParameterList
  private static boolean functionDeclarationPrivate_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationPrivate_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = componentName(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata*  (returnType componentName formalParameterList | componentName formalParameterList) functionBody
  public static boolean functionDeclarationWithBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<function declaration with body>");
    r = functionDeclarationWithBody_0(b, l + 1);
    r = r && functionDeclarationWithBody_1(b, l + 1);
    r = r && functionBody(b, l + 1);
    exit_section_(b, l, m, FUNCTION_DECLARATION_WITH_BODY, r, false, null);
    return r;
  }

  // metadata*
  private static boolean functionDeclarationWithBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionDeclarationWithBody_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // returnType componentName formalParameterList | componentName formalParameterList
  private static boolean functionDeclarationWithBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionDeclarationWithBody_1_0(b, l + 1);
    if (!r) r = functionDeclarationWithBody_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // returnType componentName formalParameterList
  private static boolean functionDeclarationWithBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnType(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // componentName formalParameterList
  private static boolean functionDeclarationWithBody_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = componentName(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'external'? (returnType componentName formalParameterList | componentName formalParameterList) ( ';' | functionBodyOrNative)
  public static boolean functionDeclarationWithBodyOrNative(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<function declaration with body or native>");
    r = functionDeclarationWithBodyOrNative_0(b, l + 1);
    r = r && functionDeclarationWithBodyOrNative_1(b, l + 1);
    r = r && functionDeclarationWithBodyOrNative_2(b, l + 1);
    r = r && functionDeclarationWithBodyOrNative_3(b, l + 1);
    exit_section_(b, l, m, FUNCTION_DECLARATION_WITH_BODY_OR_NATIVE, r, false, null);
    return r;
  }

  // metadata*
  private static boolean functionDeclarationWithBodyOrNative_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionDeclarationWithBodyOrNative_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external'?
  private static boolean functionDeclarationWithBodyOrNative_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_1")) return false;
    consumeToken(b, EXTERNAL);
    return true;
  }

  // returnType componentName formalParameterList | componentName formalParameterList
  private static boolean functionDeclarationWithBodyOrNative_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionDeclarationWithBodyOrNative_2_0(b, l + 1);
    if (!r) r = functionDeclarationWithBodyOrNative_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // returnType componentName formalParameterList
  private static boolean functionDeclarationWithBodyOrNative_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnType(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // componentName formalParameterList
  private static boolean functionDeclarationWithBodyOrNative_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = componentName(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';' | functionBodyOrNative
  private static boolean functionDeclarationWithBodyOrNative_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // formalParameterList functionExpressionBody
  public static boolean functionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionExpression")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = formalParameterList(b, l + 1);
    r = r && functionExpressionBody(b, l + 1);
    exit_section_(b, m, FUNCTION_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // <<arrowBodyWrapper>> | <<blockBodyWrapper>>
  public static boolean functionExpressionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionExpressionBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<function expression body>");
    r = arrowBodyWrapper(b, l + 1);
    if (!r) r = blockBodyWrapper(b, l + 1);
    exit_section_(b, l, m, FUNCTION_EXPRESSION_BODY, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'native' (stringLiteralExpression ';' | ';' | stringLiteralExpression functionBody)
  static boolean functionNative(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNative")) return false;
    if (!nextTokenIs(b, NATIVE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NATIVE);
    r = r && functionNative_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // stringLiteralExpression ';' | ';' | stringLiteralExpression functionBody
  private static boolean functionNative_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNative_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionNative_1_0(b, l + 1);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = functionNative_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // stringLiteralExpression ';'
  private static boolean functionNative_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNative_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stringLiteralExpression(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // stringLiteralExpression functionBody
  private static boolean functionNative_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNative_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stringLiteralExpression(b, l + 1);
    r = r && functionBody(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // returnType componentName | componentName
  static boolean functionPrefix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionPrefix")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionPrefix_0(b, l + 1);
    if (!r) r = componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // returnType componentName
  private static boolean functionPrefix_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionPrefix_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnType(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* functionDeclarationPrivate
  public static boolean functionSignature(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionSignature")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<function signature>");
    r = functionSignature_0(b, l + 1);
    r = r && functionDeclarationPrivate(b, l + 1);
    exit_section_(b, l, m, FUNCTION_SIGNATURE, r, false, null);
    return r;
  }

  // metadata*
  private static boolean functionSignature_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionSignature_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionSignature_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* 'typedef' functionPrefix typeParameters? formalParameterList ';'
  public static boolean functionTypeAlias(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias")) return false;
    if (!nextTokenIs(b, "<function type alias>", AT, TYPEDEF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<function type alias>");
    r = functionTypeAlias_0(b, l + 1);
    r = r && consumeToken(b, TYPEDEF);
    r = r && functionPrefix(b, l + 1);
    r = r && functionTypeAlias_3(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    p = r; // pin = 5
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, FUNCTION_TYPE_ALIAS, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean functionTypeAlias_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionTypeAlias_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // typeParameters?
  private static boolean functionTypeAlias_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias_3")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // getterDeclarationWithReturnType | getterDeclarationWithoutReturnType
  public static boolean getterDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<getter declaration>");
    r = getterDeclarationWithReturnType(b, l + 1);
    if (!r) r = getterDeclarationWithoutReturnType(b, l + 1);
    exit_section_(b, l, m, GETTER_DECLARATION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')* returnType 'get' componentName formalParameterList? (';' | functionBodyOrNative)
  static boolean getterDeclarationWithReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = getterDeclarationWithReturnType_0(b, l + 1);
    r = r && getterDeclarationWithReturnType_1(b, l + 1);
    r = r && returnType(b, l + 1);
    r = r && consumeToken(b, GET);
    r = r && componentName(b, l + 1);
    p = r; // pin = 5
    r = r && report_error_(b, getterDeclarationWithReturnType_5(b, l + 1));
    r = p && getterDeclarationWithReturnType_6(b, l + 1) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean getterDeclarationWithReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "getterDeclarationWithReturnType_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean getterDeclarationWithReturnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!getterDeclarationWithReturnType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "getterDeclarationWithReturnType_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean getterDeclarationWithReturnType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    exit_section_(b, m, null, r);
    return r;
  }

  // formalParameterList?
  private static boolean getterDeclarationWithReturnType_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType_5")) return false;
    formalParameterList(b, l + 1);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean getterDeclarationWithReturnType_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')*            'get' componentName formalParameterList? (';' | functionBodyOrNative)
  static boolean getterDeclarationWithoutReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType")) return false;
    if (!nextTokenIs(b, "", AT, EXTERNAL, GET, STATIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = getterDeclarationWithoutReturnType_0(b, l + 1);
    r = r && getterDeclarationWithoutReturnType_1(b, l + 1);
    r = r && consumeToken(b, GET);
    r = r && componentName(b, l + 1);
    p = r; // pin = 4
    r = r && report_error_(b, getterDeclarationWithoutReturnType_4(b, l + 1));
    r = p && getterDeclarationWithoutReturnType_5(b, l + 1) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean getterDeclarationWithoutReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "getterDeclarationWithoutReturnType_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean getterDeclarationWithoutReturnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!getterDeclarationWithoutReturnType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "getterDeclarationWithoutReturnType_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean getterDeclarationWithoutReturnType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    exit_section_(b, m, null, r);
    return r;
  }

  // formalParameterList?
  private static boolean getterDeclarationWithoutReturnType_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType_4")) return false;
    formalParameterList(b, l + 1);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean getterDeclarationWithoutReturnType_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // getterDeclaration | setterDeclaration
  static boolean getterOrSetterDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterOrSetterDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = getterDeclaration(b, l + 1);
    if (!r) r = setterDeclaration(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'hide' libraryReferenceList
  public static boolean hideCombinator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "hideCombinator")) return false;
    if (!nextTokenIs(b, HIDE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, HIDE);
    p = r; // pin = 1
    r = r && libraryReferenceList(b, l + 1);
    exit_section_(b, l, m, HIDE_COMBINATOR, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ID, r);
    return r;
  }

  /* ********************************************************** */
  // '??' logicOrExpressionWrapper
  public static boolean ifNullExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifNullExpression")) return false;
    if (!nextTokenIs(b, QUEST_QUEST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, null);
    r = consumeToken(b, QUEST_QUEST);
    r = r && logicOrExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, IF_NULL_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // logicOrExpressionWrapper ifNullExpression*
  static boolean ifNullExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifNullExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = logicOrExpressionWrapper(b, l + 1);
    r = r && ifNullExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ifNullExpression*
  private static boolean ifNullExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifNullExpressionWrapper_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!ifNullExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifNullExpressionWrapper_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // 'if' '(' expressionWithRecoverUntilParen ')' statement ('else' statement)?
  public static boolean ifStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, IF);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, LPAREN));
    r = p && report_error_(b, expressionWithRecoverUntilParen(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && report_error_(b, statement(b, l + 1)) && r;
    r = p && ifStatement_5(b, l + 1) && r;
    exit_section_(b, l, m, IF_STATEMENT, r, p, null);
    return r || p;
  }

  // ('else' statement)?
  private static boolean ifStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_5")) return false;
    ifStatement_5_0(b, l + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifStatement_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'import' uriElement ('deferred'? 'as' componentName )? combinator* ';'
  public static boolean importStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement")) return false;
    if (!nextTokenIs(b, "<import statement>", AT, IMPORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<import statement>");
    r = importStatement_0(b, l + 1);
    r = r && consumeToken(b, IMPORT);
    r = r && uriElement(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, importStatement_3(b, l + 1));
    r = p && report_error_(b, importStatement_4(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, IMPORT_STATEMENT, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean importStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "importStatement_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('deferred'? 'as' componentName )?
  private static boolean importStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_3")) return false;
    importStatement_3_0(b, l + 1);
    return true;
  }

  // 'deferred'? 'as' componentName
  private static boolean importStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = importStatement_3_0_0(b, l + 1);
    r = r && consumeToken(b, AS);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'deferred'?
  private static boolean importStatement_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_3_0_0")) return false;
    consumeToken(b, DEFERRED);
    return true;
  }

  // combinator*
  private static boolean importStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!combinator(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "importStatement_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static' | 'final' | 'const')* type | metadata+
  public static boolean incompleteDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<incomplete declaration>");
    r = incompleteDeclaration_0(b, l + 1);
    if (!r) r = incompleteDeclaration_1(b, l + 1);
    exit_section_(b, l, m, INCOMPLETE_DECLARATION, r, false, null);
    return r;
  }

  // metadata* ('external' | 'static' | 'final' | 'const')* type
  private static boolean incompleteDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = incompleteDeclaration_0_0(b, l + 1);
    r = r && incompleteDeclaration_0_1(b, l + 1);
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean incompleteDeclaration_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration_0_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "incompleteDeclaration_0_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('external' | 'static' | 'final' | 'const')*
  private static boolean incompleteDeclaration_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!incompleteDeclaration_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "incompleteDeclaration_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external' | 'static' | 'final' | 'const'
  private static boolean incompleteDeclaration_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, CONST);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata+
  private static boolean incompleteDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = metadata(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "incompleteDeclaration_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<' + <<nonStrictID>> + '>' <<nonStrictID>>
  static boolean incorrectNormalFormalParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incorrectNormalFormalParameter")) return false;
    if (!nextTokenIs(b, LT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = incorrectNormalFormalParameter_0(b, l + 1);
    r = r && incorrectNormalFormalParameter_1(b, l + 1);
    r = r && consumeToken(b, GT);
    r = r && nonStrictID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '<' +
  private static boolean incorrectNormalFormalParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incorrectNormalFormalParameter_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    int c = current_position_(b);
    while (r) {
      if (!consumeToken(b, LT)) break;
      if (!empty_element_parsed_guard_(b, "incorrectNormalFormalParameter_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // <<nonStrictID>> +
  private static boolean incorrectNormalFormalParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incorrectNormalFormalParameter_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!nonStrictID(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "incorrectNormalFormalParameter_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ':' superCallOrFieldInitializer (',' superCallOrFieldInitializer)*
  public static boolean initializers(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initializers")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && superCallOrFieldInitializer(b, l + 1);
    r = r && initializers_2(b, l + 1);
    exit_section_(b, m, INITIALIZERS, r);
    return r;
  }

  // (',' superCallOrFieldInitializer)*
  private static boolean initializers_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initializers_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!initializers_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "initializers_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' superCallOrFieldInitializer
  private static boolean initializers_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initializers_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && superCallOrFieldInitializer(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'implements' typeList
  public static boolean interfaces(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaces")) return false;
    if (!nextTokenIs(b, IMPLEMENTS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, IMPLEMENTS);
    p = r; // pin = 1
    r = r && typeList(b, l + 1);
    exit_section_(b, l, m, INTERFACES, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'is' '!'? type
  public static boolean isExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "isExpression")) return false;
    if (!nextTokenIs(b, IS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, null);
    r = consumeToken(b, IS);
    r = r && isExpression_1(b, l + 1);
    r = r && type(b, l + 1);
    exit_section_(b, l, m, IS_EXPRESSION, r, false, null);
    return r;
  }

  // '!'?
  private static boolean isExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "isExpression_1")) return false;
    consumeToken(b, NOT);
    return true;
  }

  /* ********************************************************** */
  // componentName ':'
  public static boolean label(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<label>");
    r = componentName(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, l, m, LABEL, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean libraryComponentReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryComponentReferenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<library component reference expression>");
    r = nonStrictID(b, l + 1);
    exit_section_(b, l, m, LIBRARY_COMPONENT_REFERENCE_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<nonStrictID>> ('.' <<nonStrictID>>)*
  public static boolean libraryId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryId")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<library id>");
    r = nonStrictID(b, l + 1);
    r = r && libraryId_1(b, l + 1);
    exit_section_(b, l, m, LIBRARY_ID, r, false, null);
    return r;
  }

  // ('.' <<nonStrictID>>)*
  private static boolean libraryId_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryId_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!libraryId_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "libraryId_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // '.' <<nonStrictID>>
  private static boolean libraryId_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryId_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && nonStrictID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // <<nonStrictID>> ('.' <<nonStrictID>>)*
  public static boolean libraryNameElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryNameElement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<library name element>");
    r = nonStrictID(b, l + 1);
    r = r && libraryNameElement_1(b, l + 1);
    exit_section_(b, l, m, LIBRARY_NAME_ELEMENT, r, false, null);
    return r;
  }

  // ('.' <<nonStrictID>>)*
  private static boolean libraryNameElement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryNameElement_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!libraryNameElement_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "libraryNameElement_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // '.' <<nonStrictID>>
  private static boolean libraryNameElement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryNameElement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && nonStrictID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // libraryComponentReferenceExpression (',' libraryComponentReferenceExpression)*
  public static boolean libraryReferenceList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryReferenceList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<library reference list>");
    r = libraryComponentReferenceExpression(b, l + 1);
    r = r && libraryReferenceList_1(b, l + 1);
    exit_section_(b, l, m, LIBRARY_REFERENCE_LIST, r, false, null);
    return r;
  }

  // (',' libraryComponentReferenceExpression)*
  private static boolean libraryReferenceList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryReferenceList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!libraryReferenceList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "libraryReferenceList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' libraryComponentReferenceExpression
  private static boolean libraryReferenceList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryReferenceList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && libraryComponentReferenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'library' libraryNameElement ';'
  public static boolean libraryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryStatement")) return false;
    if (!nextTokenIs(b, "<library statement>", AT, LIBRARY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<library statement>");
    r = libraryStatement_0(b, l + 1);
    r = r && consumeToken(b, LIBRARY);
    r = r && libraryNameElement(b, l + 1);
    p = r; // pin = 3
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, LIBRARY_STATEMENT, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean libraryStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryStatement_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "libraryStatement_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // 'const'? typeArguments? '[' (expressionList ','?)? ']'
  public static boolean listLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listLiteralExpression")) return false;
    if (!nextTokenIs(b, "<list literal expression>", LT, LBRACKET, CONST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<list literal expression>");
    r = listLiteralExpression_0(b, l + 1);
    r = r && listLiteralExpression_1(b, l + 1);
    r = r && consumeToken(b, LBRACKET);
    r = r && listLiteralExpression_3(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, l, m, LIST_LITERAL_EXPRESSION, r, false, null);
    return r;
  }

  // 'const'?
  private static boolean listLiteralExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listLiteralExpression_0")) return false;
    consumeToken(b, CONST);
    return true;
  }

  // typeArguments?
  private static boolean listLiteralExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listLiteralExpression_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // (expressionList ','?)?
  private static boolean listLiteralExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listLiteralExpression_3")) return false;
    listLiteralExpression_3_0(b, l + 1);
    return true;
  }

  // expressionList ','?
  private static boolean listLiteralExpression_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listLiteralExpression_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expressionList(b, l + 1);
    r = r && listLiteralExpression_3_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean listLiteralExpression_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listLiteralExpression_3_0_1")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // NULL | TRUE | FALSE | NUMBER | HEX_NUMBER | stringLiteralExpression | symbolLiteralExpression | <<mapLiteralExpressionWrapper>> | <<listLiteralExpressionWrapper>>
  public static boolean literalExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<literal expression>");
    r = consumeToken(b, NULL);
    if (!r) r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, HEX_NUMBER);
    if (!r) r = stringLiteralExpression(b, l + 1);
    if (!r) r = symbolLiteralExpression(b, l + 1);
    if (!r) r = mapLiteralExpressionWrapper(b, l + 1);
    if (!r) r = listLiteralExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, LITERAL_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '&&' compareExpressionWrapper
  public static boolean logicAndExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicAndExpression")) return false;
    if (!nextTokenIs(b, AND_AND)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, null);
    r = consumeToken(b, AND_AND);
    r = r && compareExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, LOGIC_AND_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // compareExpressionWrapper logicAndExpression*
  static boolean logicAndExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicAndExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compareExpressionWrapper(b, l + 1);
    r = r && logicAndExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // logicAndExpression*
  private static boolean logicAndExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicAndExpressionWrapper_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!logicAndExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logicAndExpressionWrapper_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '||' logicAndExpressionWrapper
  public static boolean logicOrExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicOrExpression")) return false;
    if (!nextTokenIs(b, OR_OR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, null);
    r = consumeToken(b, OR_OR);
    r = r && logicAndExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, LOGIC_OR_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // logicAndExpressionWrapper logicOrExpression*
  static boolean logicOrExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicOrExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = logicAndExpressionWrapper(b, l + 1);
    r = r && logicOrExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // logicOrExpression*
  private static boolean logicOrExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicOrExpressionWrapper_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!logicOrExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logicOrExpressionWrapper_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // LONG_TEMPLATE_ENTRY_START expression LONG_TEMPLATE_ENTRY_END
  public static boolean longTemplateEntry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "longTemplateEntry")) return false;
    if (!nextTokenIs(b, LONG_TEMPLATE_ENTRY_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, LONG_TEMPLATE_ENTRY_START);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, LONG_TEMPLATE_ENTRY_END) && r;
    exit_section_(b, l, m, LONG_TEMPLATE_ENTRY, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // expression ':' expression
  public static boolean mapLiteralEntry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteralEntry")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<map literal entry>");
    r = expression(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, MAP_LITERAL_ENTRY, r, false, map_literal_entry_recover_parser_);
    return r;
  }

  /* ********************************************************** */
  // 'const'? typeArguments? '{' (mapLiteralEntry (',' mapLiteralEntry)* ','? )? '}'
  public static boolean mapLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteralExpression")) return false;
    if (!nextTokenIs(b, "<map literal expression>", LT, CONST, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<map literal expression>");
    r = mapLiteralExpression_0(b, l + 1);
    r = r && mapLiteralExpression_1(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && mapLiteralExpression_3(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, MAP_LITERAL_EXPRESSION, r, false, null);
    return r;
  }

  // 'const'?
  private static boolean mapLiteralExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteralExpression_0")) return false;
    consumeToken(b, CONST);
    return true;
  }

  // typeArguments?
  private static boolean mapLiteralExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteralExpression_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // (mapLiteralEntry (',' mapLiteralEntry)* ','? )?
  private static boolean mapLiteralExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteralExpression_3")) return false;
    mapLiteralExpression_3_0(b, l + 1);
    return true;
  }

  // mapLiteralEntry (',' mapLiteralEntry)* ','?
  private static boolean mapLiteralExpression_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteralExpression_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mapLiteralEntry(b, l + 1);
    r = r && mapLiteralExpression_3_0_1(b, l + 1);
    r = r && mapLiteralExpression_3_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' mapLiteralEntry)*
  private static boolean mapLiteralExpression_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteralExpression_3_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!mapLiteralExpression_3_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "mapLiteralExpression_3_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' mapLiteralEntry
  private static boolean mapLiteralExpression_3_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteralExpression_3_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && mapLiteralEntry(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean mapLiteralExpression_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapLiteralExpression_3_0_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // !(',' | '}')
  static boolean map_literal_entry_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_literal_entry_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !map_literal_entry_recover_0(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  // ',' | '}'
  private static boolean map_literal_entry_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_literal_entry_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '@' simpleQualifiedReferenceExpression arguments?
  public static boolean metadata(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "metadata")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT);
    r = r && simpleQualifiedReferenceExpression(b, l + 1);
    r = r && metadata_2(b, l + 1);
    exit_section_(b, m, METADATA, r);
    return r;
  }

  // arguments?
  private static boolean metadata_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "metadata_2")) return false;
    arguments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static' | 'const')* functionDeclarationPrivate initializers? (';' | functionBodyOrNative | redirection)?
  public static boolean methodDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<method declaration>");
    r = methodDeclaration_0(b, l + 1);
    r = r && methodDeclaration_1(b, l + 1);
    r = r && functionDeclarationPrivate(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, methodDeclaration_3(b, l + 1));
    r = p && methodDeclaration_4(b, l + 1) && r;
    exit_section_(b, l, m, METHOD_DECLARATION, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean methodDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "methodDeclaration_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('external' | 'static' | 'const')*
  private static boolean methodDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!methodDeclaration_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "methodDeclaration_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external' | 'static' | 'const'
  private static boolean methodDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, CONST);
    exit_section_(b, m, null, r);
    return r;
  }

  // initializers?
  private static boolean methodDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_3")) return false;
    initializers(b, l + 1);
    return true;
  }

  // (';' | functionBodyOrNative | redirection)?
  private static boolean methodDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_4")) return false;
    methodDeclaration_4_0(b, l + 1);
    return true;
  }

  // ';' | functionBodyOrNative | redirection
  private static boolean methodDeclaration_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    if (!r) r = redirection(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '=' type mixins? interfaces? ';'
  public static boolean mixinApplication(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinApplication")) return false;
    if (!nextTokenIs(b, EQ)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, EQ);
    p = r; // pin = 1
    r = r && report_error_(b, type(b, l + 1));
    r = p && report_error_(b, mixinApplication_2(b, l + 1)) && r;
    r = p && report_error_(b, mixinApplication_3(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, MIXIN_APPLICATION, r, p, null);
    return r || p;
  }

  // mixins?
  private static boolean mixinApplication_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinApplication_2")) return false;
    mixins(b, l + 1);
    return true;
  }

  // interfaces?
  private static boolean mixinApplication_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinApplication_3")) return false;
    interfaces(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'with' typeList
  public static boolean mixins(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixins")) return false;
    if (!nextTokenIs(b, WITH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, WITH);
    p = r; // pin = 1
    r = r && typeList(b, l + 1);
    exit_section_(b, l, m, MIXINS, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // multiplicativeOperator prefixExpression
  public static boolean multiplicativeExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpression")) return false;
    if (!nextTokenIs(b, "<multiplicative expression>", REM, MUL, DIV, INT_DIV)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, "<multiplicative expression>");
    r = multiplicativeOperator(b, l + 1);
    r = r && prefixExpression(b, l + 1);
    exit_section_(b, l, m, MULTIPLICATIVE_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // prefixExpression multiplicativeExpression*
  static boolean multiplicativeExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prefixExpression(b, l + 1);
    r = r && multiplicativeExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // multiplicativeExpression*
  private static boolean multiplicativeExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpressionWrapper_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!multiplicativeExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "multiplicativeExpressionWrapper_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '*' | '/' | '%' | '~/'
  public static boolean multiplicativeOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeOperator")) return false;
    if (!nextTokenIs(b, "<multiplicative operator>", REM, MUL, DIV, INT_DIV)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<multiplicative operator>");
    r = consumeToken(b, MUL);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, REM);
    if (!r) r = consumeToken(b, INT_DIV);
    exit_section_(b, l, m, MULTIPLICATIVE_OPERATOR, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // parameterNameReferenceExpression ':' expression
  public static boolean namedArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedArgument")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<named argument>");
    r = parameterNameReferenceExpression(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, NAMED_ARGUMENT, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'const')* componentName '.' componentName formalParameterList initializers? (';' | functionBodyOrNative | redirection)?
  public static boolean namedConstructorDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<named constructor declaration>");
    r = namedConstructorDeclaration_0(b, l + 1);
    r = r && namedConstructorDeclaration_1(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && consumeToken(b, DOT);
    r = r && componentName(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    p = r; // pin = 6
    r = r && report_error_(b, namedConstructorDeclaration_6(b, l + 1));
    r = p && namedConstructorDeclaration_7(b, l + 1) && r;
    exit_section_(b, l, m, NAMED_CONSTRUCTOR_DECLARATION, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean namedConstructorDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namedConstructorDeclaration_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('external' | 'const')*
  private static boolean namedConstructorDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!namedConstructorDeclaration_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namedConstructorDeclaration_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external' | 'const'
  private static boolean namedConstructorDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, CONST);
    exit_section_(b, m, null, r);
    return r;
  }

  // initializers?
  private static boolean namedConstructorDeclaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_6")) return false;
    initializers(b, l + 1);
    return true;
  }

  // (';' | functionBodyOrNative | redirection)?
  private static boolean namedConstructorDeclaration_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_7")) return false;
    namedConstructorDeclaration_7_0(b, l + 1);
    return true;
  }

  // ';' | functionBodyOrNative | redirection
  private static boolean namedConstructorDeclaration_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    if (!r) r = redirection(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '[' defaultFormalNamedParameter (',' defaultFormalNamedParameter)* ']' |
  //                           '{' defaultFormalNamedParameter (',' defaultFormalNamedParameter)* '}'
  public static boolean namedFormalParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters")) return false;
    if (!nextTokenIs(b, "<named formal parameters>", LBRACKET, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<named formal parameters>");
    r = namedFormalParameters_0(b, l + 1);
    if (!r) r = namedFormalParameters_1(b, l + 1);
    exit_section_(b, l, m, NAMED_FORMAL_PARAMETERS, r, false, null);
    return r;
  }

  // '[' defaultFormalNamedParameter (',' defaultFormalNamedParameter)* ']'
  private static boolean namedFormalParameters_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && defaultFormalNamedParameter(b, l + 1);
    r = r && namedFormalParameters_0_2(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' defaultFormalNamedParameter)*
  private static boolean namedFormalParameters_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters_0_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!namedFormalParameters_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namedFormalParameters_0_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' defaultFormalNamedParameter
  private static boolean namedFormalParameters_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && defaultFormalNamedParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '{' defaultFormalNamedParameter (',' defaultFormalNamedParameter)* '}'
  private static boolean namedFormalParameters_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && defaultFormalNamedParameter(b, l + 1);
    r = r && namedFormalParameters_1_2(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' defaultFormalNamedParameter)*
  private static boolean namedFormalParameters_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters_1_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!namedFormalParameters_1_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namedFormalParameters_1_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' defaultFormalNamedParameter
  private static boolean namedFormalParameters_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && defaultFormalNamedParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ('new' | 'const') type ('.' referenceExpression)? arguments
  public static boolean newExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpression")) return false;
    if (!nextTokenIs(b, "<new expression>", CONST, NEW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<new expression>");
    r = newExpression_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, type(b, l + 1));
    r = p && report_error_(b, newExpression_2(b, l + 1)) && r;
    r = p && arguments(b, l + 1) && r;
    exit_section_(b, l, m, NEW_EXPRESSION, r, p, null);
    return r || p;
  }

  // 'new' | 'const'
  private static boolean newExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEW);
    if (!r) r = consumeToken(b, CONST);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('.' referenceExpression)?
  private static boolean newExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpression_2")) return false;
    newExpression_2_0(b, l + 1);
    return true;
  }

  // '.' referenceExpression
  private static boolean newExpression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpression_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && referenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // block // Guard to break tie with map literal.  todo why ';'?
  //                                | functionDeclarationWithBody ';'?
  //                                | forStatement ';'?
  //                                | whileStatement ';'?
  //                                | doWhileStatement ';'?
  //                                | switchStatement ';'?
  //                                | ifStatement ';'?
  //                                | rethrowStatement
  //                                | tryStatement
  //                                | breakStatement
  //                                | continueStatement
  //                                | returnStatement
  //                                | assertStatement
  //                                | statementFollowedBySemiColon
  //                                | yieldEachStatement
  //                                | yieldStatement
  //                                | ';'
  static boolean nonLabelledStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = block(b, l + 1);
    if (!r) r = nonLabelledStatement_1(b, l + 1);
    if (!r) r = nonLabelledStatement_2(b, l + 1);
    if (!r) r = nonLabelledStatement_3(b, l + 1);
    if (!r) r = nonLabelledStatement_4(b, l + 1);
    if (!r) r = nonLabelledStatement_5(b, l + 1);
    if (!r) r = nonLabelledStatement_6(b, l + 1);
    if (!r) r = rethrowStatement(b, l + 1);
    if (!r) r = tryStatement(b, l + 1);
    if (!r) r = breakStatement(b, l + 1);
    if (!r) r = continueStatement(b, l + 1);
    if (!r) r = returnStatement(b, l + 1);
    if (!r) r = assertStatement(b, l + 1);
    if (!r) r = statementFollowedBySemiColon(b, l + 1);
    if (!r) r = yieldEachStatement(b, l + 1);
    if (!r) r = yieldStatement(b, l + 1);
    if (!r) r = consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // functionDeclarationWithBody ';'?
  private static boolean nonLabelledStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionDeclarationWithBody(b, l + 1);
    r = r && nonLabelledStatement_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';'?
  private static boolean nonLabelledStatement_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_1_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  // forStatement ';'?
  private static boolean nonLabelledStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forStatement(b, l + 1);
    r = r && nonLabelledStatement_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';'?
  private static boolean nonLabelledStatement_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_2_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  // whileStatement ';'?
  private static boolean nonLabelledStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = whileStatement(b, l + 1);
    r = r && nonLabelledStatement_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';'?
  private static boolean nonLabelledStatement_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_3_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  // doWhileStatement ';'?
  private static boolean nonLabelledStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = doWhileStatement(b, l + 1);
    r = r && nonLabelledStatement_4_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';'?
  private static boolean nonLabelledStatement_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_4_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  // switchStatement ';'?
  private static boolean nonLabelledStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = switchStatement(b, l + 1);
    r = r && nonLabelledStatement_5_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';'?
  private static boolean nonLabelledStatement_5_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_5_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  // ifStatement ';'?
  private static boolean nonLabelledStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ifStatement(b, l + 1);
    r = r && nonLabelledStatement_6_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';'?
  private static boolean nonLabelledStatement_6_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_6_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // functionSignature
  //                         | fieldFormalParameter
  //                         | simpleFormalParameter
  //                         | incorrectNormalFormalParameter
  public static boolean normalFormalParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normalFormalParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<normal formal parameter>");
    r = functionSignature(b, l + 1);
    if (!r) r = fieldFormalParameter(b, l + 1);
    if (!r) r = simpleFormalParameter(b, l + 1);
    if (!r) r = incorrectNormalFormalParameter(b, l + 1);
    exit_section_(b, l, m, NORMAL_FORMAL_PARAMETER, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !')'
  static boolean not_paren_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_paren_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !consumeToken(b, RPAREN);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // catchPart block | 'on' type catchPart? block
  public static boolean onPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onPart")) return false;
    if (!nextTokenIs(b, "<on part>", CATCH, ON)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<on part>");
    r = onPart_0(b, l + 1);
    if (!r) r = onPart_1(b, l + 1);
    exit_section_(b, l, m, ON_PART, r, false, null);
    return r;
  }

  // catchPart block
  private static boolean onPart_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onPart_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = catchPart(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'on' type catchPart? block
  private static boolean onPart_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onPart_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ON);
    r = r && type(b, l + 1);
    r = r && onPart_1_2(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // catchPart?
  private static boolean onPart_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onPart_1_2")) return false;
    catchPart(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // operatorDeclarationWithReturnType | operatorDeclarationWithoutReturnType
  public static boolean operatorDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<operator declaration>");
    r = operatorDeclarationWithReturnType(b, l + 1);
    if (!r) r = operatorDeclarationWithoutReturnType(b, l + 1);
    exit_section_(b, l, m, OPERATOR_DECLARATION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'external'? returnType 'operator' userDefinableOperator formalParameterList (';' | functionBodyOrNative)
  static boolean operatorDeclarationWithReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDeclarationWithReturnType")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = operatorDeclarationWithReturnType_0(b, l + 1);
    r = r && operatorDeclarationWithReturnType_1(b, l + 1);
    r = r && returnType(b, l + 1);
    r = r && consumeToken(b, OPERATOR);
    r = r && userDefinableOperator(b, l + 1);
    p = r; // pin = 5
    r = r && report_error_(b, formalParameterList(b, l + 1));
    r = p && operatorDeclarationWithReturnType_6(b, l + 1) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean operatorDeclarationWithReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDeclarationWithReturnType_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "operatorDeclarationWithReturnType_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external'?
  private static boolean operatorDeclarationWithReturnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDeclarationWithReturnType_1")) return false;
    consumeToken(b, EXTERNAL);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean operatorDeclarationWithReturnType_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDeclarationWithReturnType_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'external'?            'operator' userDefinableOperator formalParameterList (';' | functionBodyOrNative)
  static boolean operatorDeclarationWithoutReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDeclarationWithoutReturnType")) return false;
    if (!nextTokenIs(b, "", AT, EXTERNAL, OPERATOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = operatorDeclarationWithoutReturnType_0(b, l + 1);
    r = r && operatorDeclarationWithoutReturnType_1(b, l + 1);
    r = r && consumeToken(b, OPERATOR);
    r = r && userDefinableOperator(b, l + 1);
    p = r; // pin = 4
    r = r && report_error_(b, formalParameterList(b, l + 1));
    r = p && operatorDeclarationWithoutReturnType_5(b, l + 1) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean operatorDeclarationWithoutReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDeclarationWithoutReturnType_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "operatorDeclarationWithoutReturnType_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external'?
  private static boolean operatorDeclarationWithoutReturnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDeclarationWithoutReturnType_1")) return false;
    consumeToken(b, EXTERNAL);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean operatorDeclarationWithoutReturnType_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDeclarationWithoutReturnType_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean parameterNameReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterNameReferenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<parameter name reference expression>");
    r = nonStrictID(b, l + 1);
    exit_section_(b, l, m, PARAMETER_NAME_REFERENCE_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !')'
  static boolean parenthesesRecovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesesRecovery")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !consumeToken(b, RPAREN);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '(' expressionInParentheses ')'
  public static boolean parenthesizedExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesizedExpression")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expressionInParentheses(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, PARENTHESIZED_EXPRESSION, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // metadata* 'part' 'of' libraryId ';'
  public static boolean partOfStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "partOfStatement")) return false;
    if (!nextTokenIs(b, "<part of statement>", AT, PART)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<part of statement>");
    r = partOfStatement_0(b, l + 1);
    r = r && consumeToken(b, PART);
    r = r && consumeToken(b, OF);
    r = r && libraryId(b, l + 1);
    p = r; // pin = 4
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, PART_OF_STATEMENT, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean partOfStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "partOfStatement_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "partOfStatement_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* 'part' uriElement ';'
  public static boolean partStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "partStatement")) return false;
    if (!nextTokenIs(b, "<part statement>", AT, PART)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<part statement>");
    r = partStatement_0(b, l + 1);
    r = r && consumeToken(b, PART);
    r = r && uriElement(b, l + 1);
    p = r; // pin = 3
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, PART_STATEMENT, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean partStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "partStatement_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "partStatement_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // (prefixOperator prefixExpression) | awaitExpression | suffixExpressionWrapper
  public static boolean prefixExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prefixExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<prefix expression>");
    r = prefixExpression_0(b, l + 1);
    if (!r) r = awaitExpression(b, l + 1);
    if (!r) r = suffixExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, PREFIX_EXPRESSION, r, false, null);
    return r;
  }

  // prefixOperator prefixExpression
  private static boolean prefixExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prefixExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prefixOperator(b, l + 1);
    r = r && prefixExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '-' | '+' | '--' | '++' | '!' | '~'
  public static boolean prefixOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prefixOperator")) return false;
    if (!nextTokenIs(b, "<prefix operator>", NOT, PLUS,
      PLUS_PLUS, MINUS, MINUS_MINUS, BIN_NOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<prefix operator>");
    r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS_MINUS);
    if (!r) r = consumeToken(b, PLUS_PLUS);
    if (!r) r = consumeToken(b, NOT);
    if (!r) r = consumeToken(b, BIN_NOT);
    exit_section_(b, l, m, PREFIX_OPERATOR, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // literalExpression |
  //                      functionExpression |
  //                      newExpression | // constant object expression is also parsed as newExpression
  //                      refOrThisOrSuperOrParenExpression |
  //                      throwExpression
  static boolean primary(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primary")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = literalExpression(b, l + 1);
    if (!r) r = functionExpression(b, l + 1);
    if (!r) r = newExpression(b, l + 1);
    if (!r) r = refOrThisOrSuperOrParenExpression(b, l + 1);
    if (!r) r = throwExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '.' referenceExpression | '?.' referenceExpression
  public static boolean qualifiedReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedReferenceExpression")) return false;
    if (!nextTokenIs(b, "<qualified reference expression>", DOT, QUEST_DOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, "<qualified reference expression>");
    r = qualifiedReferenceExpression_0(b, l + 1);
    if (!r) r = qualifiedReferenceExpression_1(b, l + 1);
    exit_section_(b, l, m, REFERENCE_EXPRESSION, r, false, null);
    return r;
  }

  // '.' referenceExpression
  private static boolean qualifiedReferenceExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedReferenceExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && referenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '?.' referenceExpression
  private static boolean qualifiedReferenceExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedReferenceExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUEST_DOT);
    r = r && referenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ':' 'this' ('.' referenceExpression)? arguments
  public static boolean redirection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "redirection")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, COLON);
    r = r && consumeToken(b, THIS);
    p = r; // pin = 2
    r = r && report_error_(b, redirection_2(b, l + 1));
    r = p && arguments(b, l + 1) && r;
    exit_section_(b, l, m, REDIRECTION, r, p, null);
    return r || p;
  }

  // ('.' referenceExpression)?
  private static boolean redirection_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "redirection_2")) return false;
    redirection_2_0(b, l + 1);
    return true;
  }

  // '.' referenceExpression
  private static boolean redirection_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "redirection_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && referenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // referenceExpression | thisExpression | superExpression | << parenthesizedExpressionWrapper >>
  static boolean refOrThisOrSuperOrParenExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "refOrThisOrSuperOrParenExpression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = referenceExpression(b, l + 1);
    if (!r) r = thisExpression(b, l + 1);
    if (!r) r = superExpression(b, l + 1);
    if (!r) r = parenthesizedExpressionWrapper(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean referenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<reference expression>");
    r = nonStrictID(b, l + 1);
    exit_section_(b, l, m, REFERENCE_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<gtEq>> | '>' | '<=' | '<'
  public static boolean relationalOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<relational operator>");
    r = gtEq(b, l + 1);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, LT_EQ);
    if (!r) r = consumeToken(b, LT);
    exit_section_(b, l, m, RELATIONAL_OPERATOR, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'rethrow' ';'
  public static boolean rethrowStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rethrowStatement")) return false;
    if (!nextTokenIs(b, RETHROW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, RETHROW);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, RETHROW_STATEMENT, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'return' expression? ';'
  public static boolean returnStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement")) return false;
    if (!nextTokenIs(b, RETURN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, RETURN);
    p = r; // pin = 1
    r = r && report_error_(b, returnStatement_1(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, RETURN_STATEMENT, r, p, null);
    return r || p;
  }

  // expression?
  private static boolean returnStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement_1")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'void' | type
  public static boolean returnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<return type>");
    r = consumeToken(b, VOID);
    if (!r) r = type(b, l + 1);
    exit_section_(b, l, m, RETURN_TYPE, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // setterDeclarationWithReturnType | setterDeclarationWithoutReturnType
  public static boolean setterDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<setter declaration>");
    r = setterDeclarationWithReturnType(b, l + 1);
    if (!r) r = setterDeclarationWithoutReturnType(b, l + 1);
    exit_section_(b, l, m, SETTER_DECLARATION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')* returnType 'set' componentName formalParameterList (';' | functionBodyOrNative)
  static boolean setterDeclarationWithReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithReturnType")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = setterDeclarationWithReturnType_0(b, l + 1);
    r = r && setterDeclarationWithReturnType_1(b, l + 1);
    r = r && returnType(b, l + 1);
    r = r && consumeToken(b, SET);
    r = r && componentName(b, l + 1);
    p = r; // pin = 5
    r = r && report_error_(b, formalParameterList(b, l + 1));
    r = p && setterDeclarationWithReturnType_6(b, l + 1) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean setterDeclarationWithReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithReturnType_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "setterDeclarationWithReturnType_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean setterDeclarationWithReturnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithReturnType_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!setterDeclarationWithReturnType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "setterDeclarationWithReturnType_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean setterDeclarationWithReturnType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithReturnType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';' | functionBodyOrNative
  private static boolean setterDeclarationWithReturnType_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithReturnType_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')*            'set' componentName formalParameterList (';' | functionBodyOrNative)
  static boolean setterDeclarationWithoutReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithoutReturnType")) return false;
    if (!nextTokenIs(b, "", AT, EXTERNAL, SET, STATIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = setterDeclarationWithoutReturnType_0(b, l + 1);
    r = r && setterDeclarationWithoutReturnType_1(b, l + 1);
    r = r && consumeToken(b, SET);
    r = r && componentName(b, l + 1);
    p = r; // pin = 4
    r = r && report_error_(b, formalParameterList(b, l + 1));
    r = p && setterDeclarationWithoutReturnType_5(b, l + 1) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean setterDeclarationWithoutReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithoutReturnType_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "setterDeclarationWithoutReturnType_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean setterDeclarationWithoutReturnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithoutReturnType_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!setterDeclarationWithoutReturnType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "setterDeclarationWithoutReturnType_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean setterDeclarationWithoutReturnType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithoutReturnType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';' | functionBodyOrNative
  private static boolean setterDeclarationWithoutReturnType_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithoutReturnType_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // shiftOperator additiveExpressionWrapper
  public static boolean shiftExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, "<shift expression>");
    r = shiftOperator(b, l + 1);
    r = r && additiveExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, SHIFT_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // additiveExpressionWrapper shiftExpression*
  static boolean shiftExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = additiveExpressionWrapper(b, l + 1);
    r = r && shiftExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // shiftExpression*
  private static boolean shiftExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftExpressionWrapper_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!shiftExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "shiftExpressionWrapper_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '<<' | <<gtGt>>
  public static boolean shiftOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<shift operator>");
    r = consumeToken(b, LT_LT);
    if (!r) r = gtGt(b, l + 1);
    exit_section_(b, l, m, SHIFT_OPERATOR, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // SHORT_TEMPLATE_ENTRY_START (thisExpression | referenceExpression)
  public static boolean shortTemplateEntry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shortTemplateEntry")) return false;
    if (!nextTokenIs(b, SHORT_TEMPLATE_ENTRY_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, SHORT_TEMPLATE_ENTRY_START);
    p = r; // pin = 1
    r = r && shortTemplateEntry_1(b, l + 1);
    exit_section_(b, l, m, SHORT_TEMPLATE_ENTRY, r, p, null);
    return r || p;
  }

  // thisExpression | referenceExpression
  private static boolean shortTemplateEntry_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shortTemplateEntry_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = thisExpression(b, l + 1);
    if (!r) r = referenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'show' libraryReferenceList
  public static boolean showCombinator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "showCombinator")) return false;
    if (!nextTokenIs(b, SHOW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, SHOW);
    p = r; // pin = 1
    r = r && libraryReferenceList(b, l + 1);
    exit_section_(b, l, m, SHOW_COMBINATOR, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // declaredIdentifier | metadata* componentName
  public static boolean simpleFormalParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFormalParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<simple formal parameter>");
    r = declaredIdentifier(b, l + 1);
    if (!r) r = simpleFormalParameter_1(b, l + 1);
    exit_section_(b, l, m, SIMPLE_FORMAL_PARAMETER, r, false, null);
    return r;
  }

  // metadata* componentName
  private static boolean simpleFormalParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFormalParameter_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleFormalParameter_1_0(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean simpleFormalParameter_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFormalParameter_1_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleFormalParameter_1_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // referenceExpression qualifiedReferenceExpression*
  public static boolean simpleQualifiedReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleQualifiedReferenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<simple qualified reference expression>");
    r = referenceExpression(b, l + 1);
    r = r && simpleQualifiedReferenceExpression_1(b, l + 1);
    exit_section_(b, l, m, REFERENCE_EXPRESSION, r, false, null);
    return r;
  }

  // qualifiedReferenceExpression*
  private static boolean simpleQualifiedReferenceExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleQualifiedReferenceExpression_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!qualifiedReferenceExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleQualifiedReferenceExpression_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // !'}'
  static boolean simple_scope_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simple_scope_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !consumeToken(b, RBRACE);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (superclass mixins?)? interfaces? ('native' stringLiteralExpression?)? classBody?
  static boolean standardClassDeclarationTail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = standardClassDeclarationTail_0(b, l + 1);
    r = r && standardClassDeclarationTail_1(b, l + 1);
    r = r && standardClassDeclarationTail_2(b, l + 1);
    r = r && standardClassDeclarationTail_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (superclass mixins?)?
  private static boolean standardClassDeclarationTail_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_0")) return false;
    standardClassDeclarationTail_0_0(b, l + 1);
    return true;
  }

  // superclass mixins?
  private static boolean standardClassDeclarationTail_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = superclass(b, l + 1);
    r = r && standardClassDeclarationTail_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // mixins?
  private static boolean standardClassDeclarationTail_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_0_0_1")) return false;
    mixins(b, l + 1);
    return true;
  }

  // interfaces?
  private static boolean standardClassDeclarationTail_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_1")) return false;
    interfaces(b, l + 1);
    return true;
  }

  // ('native' stringLiteralExpression?)?
  private static boolean standardClassDeclarationTail_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_2")) return false;
    standardClassDeclarationTail_2_0(b, l + 1);
    return true;
  }

  // 'native' stringLiteralExpression?
  private static boolean standardClassDeclarationTail_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NATIVE);
    r = r && standardClassDeclarationTail_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // stringLiteralExpression?
  private static boolean standardClassDeclarationTail_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_2_0_1")) return false;
    stringLiteralExpression(b, l + 1);
    return true;
  }

  // classBody?
  private static boolean standardClassDeclarationTail_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_3")) return false;
    classBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // label* nonLabelledStatement
  static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement_0(b, l + 1);
    r = r && nonLabelledStatement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // label*
  private static boolean statement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!label(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "statement_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // (varDeclarationList | expression) ';'
  static boolean statementFollowedBySemiColon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementFollowedBySemiColon")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = statementFollowedBySemiColon_0(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // varDeclarationList | expression
  private static boolean statementFollowedBySemiColon_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementFollowedBySemiColon_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varDeclarationList(b, l + 1);
    if (!r) r = expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // statement*
  public static boolean statements(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statements")) return false;
    Marker m = enter_section_(b, l, _NONE_, "<statements>");
    int c = current_position_(b);
    while (true) {
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "statements", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, STATEMENTS, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // (RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING | stringTemplate)+
  public static boolean stringLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringLiteralExpression")) return false;
    if (!nextTokenIs(b, "<string literal expression>", OPEN_QUOTE, RAW_SINGLE_QUOTED_STRING, RAW_TRIPLE_QUOTED_STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<string literal expression>");
    r = stringLiteralExpression_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!stringLiteralExpression_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "stringLiteralExpression", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, STRING_LITERAL_EXPRESSION, r, false, null);
    return r;
  }

  // RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING | stringTemplate
  private static boolean stringLiteralExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringLiteralExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RAW_SINGLE_QUOTED_STRING);
    if (!r) r = consumeToken(b, RAW_TRIPLE_QUOTED_STRING);
    if (!r) r = stringTemplate(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // OPEN_QUOTE (REGULAR_STRING_PART | shortTemplateEntry | longTemplateEntry)* CLOSING_QUOTE
  static boolean stringTemplate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringTemplate")) return false;
    if (!nextTokenIs(b, OPEN_QUOTE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, OPEN_QUOTE);
    p = r; // pin = 1
    r = r && report_error_(b, stringTemplate_1(b, l + 1));
    r = p && consumeToken(b, CLOSING_QUOTE) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // (REGULAR_STRING_PART | shortTemplateEntry | longTemplateEntry)*
  private static boolean stringTemplate_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringTemplate_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!stringTemplate_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "stringTemplate_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // REGULAR_STRING_PART | shortTemplateEntry | longTemplateEntry
  private static boolean stringTemplate_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringTemplate_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REGULAR_STRING_PART);
    if (!r) r = shortTemplateEntry(b, l + 1);
    if (!r) r = longTemplateEntry(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '--' | '++'
  public static boolean suffixExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "suffixExpression")) return false;
    if (!nextTokenIs(b, "<suffix expression>", PLUS_PLUS, MINUS_MINUS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, "<suffix expression>");
    r = consumeToken(b, MINUS_MINUS);
    if (!r) r = consumeToken(b, PLUS_PLUS);
    exit_section_(b, l, m, SUFFIX_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // valueExpression suffixExpression*
  static boolean suffixExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "suffixExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = valueExpression(b, l + 1);
    r = r && suffixExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // suffixExpression*
  private static boolean suffixExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "suffixExpressionWrapper_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!suffixExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "suffixExpressionWrapper_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // ('super' | 'this') ('.' referenceExpression)? arguments
  //                               | fieldInitializer
  public static boolean superCallOrFieldInitializer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<super call or field initializer>");
    r = superCallOrFieldInitializer_0(b, l + 1);
    if (!r) r = fieldInitializer(b, l + 1);
    exit_section_(b, l, m, SUPER_CALL_OR_FIELD_INITIALIZER, r, false, super_call_or_field_initializer_recover_parser_);
    return r;
  }

  // ('super' | 'this') ('.' referenceExpression)? arguments
  private static boolean superCallOrFieldInitializer_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = superCallOrFieldInitializer_0_0(b, l + 1);
    r = r && superCallOrFieldInitializer_0_1(b, l + 1);
    r = r && arguments(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'super' | 'this'
  private static boolean superCallOrFieldInitializer_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SUPER);
    if (!r) r = consumeToken(b, THIS);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('.' referenceExpression)?
  private static boolean superCallOrFieldInitializer_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer_0_1")) return false;
    superCallOrFieldInitializer_0_1_0(b, l + 1);
    return true;
  }

  // '.' referenceExpression
  private static boolean superCallOrFieldInitializer_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && referenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'super'
  public static boolean superExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superExpression")) return false;
    if (!nextTokenIs(b, SUPER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SUPER);
    exit_section_(b, m, SUPER_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> | ',' | ':' | ';' | '=>' | '@' | 'abstract' | 'async' | 'class' | 'const' |
  //                                                       'export' | 'external' | 'factory' | 'final' | 'get' | 'import' | 'library' |
  //                                                       'native' | 'operator' | 'part' | 'set' | 'static' | 'sync' | 'typedef' | 'var' | 'void' | '{' |
  //                                                       '}' )
  static boolean super_call_or_field_initializer_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "super_call_or_field_initializer_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !super_call_or_field_initializer_recover_0(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  // <<nonStrictID>> | ',' | ':' | ';' | '=>' | '@' | 'abstract' | 'async' | 'class' | 'const' |
  //                                                       'export' | 'external' | 'factory' | 'final' | 'get' | 'import' | 'library' |
  //                                                       'native' | 'operator' | 'part' | 'set' | 'static' | 'sync' | 'typedef' | 'var' | 'void' | '{' |
  //                                                       '}'
  private static boolean super_call_or_field_initializer_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "super_call_or_field_initializer_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, EXPRESSION_BODY_DEF);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, ASYNC);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FACTORY);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, NATIVE);
    if (!r) r = consumeToken(b, OPERATOR);
    if (!r) r = consumeToken(b, PART);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, SYNC);
    if (!r) r = consumeToken(b, TYPEDEF);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    if (!r) r = consumeToken(b, LBRACE);
    if (!r) r = consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'extends' type
  public static boolean superclass(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superclass")) return false;
    if (!nextTokenIs(b, EXTENDS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, EXTENDS);
    p = r; // pin = 1
    r = r && type(b, l + 1);
    exit_section_(b, l, m, SUPERCLASS, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // label* 'case' expression ':' statements
  public static boolean switchCase(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCase")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<switch case>");
    r = switchCase_0(b, l + 1);
    r = r && consumeToken(b, CASE);
    p = r; // pin = 2
    r = r && report_error_(b, expression(b, l + 1));
    r = p && report_error_(b, consumeToken(b, COLON)) && r;
    r = p && statements(b, l + 1) && r;
    exit_section_(b, l, m, SWITCH_CASE, r, p, null);
    return r || p;
  }

  // label*
  private static boolean switchCase_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCase_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!label(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchCase_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // 'switch' '(' expressionWithRecoverUntilParen ')' '{' switchCase* defaultCase? '}'
  public static boolean switchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement")) return false;
    if (!nextTokenIs(b, SWITCH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, SWITCH);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, LPAREN));
    r = p && report_error_(b, expressionWithRecoverUntilParen(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && report_error_(b, consumeToken(b, LBRACE)) && r;
    r = p && report_error_(b, switchStatement_5(b, l + 1)) && r;
    r = p && report_error_(b, switchStatement_6(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, SWITCH_STATEMENT, r, p, null);
    return r || p;
  }

  // switchCase*
  private static boolean switchStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_5")) return false;
    int c = current_position_(b);
    while (true) {
      if (!switchCase(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchStatement_5", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // defaultCase?
  private static boolean switchStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_6")) return false;
    defaultCase(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '#' ('void' | userDefinableOperator | simpleQualifiedReferenceExpression)
  public static boolean symbolLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolLiteralExpression")) return false;
    if (!nextTokenIs(b, HASH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, HASH);
    p = r; // pin = 1
    r = r && symbolLiteralExpression_1(b, l + 1);
    exit_section_(b, l, m, SYMBOL_LITERAL_EXPRESSION, r, p, null);
    return r || p;
  }

  // 'void' | userDefinableOperator | simpleQualifiedReferenceExpression
  private static boolean symbolLiteralExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolLiteralExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VOID);
    if (!r) r = userDefinableOperator(b, l + 1);
    if (!r) r = simpleQualifiedReferenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '?' expression ':' ternaryExpressionWrapper
  public static boolean ternaryExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryExpression")) return false;
    if (!nextTokenIs(b, QUEST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, null);
    r = consumeToken(b, QUEST);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && ternaryExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, TERNARY_EXPRESSION, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ifNullExpressionWrapper ternaryExpression?
  static boolean ternaryExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ifNullExpressionWrapper(b, l + 1);
    r = r && ternaryExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ternaryExpression?
  private static boolean ternaryExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryExpressionWrapper_1")) return false;
    ternaryExpression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'this'
  public static boolean thisExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "thisExpression")) return false;
    if (!nextTokenIs(b, THIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, THIS);
    exit_section_(b, m, THIS_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // 'throw' expression
  public static boolean throwExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "throwExpression")) return false;
    if (!nextTokenIs(b, THROW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, THROW);
    r = r && expression(b, l + 1);
    exit_section_(b, m, THROW_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // libraryStatement
  //                              | partOfStatement
  //                              | importStatement
  //                              | exportStatement
  //                              | partStatement
  //                              | classDefinition
  //                              | enumDefinition
  //                              | functionTypeAlias
  //                              | getterOrSetterDeclaration
  //                              | functionDeclarationWithBodyOrNative
  //                              | varDeclarationListWithSemicolon
  //                              | incompleteDeclaration
  static boolean topLevelDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "topLevelDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = libraryStatement(b, l + 1);
    if (!r) r = partOfStatement(b, l + 1);
    if (!r) r = importStatement(b, l + 1);
    if (!r) r = exportStatement(b, l + 1);
    if (!r) r = partStatement(b, l + 1);
    if (!r) r = classDefinition(b, l + 1);
    if (!r) r = enumDefinition(b, l + 1);
    if (!r) r = functionTypeAlias(b, l + 1);
    if (!r) r = getterOrSetterDeclaration(b, l + 1);
    if (!r) r = functionDeclarationWithBodyOrNative(b, l + 1);
    if (!r) r = varDeclarationListWithSemicolon(b, l + 1);
    if (!r) r = incompleteDeclaration(b, l + 1);
    exit_section_(b, l, m, null, r, false, top_level_recover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> | '@' | 'abstract' | 'class' | 'const' | 'enum' | 'export' | 'external' | 'final' | 'get' | 'import' | 'library' | 'part' | 'set' | 'static' | 'typedef' | 'var' | 'void')
  static boolean top_level_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_level_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !top_level_recover_0(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  // <<nonStrictID>> | '@' | 'abstract' | 'class' | 'const' | 'enum' | 'export' | 'external' | 'final' | 'get' | 'import' | 'library' | 'part' | 'set' | 'static' | 'typedef' | 'var' | 'void'
  private static boolean top_level_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_level_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, PART);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, TYPEDEF);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'try' block (onPart+ finallyPart? | finallyPart)
  public static boolean tryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryStatement")) return false;
    if (!nextTokenIs(b, TRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, TRY);
    p = r; // pin = 1
    r = r && report_error_(b, block(b, l + 1));
    r = p && tryStatement_2(b, l + 1) && r;
    exit_section_(b, l, m, TRY_STATEMENT, r, p, null);
    return r || p;
  }

  // onPart+ finallyPart? | finallyPart
  private static boolean tryStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryStatement_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tryStatement_2_0(b, l + 1);
    if (!r) r = finallyPart(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // onPart+ finallyPart?
  private static boolean tryStatement_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryStatement_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tryStatement_2_0_0(b, l + 1);
    r = r && tryStatement_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // onPart+
  private static boolean tryStatement_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryStatement_2_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onPart(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!onPart(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "tryStatement_2_0_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // finallyPart?
  private static boolean tryStatement_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryStatement_2_0_1")) return false;
    finallyPart(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // simpleQualifiedReferenceExpression typeArguments?
  public static boolean type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<type>");
    r = simpleQualifiedReferenceExpression(b, l + 1);
    p = r; // pin = 1
    r = r && type_1(b, l + 1);
    exit_section_(b, l, m, TYPE, r, p, null);
    return r || p;
  }

  // typeArguments?
  private static boolean type_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '<' typeList '>'
  public static boolean typeArguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeArguments")) return false;
    if (!nextTokenIs(b, LT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && typeList(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, TYPE_ARGUMENTS, r);
    return r;
  }

  /* ********************************************************** */
  // type (',' type)*
  public static boolean typeList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<type list>");
    r = type(b, l + 1);
    r = r && typeList_1(b, l + 1);
    exit_section_(b, l, m, TYPE_LIST, r, false, null);
    return r;
  }

  // (',' type)*
  private static boolean typeList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!typeList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' type
  private static boolean typeList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* componentName ('extends' type)?
  public static boolean typeParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<type parameter>");
    r = typeParameter_0(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && typeParameter_2(b, l + 1);
    exit_section_(b, l, m, TYPE_PARAMETER, r, false, type_parameter_recover_parser_);
    return r;
  }

  // metadata*
  private static boolean typeParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameter_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeParameter_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ('extends' type)?
  private static boolean typeParameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameter_2")) return false;
    typeParameter_2_0(b, l + 1);
    return true;
  }

  // 'extends' type
  private static boolean typeParameter_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameter_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTENDS);
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<' typeParameter (',' typeParameter)* '>'
  public static boolean typeParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameters")) return false;
    if (!nextTokenIs(b, LT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, LT);
    p = r; // pin = 1
    r = r && report_error_(b, typeParameter(b, l + 1));
    r = p && report_error_(b, typeParameters_2(b, l + 1)) && r;
    r = p && consumeToken(b, GT) && r;
    exit_section_(b, l, m, TYPE_PARAMETERS, r, p, null);
    return r || p;
  }

  // (',' typeParameter)*
  private static boolean typeParameters_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameters_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!typeParameters_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeParameters_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' typeParameter
  private static boolean typeParameters_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameters_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && typeParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> | '(' | ',' | '=' | '>' | '@' | 'abstract' | 'class' | 'const' | 'export' | 'extends' |
  //                                      'external' | 'final' | 'get' | 'implements' | 'import' | 'library' | 'native' | 'part' | 'set' |
  //                                      'static' | 'typedef' | 'var' | 'void' | '{')
  static boolean type_parameter_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_parameter_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !type_parameter_recover_0(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  // <<nonStrictID>> | '(' | ',' | '=' | '>' | '@' | 'abstract' | 'class' | 'const' | 'export' | 'extends' |
  //                                      'external' | 'final' | 'get' | 'implements' | 'import' | 'library' | 'native' | 'part' | 'set' |
  //                                      'static' | 'typedef' | 'var' | 'void' | '{'
  private static boolean type_parameter_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_parameter_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    if (!r) r = consumeToken(b, LPAREN);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, EXTENDS);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, IMPLEMENTS);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, NATIVE);
    if (!r) r = consumeToken(b, PART);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, TYPEDEF);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    if (!r) r = consumeToken(b, LBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // stringLiteralExpression
  public static boolean uriElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uriElement")) return false;
    if (!nextTokenIs(b, "<uri element>", OPEN_QUOTE, RAW_SINGLE_QUOTED_STRING, RAW_TRIPLE_QUOTED_STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<uri element>");
    r = stringLiteralExpression(b, l + 1);
    exit_section_(b, l, m, URI_ELEMENT, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // binaryOperator |
  //                           '~' |
  //                           '[' ']' '='?
  public static boolean userDefinableOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "userDefinableOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<user definable operator>");
    r = binaryOperator(b, l + 1);
    if (!r) r = consumeToken(b, BIN_NOT);
    if (!r) r = userDefinableOperator_2(b, l + 1);
    exit_section_(b, l, m, USER_DEFINABLE_OPERATOR, r, false, null);
    return r;
  }

  // '[' ']' '='?
  private static boolean userDefinableOperator_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "userDefinableOperator_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && consumeToken(b, RBRACKET);
    r = r && userDefinableOperator_2_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '='?
  private static boolean userDefinableOperator_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "userDefinableOperator_2_2")) return false;
    consumeToken(b, EQ);
    return true;
  }

  /* ********************************************************** */
  // primary callOrArrayAccessOrQualifiedRefExpression (isExpression | asExpression)? cascadeReferenceExpression*
  public static boolean valueExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "valueExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<value expression>");
    r = primary(b, l + 1);
    r = r && callOrArrayAccessOrQualifiedRefExpression(b, l + 1);
    r = r && valueExpression_2(b, l + 1);
    r = r && valueExpression_3(b, l + 1);
    exit_section_(b, l, m, VALUE_EXPRESSION, r, false, null);
    return r;
  }

  // (isExpression | asExpression)?
  private static boolean valueExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "valueExpression_2")) return false;
    valueExpression_2_0(b, l + 1);
    return true;
  }

  // isExpression | asExpression
  private static boolean valueExpression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "valueExpression_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isExpression(b, l + 1);
    if (!r) r = asExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // cascadeReferenceExpression*
  private static boolean valueExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "valueExpression_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!cascadeReferenceExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "valueExpression_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* 'static'? (finalOrConst type componentName | finalOrConst componentName <<failIfItLooksLikeConstantObjectExpression>> | type !asExpression componentName | 'var' componentName)
  public static boolean varAccessDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<var access declaration>");
    r = varAccessDeclaration_0(b, l + 1);
    r = r && varAccessDeclaration_1(b, l + 1);
    r = r && varAccessDeclaration_2(b, l + 1);
    exit_section_(b, l, m, VAR_ACCESS_DECLARATION, r, false, null);
    return r;
  }

  // metadata*
  private static boolean varAccessDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "varAccessDeclaration_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'static'?
  private static boolean varAccessDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_1")) return false;
    consumeToken(b, STATIC);
    return true;
  }

  // finalOrConst type componentName | finalOrConst componentName <<failIfItLooksLikeConstantObjectExpression>> | type !asExpression componentName | 'var' componentName
  private static boolean varAccessDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varAccessDeclaration_2_0(b, l + 1);
    if (!r) r = varAccessDeclaration_2_1(b, l + 1);
    if (!r) r = varAccessDeclaration_2_2(b, l + 1);
    if (!r) r = varAccessDeclaration_2_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // finalOrConst type componentName
  private static boolean varAccessDeclaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalOrConst(b, l + 1);
    r = r && type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // finalOrConst componentName <<failIfItLooksLikeConstantObjectExpression>>
  private static boolean varAccessDeclaration_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalOrConst(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && failIfItLooksLikeConstantObjectExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // type !asExpression componentName
  private static boolean varAccessDeclaration_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_2_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type(b, l + 1);
    r = r && varAccessDeclaration_2_2_1(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !asExpression
  private static boolean varAccessDeclaration_2_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_2_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !asExpression(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  // 'var' componentName
  private static boolean varAccessDeclaration_2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_2_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VAR);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // varAccessDeclaration varInit? (',' varDeclarationListPart)*
  public static boolean varDeclarationList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<var declaration list>");
    r = varAccessDeclaration(b, l + 1);
    r = r && varDeclarationList_1(b, l + 1);
    r = r && varDeclarationList_2(b, l + 1);
    exit_section_(b, l, m, VAR_DECLARATION_LIST, r, false, null);
    return r;
  }

  // varInit?
  private static boolean varDeclarationList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationList_1")) return false;
    varInit(b, l + 1);
    return true;
  }

  // (',' varDeclarationListPart)*
  private static boolean varDeclarationList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationList_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!varDeclarationList_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "varDeclarationList_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' varDeclarationListPart
  private static boolean varDeclarationList_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationList_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && varDeclarationListPart(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // componentName varInit?
  public static boolean varDeclarationListPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationListPart")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<var declaration list part>");
    r = componentName(b, l + 1);
    r = r && varDeclarationListPart_1(b, l + 1);
    exit_section_(b, l, m, VAR_DECLARATION_LIST_PART, r, false, null);
    return r;
  }

  // varInit?
  private static boolean varDeclarationListPart_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationListPart_1")) return false;
    varInit(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // varDeclarationList ';'
  static boolean varDeclarationListWithSemicolon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationListWithSemicolon")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = varDeclarationList(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '=' type ['.' referenceExpression]
  static boolean varFactoryDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varFactoryDeclaration")) return false;
    if (!nextTokenIs(b, EQ)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, EQ);
    p = r; // pin = 1
    r = r && report_error_(b, type(b, l + 1));
    r = p && varFactoryDeclaration_2(b, l + 1) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // ['.' referenceExpression]
  private static boolean varFactoryDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varFactoryDeclaration_2")) return false;
    varFactoryDeclaration_2_0(b, l + 1);
    return true;
  }

  // '.' referenceExpression
  private static boolean varFactoryDeclaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varFactoryDeclaration_2_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, DOT);
    p = r; // pin = 1
    r = r && referenceExpression(b, l + 1);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '=' expression
  public static boolean varInit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varInit")) return false;
    if (!nextTokenIs(b, EQ)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, EQ);
    p = r; // pin = 1
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, VAR_INIT, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'while' '(' expressionWithRecoverUntilParen ')' statement
  public static boolean whileStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whileStatement")) return false;
    if (!nextTokenIs(b, WHILE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, WHILE);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, LPAREN));
    r = p && report_error_(b, expressionWithRecoverUntilParen(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && statement(b, l + 1) && r;
    exit_section_(b, l, m, WHILE_STATEMENT, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'yield' '*' expression ';'
  public static boolean yieldEachStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "yieldEachStatement")) return false;
    if (!nextTokenIs(b, YIELD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, YIELD);
    r = r && consumeToken(b, MUL);
    p = r; // pin = 2
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, YIELD_EACH_STATEMENT, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'yield' expression ';'
  public static boolean yieldStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "yieldStatement")) return false;
    if (!nextTokenIs(b, YIELD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, YIELD);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, YIELD_STATEMENT, r, p, null);
    return r || p;
  }

  final static Parser argument_list_part_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return argument_list_part_recover(b, l + 1);
    }
  };
  final static Parser argument_list_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return argument_list_recover(b, l + 1);
    }
  };
  final static Parser class_member_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return class_member_recover(b, l + 1);
    }
  };
  final static Parser default_formal_parameter_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return default_formal_parameter_recover(b, l + 1);
    }
  };
  final static Parser expression_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return expression_recover(b, l + 1);
    }
  };
  final static Parser for_loops_parts_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return for_loops_parts_recover(b, l + 1);
    }
  };
  final static Parser map_literal_entry_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return map_literal_entry_recover(b, l + 1);
    }
  };
  final static Parser not_paren_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return not_paren_recover(b, l + 1);
    }
  };
  final static Parser parenthesesRecovery_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return parenthesesRecovery(b, l + 1);
    }
  };
  final static Parser simple_scope_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return simple_scope_recover(b, l + 1);
    }
  };
  final static Parser super_call_or_field_initializer_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return super_call_or_field_initializer_recover(b, l + 1);
    }
  };
  final static Parser top_level_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return top_level_recover(b, l + 1);
    }
  };
  final static Parser type_parameter_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return type_parameter_recover(b, l + 1);
    }
  };
}
