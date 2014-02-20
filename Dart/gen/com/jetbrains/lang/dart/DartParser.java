// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.openapi.diagnostic.Logger;
import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartGeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class DartParser implements PsiParser {

  public static final Logger LOG_ = Logger.getInstance("com.jetbrains.lang.dart.DartParser");

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, EXTENDS_SETS_);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    if (root_ == ADDITIVE_EXPRESSION) {
      result_ = additiveExpression(builder_, 0);
    }
    else if (root_ == ADDITIVE_OPERATOR) {
      result_ = additiveOperator(builder_, 0);
    }
    else if (root_ == ARGUMENT_LIST) {
      result_ = argumentList(builder_, 0);
    }
    else if (root_ == ARGUMENTS) {
      result_ = arguments(builder_, 0);
    }
    else if (root_ == ARRAY_ACCESS_EXPRESSION) {
      result_ = arrayAccessExpression(builder_, 0);
    }
    else if (root_ == AS_EXPRESSION) {
      result_ = asExpression(builder_, 0);
    }
    else if (root_ == ASSERT_STATEMENT) {
      result_ = assertStatement(builder_, 0);
    }
    else if (root_ == ASSIGN_EXPRESSION) {
      result_ = assignExpression(builder_, 0);
    }
    else if (root_ == ASSIGNMENT_OPERATOR) {
      result_ = assignmentOperator(builder_, 0);
    }
    else if (root_ == BITWISE_EXPRESSION) {
      result_ = bitwiseExpression(builder_, 0);
    }
    else if (root_ == BITWISE_OPERATOR) {
      result_ = bitwiseOperator(builder_, 0);
    }
    else if (root_ == BLOCK) {
      result_ = block(builder_, 0);
    }
    else if (root_ == BREAK_STATEMENT) {
      result_ = breakStatement(builder_, 0);
    }
    else if (root_ == CALL_EXPRESSION) {
      result_ = callExpression(builder_, 0);
    }
    else if (root_ == CASCADE_REFERENCE_EXPRESSION) {
      result_ = cascadeReferenceExpression(builder_, 0);
    }
    else if (root_ == CATCH_PART) {
      result_ = catchPart(builder_, 0);
    }
    else if (root_ == CLASS_BODY) {
      result_ = classBody(builder_, 0);
    }
    else if (root_ == CLASS_DEFINITION) {
      result_ = classDefinition(builder_, 0);
    }
    else if (root_ == CLASS_MEMBERS) {
      result_ = classMembers(builder_, 0);
    }
    else if (root_ == CLASS_TYPE_ALIAS) {
      result_ = classTypeAlias(builder_, 0);
    }
    else if (root_ == COMPARE_EXPRESSION) {
      result_ = compareExpression(builder_, 0);
    }
    else if (root_ == COMPONENT_NAME) {
      result_ = componentName(builder_, 0);
    }
    else if (root_ == CONST_CONSTRUCTOR_EXPRESSION) {
      result_ = constConstructorExpression(builder_, 0);
    }
    else if (root_ == CONTINUE_STATEMENT) {
      result_ = continueStatement(builder_, 0);
    }
    else if (root_ == DEFAULT_CASE) {
      result_ = defaultCase(builder_, 0);
    }
    else if (root_ == DEFAULT_FORMAL_NAMED_PARAMETER) {
      result_ = defaultFormalNamedParameter(builder_, 0);
    }
    else if (root_ == DO_WHILE_STATEMENT) {
      result_ = doWhileStatement(builder_, 0);
    }
    else if (root_ == EQUALITY_OPERATOR) {
      result_ = equalityOperator(builder_, 0);
    }
    else if (root_ == EXPORT_STATEMENT) {
      result_ = exportStatement(builder_, 0);
    }
    else if (root_ == EXPRESSION) {
      result_ = expression(builder_, 0);
    }
    else if (root_ == EXPRESSION_LIST) {
      result_ = expressionList(builder_, 0);
    }
    else if (root_ == FACTORY_CONSTRUCTOR_DECLARATION) {
      result_ = factoryConstructorDeclaration(builder_, 0);
    }
    else if (root_ == FIELD_FORMAL_PARAMETER) {
      result_ = fieldFormalParameter(builder_, 0);
    }
    else if (root_ == FIELD_INITIALIZER) {
      result_ = fieldInitializer(builder_, 0);
    }
    else if (root_ == FINALLY_PART) {
      result_ = finallyPart(builder_, 0);
    }
    else if (root_ == FOR_IN_PART) {
      result_ = forInPart(builder_, 0);
    }
    else if (root_ == FOR_LOOP_PARTS) {
      result_ = forLoopParts(builder_, 0);
    }
    else if (root_ == FOR_LOOP_PARTS_IN_BRACES) {
      result_ = forLoopPartsInBraces(builder_, 0);
    }
    else if (root_ == FOR_STATEMENT) {
      result_ = forStatement(builder_, 0);
    }
    else if (root_ == FORMAL_PARAMETER_LIST) {
      result_ = formalParameterList(builder_, 0);
    }
    else if (root_ == FUNCTION_BODY) {
      result_ = functionBody(builder_, 0);
    }
    else if (root_ == FUNCTION_DECLARATION_WITH_BODY) {
      result_ = functionDeclarationWithBody(builder_, 0);
    }
    else if (root_ == FUNCTION_DECLARATION_WITH_BODY_OR_NATIVE) {
      result_ = functionDeclarationWithBodyOrNative(builder_, 0);
    }
    else if (root_ == FUNCTION_EXPRESSION) {
      result_ = functionExpression(builder_, 0);
    }
    else if (root_ == FUNCTION_EXPRESSION_BODY) {
      result_ = functionExpressionBody(builder_, 0);
    }
    else if (root_ == FUNCTION_SIGNATURE) {
      result_ = functionSignature(builder_, 0);
    }
    else if (root_ == FUNCTION_TYPE_ALIAS) {
      result_ = functionTypeAlias(builder_, 0);
    }
    else if (root_ == GETTER_DECLARATION) {
      result_ = getterDeclaration(builder_, 0);
    }
    else if (root_ == HIDE_COMBINATOR) {
      result_ = hideCombinator(builder_, 0);
    }
    else if (root_ == ID) {
      result_ = id(builder_, 0);
    }
    else if (root_ == IF_STATEMENT) {
      result_ = ifStatement(builder_, 0);
    }
    else if (root_ == IMPORT_STATEMENT) {
      result_ = importStatement(builder_, 0);
    }
    else if (root_ == INCOMPLETE_DECLARATION) {
      result_ = incompleteDeclaration(builder_, 0);
    }
    else if (root_ == INITIALIZERS) {
      result_ = initializers(builder_, 0);
    }
    else if (root_ == INTERFACES) {
      result_ = interfaces(builder_, 0);
    }
    else if (root_ == IS_EXPRESSION) {
      result_ = isExpression(builder_, 0);
    }
    else if (root_ == LABEL) {
      result_ = label(builder_, 0);
    }
    else if (root_ == LIBRARY_COMPONENT_REFERENCE_EXPRESSION) {
      result_ = libraryComponentReferenceExpression(builder_, 0);
    }
    else if (root_ == LIBRARY_ID) {
      result_ = libraryId(builder_, 0);
    }
    else if (root_ == LIBRARY_REFERENCE_LIST) {
      result_ = libraryReferenceList(builder_, 0);
    }
    else if (root_ == LIBRARY_STATEMENT) {
      result_ = libraryStatement(builder_, 0);
    }
    else if (root_ == LIST_LITERAL_EXPRESSION) {
      result_ = listLiteralExpression(builder_, 0);
    }
    else if (root_ == LITERAL_EXPRESSION) {
      result_ = literalExpression(builder_, 0);
    }
    else if (root_ == LOGIC_AND_EXPRESSION) {
      result_ = logicAndExpression(builder_, 0);
    }
    else if (root_ == LOGIC_OR_EXPRESSION) {
      result_ = logicOrExpression(builder_, 0);
    }
    else if (root_ == LONG_TEMPLATE_ENTRY) {
      result_ = longTemplateEntry(builder_, 0);
    }
    else if (root_ == MAP_LITERAL_ENTRY) {
      result_ = mapLiteralEntry(builder_, 0);
    }
    else if (root_ == MAP_LITERAL_EXPRESSION) {
      result_ = mapLiteralExpression(builder_, 0);
    }
    else if (root_ == METADATA) {
      result_ = metadata(builder_, 0);
    }
    else if (root_ == METHOD_DECLARATION) {
      result_ = methodDeclaration(builder_, 0);
    }
    else if (root_ == MIXIN_APPLICATION) {
      result_ = mixinApplication(builder_, 0);
    }
    else if (root_ == MIXINS) {
      result_ = mixins(builder_, 0);
    }
    else if (root_ == MULTIPLICATIVE_EXPRESSION) {
      result_ = multiplicativeExpression(builder_, 0);
    }
    else if (root_ == MULTIPLICATIVE_OPERATOR) {
      result_ = multiplicativeOperator(builder_, 0);
    }
    else if (root_ == NAMED_ARGUMENT) {
      result_ = namedArgument(builder_, 0);
    }
    else if (root_ == NAMED_CONSTRUCTOR_DECLARATION) {
      result_ = namedConstructorDeclaration(builder_, 0);
    }
    else if (root_ == NAMED_FORMAL_PARAMETERS) {
      result_ = namedFormalParameters(builder_, 0);
    }
    else if (root_ == NEW_EXPRESSION) {
      result_ = newExpression(builder_, 0);
    }
    else if (root_ == NORMAL_FORMAL_PARAMETER) {
      result_ = normalFormalParameter(builder_, 0);
    }
    else if (root_ == ON_PART) {
      result_ = onPart(builder_, 0);
    }
    else if (root_ == OPERATOR_DECLARATION) {
      result_ = operatorDeclaration(builder_, 0);
    }
    else if (root_ == PARAMETER_NAME_REFERENCE_EXPRESSION) {
      result_ = parameterNameReferenceExpression(builder_, 0);
    }
    else if (root_ == PARENTHESIZED_EXPRESSION) {
      result_ = parenthesizedExpression(builder_, 0);
    }
    else if (root_ == PART_OF_STATEMENT) {
      result_ = partOfStatement(builder_, 0);
    }
    else if (root_ == PART_STATEMENT) {
      result_ = partStatement(builder_, 0);
    }
    else if (root_ == PATH_OR_LIBRARY_REFERENCE) {
      result_ = pathOrLibraryReference(builder_, 0);
    }
    else if (root_ == PREFIX_EXPRESSION) {
      result_ = prefixExpression(builder_, 0);
    }
    else if (root_ == PREFIX_OPERATOR) {
      result_ = prefixOperator(builder_, 0);
    }
    else if (root_ == QUALIFIED_COMPONENT_NAME) {
      result_ = qualifiedComponentName(builder_, 0);
    }
    else if (root_ == REDIRECTION) {
      result_ = redirection(builder_, 0);
    }
    else if (root_ == REFERENCE_EXPRESSION) {
      result_ = referenceExpression(builder_, 0);
    }
    else if (root_ == RELATIONAL_OPERATOR) {
      result_ = relationalOperator(builder_, 0);
    }
    else if (root_ == RETHROW_STATEMENT) {
      result_ = rethrowStatement(builder_, 0);
    }
    else if (root_ == RETURN_STATEMENT) {
      result_ = returnStatement(builder_, 0);
    }
    else if (root_ == RETURN_TYPE) {
      result_ = returnType(builder_, 0);
    }
    else if (root_ == SETTER_DECLARATION) {
      result_ = setterDeclaration(builder_, 0);
    }
    else if (root_ == SHIFT_EXPRESSION) {
      result_ = shiftExpression(builder_, 0);
    }
    else if (root_ == SHIFT_OPERATOR) {
      result_ = shiftOperator(builder_, 0);
    }
    else if (root_ == SHIFT_RIGHT_OPERATOR) {
      result_ = shiftRightOperator(builder_, 0);
    }
    else if (root_ == SHORT_TEMPLATE_ENTRY) {
      result_ = shortTemplateEntry(builder_, 0);
    }
    else if (root_ == SHOW_COMBINATOR) {
      result_ = showCombinator(builder_, 0);
    }
    else if (root_ == SIMPLE_FORMAL_PARAMETER) {
      result_ = simpleFormalParameter(builder_, 0);
    }
    else if (root_ == STATEMENTS) {
      result_ = statements(builder_, 0);
    }
    else if (root_ == STRING_LITERAL_EXPRESSION) {
      result_ = stringLiteralExpression(builder_, 0);
    }
    else if (root_ == SUFFIX_EXPRESSION) {
      result_ = suffixExpression(builder_, 0);
    }
    else if (root_ == SUPER_CALL_OR_FIELD_INITIALIZER) {
      result_ = superCallOrFieldInitializer(builder_, 0);
    }
    else if (root_ == SUPER_EXPRESSION) {
      result_ = superExpression(builder_, 0);
    }
    else if (root_ == SUPERCLASS) {
      result_ = superclass(builder_, 0);
    }
    else if (root_ == SWITCH_CASE) {
      result_ = switchCase(builder_, 0);
    }
    else if (root_ == SWITCH_STATEMENT) {
      result_ = switchStatement(builder_, 0);
    }
    else if (root_ == SYMBOL_LITERAL_EXPRESSION) {
      result_ = symbolLiteralExpression(builder_, 0);
    }
    else if (root_ == TERNARY_EXPRESSION) {
      result_ = ternaryExpression(builder_, 0);
    }
    else if (root_ == THIS_EXPRESSION) {
      result_ = thisExpression(builder_, 0);
    }
    else if (root_ == THROW_STATEMENT) {
      result_ = throwStatement(builder_, 0);
    }
    else if (root_ == TRY_STATEMENT) {
      result_ = tryStatement(builder_, 0);
    }
    else if (root_ == TYPE) {
      result_ = type(builder_, 0);
    }
    else if (root_ == TYPE_ARGUMENTS) {
      result_ = typeArguments(builder_, 0);
    }
    else if (root_ == TYPE_LIST) {
      result_ = typeList(builder_, 0);
    }
    else if (root_ == TYPE_PARAMETER) {
      result_ = typeParameter(builder_, 0);
    }
    else if (root_ == TYPE_PARAMETERS) {
      result_ = typeParameters(builder_, 0);
    }
    else if (root_ == USER_DEFINABLE_OPERATOR) {
      result_ = userDefinableOperator(builder_, 0);
    }
    else if (root_ == VALUE_EXPRESSION) {
      result_ = valueExpression(builder_, 0);
    }
    else if (root_ == VAR_ACCESS_DECLARATION) {
      result_ = varAccessDeclaration(builder_, 0);
    }
    else if (root_ == VAR_DECLARATION_LIST) {
      result_ = varDeclarationList(builder_, 0);
    }
    else if (root_ == VAR_DECLARATION_LIST_PART) {
      result_ = varDeclarationListPart(builder_, 0);
    }
    else if (root_ == VAR_INIT) {
      result_ = varInit(builder_, 0);
    }
    else if (root_ == WHILE_STATEMENT) {
      result_ = whileStatement(builder_, 0);
    }
    else {
      result_ = parse_root_(root_, builder_, 0);
    }
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
    return builder_.getTreeBuilt();
  }

  protected boolean parse_root_(final IElementType root_, final PsiBuilder builder_, final int level_) {
    return dartUnit(builder_, level_ + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(ADDITIVE_EXPRESSION, ARRAY_ACCESS_EXPRESSION, ASSIGN_EXPRESSION, AS_EXPRESSION,
      BITWISE_EXPRESSION, CALL_EXPRESSION, CASCADE_REFERENCE_EXPRESSION, COMPARE_EXPRESSION,
      CONST_CONSTRUCTOR_EXPRESSION, EXPRESSION, FUNCTION_EXPRESSION, IS_EXPRESSION,
      LIBRARY_COMPONENT_REFERENCE_EXPRESSION, LIST_LITERAL_EXPRESSION, LITERAL_EXPRESSION, LOGIC_AND_EXPRESSION,
      LOGIC_OR_EXPRESSION, MAP_LITERAL_EXPRESSION, MULTIPLICATIVE_EXPRESSION, NEW_EXPRESSION,
      PARAMETER_NAME_REFERENCE_EXPRESSION, PARENTHESIZED_EXPRESSION, PREFIX_EXPRESSION, REFERENCE_EXPRESSION,
      SHIFT_EXPRESSION, STRING_LITERAL_EXPRESSION, SUFFIX_EXPRESSION, SUPER_EXPRESSION,
      SYMBOL_LITERAL_EXPRESSION, TERNARY_EXPRESSION, THIS_EXPRESSION, VALUE_EXPRESSION),
  };

  /* ********************************************************** */
  // additiveOperator multiplicativeExpressionWrapper
  public static boolean additiveExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additiveExpression")) return false;
    if (!nextTokenIs(builder_, "<additive expression>", PLUS, MINUS)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, "<additive expression>");
    result_ = additiveOperator(builder_, level_ + 1);
    result_ = result_ && multiplicativeExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, ADDITIVE_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // multiplicativeExpressionWrapper additiveExpression*
  static boolean additiveExpressionWrapper(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additiveExpressionWrapper")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = multiplicativeExpressionWrapper(builder_, level_ + 1);
    result_ = result_ && additiveExpressionWrapper_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // additiveExpression*
  private static boolean additiveExpressionWrapper_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additiveExpressionWrapper_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!additiveExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "additiveExpressionWrapper_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // '+' | '-'
  public static boolean additiveOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "additiveOperator")) return false;
    if (!nextTokenIs(builder_, "<additive operator>", PLUS, MINUS)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<additive operator>");
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    exit_section_(builder_, level_, marker_, ADDITIVE_OPERATOR, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // argumentListPart (',' argumentListPart)*
  public static boolean argumentList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argumentList")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<argument list>");
    result_ = argumentListPart(builder_, level_ + 1);
    result_ = result_ && argumentList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, ARGUMENT_LIST, result_, false, argument_list_recover_parser_);
    return result_;
  }

  // (',' argumentListPart)*
  private static boolean argumentList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argumentList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!argumentList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "argumentList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' argumentListPart
  private static boolean argumentList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argumentList_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && argumentListPart(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // namedArgument | expression
  static boolean argumentListPart(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argumentListPart")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = namedArgument(builder_, level_ + 1);
    if (!result_) result_ = expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, argument_list_part_recover_parser_);
    return result_;
  }

  /* ********************************************************** */
  // !(')' | ',')
  static boolean argument_list_part_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argument_list_part_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !argument_list_part_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // ')' | ','
  private static boolean argument_list_part_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argument_list_part_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RPAREN);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(')')
  static boolean argument_list_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argument_list_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !argument_list_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // (')')
  private static boolean argument_list_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argument_list_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '(' argumentList? ')'
  public static boolean arguments(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arguments")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && arguments_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, ARGUMENTS, result_);
    return result_;
  }

  // argumentList?
  private static boolean arguments_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arguments_1")) return false;
    argumentList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // '[' expression? ']'
  static boolean arrayAccess(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arrayAccess")) return false;
    if (!nextTokenIs(builder_, LBRACKET)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LBRACKET);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, arrayAccess_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACKET) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // expression?
  private static boolean arrayAccess_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arrayAccess_1")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // arrayAccess
  public static boolean arrayAccessExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arrayAccessExpression")) return false;
    if (!nextTokenIs(builder_, LBRACKET)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = arrayAccess(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, ARRAY_ACCESS_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '=>' (expression | throwStatement)
  static boolean arrowBody(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arrowBody")) return false;
    if (!nextTokenIs(builder_, EXPRESSION_BODY_DEF)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, EXPRESSION_BODY_DEF);
    pinned_ = result_; // pin = 1
    result_ = result_ && arrowBody_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // expression | throwStatement
  private static boolean arrowBody_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arrowBody_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = expression(builder_, level_ + 1);
    if (!result_) result_ = throwStatement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // arrowBody ';'
  static boolean arrowBodyWithSemi(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "arrowBodyWithSemi")) return false;
    if (!nextTokenIs(builder_, EXPRESSION_BODY_DEF)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = arrowBody(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // 'as' type
  public static boolean asExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "asExpression")) return false;
    if (!nextTokenIs(builder_, AS)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = consumeToken(builder_, AS);
    result_ = result_ && type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, AS_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'assert' '(' expressionWithRecoverUntilParen ')' ';'
  public static boolean assertStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assertStatement")) return false;
    if (!nextTokenIs(builder_, ASSERT)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, ASSERT);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, LPAREN));
    result_ = pinned_ && report_error_(builder_, expressionWithRecoverUntilParen(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RPAREN)) && result_;
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, ASSERT_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // assignmentOperator ternaryExpressionWrapper
  public static boolean assignExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignExpression")) return false;
    if (!nextTokenIs(builder_, "<assign expression>", REM_EQ, AND_EQ,
      MUL_EQ, PLUS_EQ, MINUS_EQ, DIV_EQ, LT_LT_EQ, EQ,
      GT_GT_EQ, XOR_EQ, OR_EQ, INT_DIV_EQ)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, "<assign expression>");
    result_ = assignmentOperator(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && ternaryExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, ASSIGN_EXPRESSION, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // ternaryExpressionWrapper assignExpression*
  static boolean assignExpressionWrapper(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignExpressionWrapper")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ternaryExpressionWrapper(builder_, level_ + 1);
    result_ = result_ && assignExpressionWrapper_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // assignExpression*
  private static boolean assignExpressionWrapper_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignExpressionWrapper_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!assignExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "assignExpressionWrapper_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // '=' | '*=' | '/=' | '~/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|='
  public static boolean assignmentOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignmentOperator")) return false;
    if (!nextTokenIs(builder_, "<assignment operator>", REM_EQ, AND_EQ,
      MUL_EQ, PLUS_EQ, MINUS_EQ, DIV_EQ, LT_LT_EQ, EQ,
      GT_GT_EQ, XOR_EQ, OR_EQ, INT_DIV_EQ)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<assignment operator>");
    result_ = consumeToken(builder_, EQ);
    if (!result_) result_ = consumeToken(builder_, MUL_EQ);
    if (!result_) result_ = consumeToken(builder_, DIV_EQ);
    if (!result_) result_ = consumeToken(builder_, INT_DIV_EQ);
    if (!result_) result_ = consumeToken(builder_, REM_EQ);
    if (!result_) result_ = consumeToken(builder_, PLUS_EQ);
    if (!result_) result_ = consumeToken(builder_, MINUS_EQ);
    if (!result_) result_ = consumeToken(builder_, LT_LT_EQ);
    if (!result_) result_ = consumeToken(builder_, GT_GT_EQ);
    if (!result_) result_ = consumeToken(builder_, AND_EQ);
    if (!result_) result_ = consumeToken(builder_, XOR_EQ);
    if (!result_) result_ = consumeToken(builder_, OR_EQ);
    exit_section_(builder_, level_, marker_, ASSIGNMENT_OPERATOR, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // multiplicativeOperator |
  //                            additiveOperator |
  //                            shiftOperator|
  //                            relationalOperator|
  //                            '==' |
  //                            bitwiseOperator
  static boolean binaryOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "binaryOperator")) return false;
    if (!nextTokenIs(builder_, "", REM, AND,
      MUL, PLUS, MINUS, DIV, LT, LT_LT,
      LT_EQ, EQ_EQ, GT, GT_EQ, XOR, OR, INT_DIV)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = multiplicativeOperator(builder_, level_ + 1);
    if (!result_) result_ = additiveOperator(builder_, level_ + 1);
    if (!result_) result_ = shiftOperator(builder_, level_ + 1);
    if (!result_) result_ = relationalOperator(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, EQ_EQ);
    if (!result_) result_ = bitwiseOperator(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // bitwiseOperator shiftExpressionWrapper
  public static boolean bitwiseExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bitwiseExpression")) return false;
    if (!nextTokenIs(builder_, "<bitwise expression>", AND, XOR, OR)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, "<bitwise expression>");
    result_ = bitwiseOperator(builder_, level_ + 1);
    result_ = result_ && shiftExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, BITWISE_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // shiftExpressionWrapper bitwiseExpression*
  static boolean bitwiseExpressionWrapper(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bitwiseExpressionWrapper")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = shiftExpressionWrapper(builder_, level_ + 1);
    result_ = result_ && bitwiseExpressionWrapper_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // bitwiseExpression*
  private static boolean bitwiseExpressionWrapper_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bitwiseExpressionWrapper_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!bitwiseExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "bitwiseExpressionWrapper_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // '&' | '^' | '|'
  public static boolean bitwiseOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bitwiseOperator")) return false;
    if (!nextTokenIs(builder_, "<bitwise operator>", AND, XOR, OR)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<bitwise operator>");
    result_ = consumeToken(builder_, AND);
    if (!result_) result_ = consumeToken(builder_, XOR);
    if (!result_) result_ = consumeToken(builder_, OR);
    exit_section_(builder_, level_, marker_, BITWISE_OPERATOR, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '{' statements '}'
  public static boolean block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "block")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, statements(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, BLOCK, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // 'break' referenceExpression? ';'
  public static boolean breakStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "breakStatement")) return false;
    if (!nextTokenIs(builder_, BREAK)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, BREAK);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, breakStatement_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, BREAK_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // referenceExpression?
  private static boolean breakStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "breakStatement_1")) return false;
    referenceExpression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // arguments
  public static boolean callExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "callExpression")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = arguments(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, CALL_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // (callExpression | arrayAccessExpression | qualifiedReferenceExpression)*
  static boolean callOrArrayAccessOrQualifiedRefExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "callOrArrayAccessOrQualifiedRefExpression")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!callOrArrayAccessOrQualifiedRefExpression_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "callOrArrayAccessOrQualifiedRefExpression", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // callExpression | arrayAccessExpression | qualifiedReferenceExpression
  private static boolean callOrArrayAccessOrQualifiedRefExpression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "callOrArrayAccessOrQualifiedRefExpression_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = callExpression(builder_, level_ + 1);
    if (!result_) result_ = arrayAccessExpression(builder_, level_ + 1);
    if (!result_) result_ = qualifiedReferenceExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '.''.' << cascadeStopper >> (arrayAccess | refOrThisOrSuperOrParenExpression callOrArrayAccessOrQualifiedRefExpression) << varInitWrapper >>
  public static boolean cascadeReferenceExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cascadeReferenceExpression")) return false;
    if (!nextTokenIs(builder_, DOT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && consumeToken(builder_, DOT);
    result_ = result_ && cascadeStopper(builder_, level_ + 1);
    result_ = result_ && cascadeReferenceExpression_3(builder_, level_ + 1);
    result_ = result_ && varInitWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, CASCADE_REFERENCE_EXPRESSION, result_, false, null);
    return result_;
  }

  // arrayAccess | refOrThisOrSuperOrParenExpression callOrArrayAccessOrQualifiedRefExpression
  private static boolean cascadeReferenceExpression_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cascadeReferenceExpression_3")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = arrayAccess(builder_, level_ + 1);
    if (!result_) result_ = cascadeReferenceExpression_3_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // refOrThisOrSuperOrParenExpression callOrArrayAccessOrQualifiedRefExpression
  private static boolean cascadeReferenceExpression_3_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cascadeReferenceExpression_3_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = refOrThisOrSuperOrParenExpression(builder_, level_ + 1);
    result_ = result_ && callOrArrayAccessOrQualifiedRefExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'catch' formalParameterList block
  public static boolean catchPart(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "catchPart")) return false;
    if (!nextTokenIs(builder_, CATCH)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, CATCH);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, formalParameterList(builder_, level_ + 1));
    result_ = pinned_ && block(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, CATCH_PART, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // '{' classMembers '}'
  public static boolean classBody(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classBody")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, classMembers(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, CLASS_BODY, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // metadata* 'abstract'? 'class' componentName typeParameters? ((superclass mixins?)? interfaces? ('native' stringLiteralExpression?)? classBody |
  //                                                                                  '=' mixinApplication ';')
  public static boolean classDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition")) return false;
    if (!nextTokenIs(builder_, "<class definition>", AT, ABSTRACT, CLASS)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<class definition>");
    result_ = classDefinition_0(builder_, level_ + 1);
    result_ = result_ && classDefinition_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CLASS);
    result_ = result_ && componentName(builder_, level_ + 1);
    pinned_ = result_; // pin = 4
    result_ = result_ && report_error_(builder_, classDefinition_4(builder_, level_ + 1));
    result_ = pinned_ && classDefinition_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, CLASS_DEFINITION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean classDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "classDefinition_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'abstract'?
  private static boolean classDefinition_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_1")) return false;
    consumeToken(builder_, ABSTRACT);
    return true;
  }

  // typeParameters?
  private static boolean classDefinition_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_4")) return false;
    typeParameters(builder_, level_ + 1);
    return true;
  }

  // (superclass mixins?)? interfaces? ('native' stringLiteralExpression?)? classBody |
  //                                                                                  '=' mixinApplication ';'
  private static boolean classDefinition_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_5")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = classDefinition_5_0(builder_, level_ + 1);
    if (!result_) result_ = classDefinition_5_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (superclass mixins?)? interfaces? ('native' stringLiteralExpression?)? classBody
  private static boolean classDefinition_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_5_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = classDefinition_5_0_0(builder_, level_ + 1);
    result_ = result_ && classDefinition_5_0_1(builder_, level_ + 1);
    result_ = result_ && classDefinition_5_0_2(builder_, level_ + 1);
    result_ = result_ && classBody(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (superclass mixins?)?
  private static boolean classDefinition_5_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_5_0_0")) return false;
    classDefinition_5_0_0_0(builder_, level_ + 1);
    return true;
  }

  // superclass mixins?
  private static boolean classDefinition_5_0_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_5_0_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = superclass(builder_, level_ + 1);
    result_ = result_ && classDefinition_5_0_0_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // mixins?
  private static boolean classDefinition_5_0_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_5_0_0_0_1")) return false;
    mixins(builder_, level_ + 1);
    return true;
  }

  // interfaces?
  private static boolean classDefinition_5_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_5_0_1")) return false;
    interfaces(builder_, level_ + 1);
    return true;
  }

  // ('native' stringLiteralExpression?)?
  private static boolean classDefinition_5_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_5_0_2")) return false;
    classDefinition_5_0_2_0(builder_, level_ + 1);
    return true;
  }

  // 'native' stringLiteralExpression?
  private static boolean classDefinition_5_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_5_0_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, NATIVE);
    result_ = result_ && classDefinition_5_0_2_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // stringLiteralExpression?
  private static boolean classDefinition_5_0_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_5_0_2_0_1")) return false;
    stringLiteralExpression(builder_, level_ + 1);
    return true;
  }

  // '=' mixinApplication ';'
  private static boolean classDefinition_5_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classDefinition_5_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQ);
    result_ = result_ && mixinApplication(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // factoryConstructorDeclaration
  //                                 | namedConstructorDeclaration
  //                                 | operatorDeclaration
  //                                 | getterOrSetterDeclaration
  //                                 | methodDeclaration
  //                                 | varDeclarationListWithSemicolon
  //                                 | incompleteDeclaration
  static boolean classMemberDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classMemberDefinition")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = factoryConstructorDeclaration(builder_, level_ + 1);
    if (!result_) result_ = namedConstructorDeclaration(builder_, level_ + 1);
    if (!result_) result_ = operatorDeclaration(builder_, level_ + 1);
    if (!result_) result_ = getterOrSetterDeclaration(builder_, level_ + 1);
    if (!result_) result_ = methodDeclaration(builder_, level_ + 1);
    if (!result_) result_ = varDeclarationListWithSemicolon(builder_, level_ + 1);
    if (!result_) result_ = incompleteDeclaration(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, class_member_recover_parser_);
    return result_;
  }

  /* ********************************************************** */
  // classMemberDefinition*
  public static boolean classMembers(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classMembers")) return false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<class members>");
    int pos_ = current_position_(builder_);
    while (true) {
      if (!classMemberDefinition(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "classMembers", pos_)) break;
      pos_ = current_position_(builder_);
    }
    exit_section_(builder_, level_, marker_, CLASS_MEMBERS, true, false, simple_scope_recover_parser_);
    return true;
  }

  /* ********************************************************** */
  // metadata* 'typedef' componentName typeParameters? '=' type mixins? ';'
  public static boolean classTypeAlias(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classTypeAlias")) return false;
    if (!nextTokenIs(builder_, "<class type alias>", AT, TYPEDEF)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<class type alias>");
    result_ = classTypeAlias_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, TYPEDEF);
    result_ = result_ && componentName(builder_, level_ + 1);
    result_ = result_ && classTypeAlias_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, EQ);
    pinned_ = result_; // pin = 5
    result_ = result_ && report_error_(builder_, type(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, classTypeAlias_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, CLASS_TYPE_ALIAS, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean classTypeAlias_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classTypeAlias_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "classTypeAlias_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // typeParameters?
  private static boolean classTypeAlias_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classTypeAlias_3")) return false;
    typeParameters(builder_, level_ + 1);
    return true;
  }

  // mixins?
  private static boolean classTypeAlias_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "classTypeAlias_6")) return false;
    mixins(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> | '@' | 'abstract' | 'class' | 'const' | 'export' | 'external' | 'factory' | 'final' | 'get' | 'import' | 'library' | 'operator' | 'part' | 'set' | 'static' | 'typedef' | 'var' | 'void' | '}' )
  static boolean class_member_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "class_member_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !class_member_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // <<nonStrictID>> | '@' | 'abstract' | 'class' | 'const' | 'export' | 'external' | 'factory' | 'final' | 'get' | 'import' | 'library' | 'operator' | 'part' | 'set' | 'static' | 'typedef' | 'var' | 'void' | '}'
  private static boolean class_member_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "class_member_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = nonStrictID(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, AT);
    if (!result_) result_ = consumeToken(builder_, ABSTRACT);
    if (!result_) result_ = consumeToken(builder_, CLASS);
    if (!result_) result_ = consumeToken(builder_, CONST);
    if (!result_) result_ = consumeToken(builder_, EXPORT);
    if (!result_) result_ = consumeToken(builder_, EXTERNAL);
    if (!result_) result_ = consumeToken(builder_, FACTORY);
    if (!result_) result_ = consumeToken(builder_, FINAL);
    if (!result_) result_ = consumeToken(builder_, GET);
    if (!result_) result_ = consumeToken(builder_, IMPORT);
    if (!result_) result_ = consumeToken(builder_, LIBRARY);
    if (!result_) result_ = consumeToken(builder_, OPERATOR);
    if (!result_) result_ = consumeToken(builder_, PART);
    if (!result_) result_ = consumeToken(builder_, SET);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    if (!result_) result_ = consumeToken(builder_, TYPEDEF);
    if (!result_) result_ = consumeToken(builder_, VAR);
    if (!result_) result_ = consumeToken(builder_, VOID);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // showCombinator | hideCombinator
  static boolean combinator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "combinator")) return false;
    if (!nextTokenIs(builder_, "", HIDE, SHOW)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = showCombinator(builder_, level_ + 1);
    if (!result_) result_ = hideCombinator(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // (relationalOperator | equalityOperator) bitwiseExpressionWrapper
  public static boolean compareExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compareExpression")) return false;
    if (!nextTokenIs(builder_, "<compare expression>", NEQ, LT,
      LT_EQ, EQ_EQ, GT, GT_EQ)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, "<compare expression>");
    result_ = compareExpression_0(builder_, level_ + 1);
    result_ = result_ && bitwiseExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, COMPARE_EXPRESSION, result_, false, null);
    return result_;
  }

  // relationalOperator | equalityOperator
  private static boolean compareExpression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compareExpression_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = relationalOperator(builder_, level_ + 1);
    if (!result_) result_ = equalityOperator(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // bitwiseExpressionWrapper compareExpression*
  static boolean compareExpressionWrapper(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compareExpressionWrapper")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = bitwiseExpressionWrapper(builder_, level_ + 1);
    result_ = result_ && compareExpressionWrapper_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // compareExpression*
  private static boolean compareExpressionWrapper_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compareExpressionWrapper_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!compareExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "compareExpressionWrapper_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean componentName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "componentName")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<component name>");
    result_ = nonStrictID(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, COMPONENT_NAME, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'const' type ('.' referenceExpression)? arguments
  public static boolean constConstructorExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "constConstructorExpression")) return false;
    if (!nextTokenIs(builder_, CONST)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, CONST);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, type(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, constConstructorExpression_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && arguments(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, CONST_CONSTRUCTOR_EXPRESSION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ('.' referenceExpression)?
  private static boolean constConstructorExpression_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "constConstructorExpression_2")) return false;
    constConstructorExpression_2_0(builder_, level_ + 1);
    return true;
  }

  // '.' referenceExpression
  private static boolean constConstructorExpression_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "constConstructorExpression_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && referenceExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'continue' referenceExpression? ';'
  public static boolean continueStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "continueStatement")) return false;
    if (!nextTokenIs(builder_, CONTINUE)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, CONTINUE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, continueStatement_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, CONTINUE_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // referenceExpression?
  private static boolean continueStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "continueStatement_1")) return false;
    referenceExpression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // topLevelDefinition*
  static boolean dartUnit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "dartUnit")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!topLevelDefinition(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "dartUnit", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* finalConstVarOrTypeAndComponentName
  static boolean declaredIdentifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declaredIdentifier")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = declaredIdentifier_0(builder_, level_ + 1);
    result_ = result_ && finalConstVarOrTypeAndComponentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // metadata*
  private static boolean declaredIdentifier_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "declaredIdentifier_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "declaredIdentifier_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // label? ('case' expression ':')* 'default' ':' statements
  public static boolean defaultCase(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "defaultCase")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<default case>");
    result_ = defaultCase_0(builder_, level_ + 1);
    result_ = result_ && defaultCase_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, DEFAULT);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && statements(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, DEFAULT_CASE, result_, false, null);
    return result_;
  }

  // label?
  private static boolean defaultCase_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "defaultCase_0")) return false;
    label(builder_, level_ + 1);
    return true;
  }

  // ('case' expression ':')*
  private static boolean defaultCase_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "defaultCase_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!defaultCase_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "defaultCase_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'case' expression ':'
  private static boolean defaultCase_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "defaultCase_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CASE);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // normalFormalParameter (('=' | ':') expression)?
  public static boolean defaultFormalNamedParameter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "defaultFormalNamedParameter")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<default formal named parameter>");
    result_ = normalFormalParameter(builder_, level_ + 1);
    result_ = result_ && defaultFormalNamedParameter_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, DEFAULT_FORMAL_NAMED_PARAMETER, result_, false, default_formal_parameter_recover_parser_);
    return result_;
  }

  // (('=' | ':') expression)?
  private static boolean defaultFormalNamedParameter_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "defaultFormalNamedParameter_1")) return false;
    defaultFormalNamedParameter_1_0(builder_, level_ + 1);
    return true;
  }

  // ('=' | ':') expression
  private static boolean defaultFormalNamedParameter_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "defaultFormalNamedParameter_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = defaultFormalNamedParameter_1_0_0(builder_, level_ + 1);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '=' | ':'
  private static boolean defaultFormalNamedParameter_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "defaultFormalNamedParameter_1_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQ);
    if (!result_) result_ = consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(')' | ',' | ']' | '}')
  static boolean default_formal_parameter_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "default_formal_parameter_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !default_formal_parameter_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // ')' | ',' | ']' | '}'
  private static boolean default_formal_parameter_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "default_formal_parameter_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RPAREN);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, RBRACKET);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'do' statement 'while' '(' expressionWithRecoverUntilParen ')' ';'
  public static boolean doWhileStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "doWhileStatement")) return false;
    if (!nextTokenIs(builder_, DO)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, DO);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, statement(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, WHILE)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, LPAREN)) && result_;
    result_ = pinned_ && report_error_(builder_, expressionWithRecoverUntilParen(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RPAREN)) && result_;
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, DO_WHILE_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // '==' | '!='
  public static boolean equalityOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "equalityOperator")) return false;
    if (!nextTokenIs(builder_, "<equality operator>", NEQ, EQ_EQ)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<equality operator>");
    result_ = consumeToken(builder_, EQ_EQ);
    if (!result_) result_ = consumeToken(builder_, NEQ);
    exit_section_(builder_, level_, marker_, EQUALITY_OPERATOR, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // metadata* 'export' pathOrLibraryReference ('as' componentName )? combinator* ';'
  public static boolean exportStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exportStatement")) return false;
    if (!nextTokenIs(builder_, "<export statement>", AT, EXPORT)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<export statement>");
    result_ = exportStatement_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, EXPORT);
    result_ = result_ && pathOrLibraryReference(builder_, level_ + 1);
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, exportStatement_3(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, exportStatement_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, EXPORT_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean exportStatement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exportStatement_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "exportStatement_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('as' componentName )?
  private static boolean exportStatement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exportStatement_3")) return false;
    exportStatement_3_0(builder_, level_ + 1);
    return true;
  }

  // 'as' componentName
  private static boolean exportStatement_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exportStatement_3_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, AS);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // combinator*
  private static boolean exportStatement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exportStatement_4")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!combinator(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "exportStatement_4", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // assignExpressionWrapper
  public static boolean expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<expression>");
    result_ = assignExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, EXPRESSION, result_, false, expression_recover_parser_);
    return result_;
  }

  /* ********************************************************** */
  // expression | statement
  static boolean expressionInParentheses(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expressionInParentheses")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = expression(builder_, level_ + 1);
    if (!result_) result_ = statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, parenthesesRecovery_parser_);
    return result_;
  }

  /* ********************************************************** */
  // expression (',' expression)*
  public static boolean expressionList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expressionList")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<expression list>");
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && expressionList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, EXPRESSION_LIST, result_, false, null);
    return result_;
  }

  // (',' expression)*
  private static boolean expressionList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expressionList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!expressionList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "expressionList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' expression
  private static boolean expressionList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expressionList_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // expression
  static boolean expressionWithRecoverUntilParen(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expressionWithRecoverUntilParen")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, not_paren_recover_parser_);
    return result_;
  }

  /* ********************************************************** */
  // !('!' | '!=' | 'is'| '%' | '%=' | '&&' | '&' | '&=' | '(' | ')' | '*' | '*=' | '+' | '++'
  //                                | '+=' | ',' | '-' | '--' | '-=' | '/' | '/=' | ':' | ';' | '<' | '<<' | '<<=' | '<='
  //                                | '=' | '==' | '=>' | '>' | '>=' | '>>=' | '?' | '[' | ']'
  //                                | '^' | '^=' | 'abstract' | 'assert' | 'break' | 'case' | 'catch' | 'class' | 'const'
  //                                | 'continue' | 'default' | 'do' | 'else' | 'factory' | 'false' | 'final' | 'finally'
  //                                | 'for' | 'get' | 'if' | 'in' | 'native' | 'new' | 'null' | 'operator' | 'rethrow'
  //                                | 'return' | 'set' | 'static' | 'super' | 'switch' | 'this' | 'throw' | 'true' | 'try'
  //                                | 'typedef' | 'var' | 'while' | '{' | '|' | '|=' | '||' | '}' | '~' | '~/=' | '.'
  //                                | HEX_NUMBER | <<nonStrictID>> | NUMBER | OPEN_QUOTE | RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING
  //                                | LONG_TEMPLATE_ENTRY_END | shiftRightOperator | ('.' '.'))
  static boolean expression_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !expression_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // '!' | '!=' | 'is'| '%' | '%=' | '&&' | '&' | '&=' | '(' | ')' | '*' | '*=' | '+' | '++'
  //                                | '+=' | ',' | '-' | '--' | '-=' | '/' | '/=' | ':' | ';' | '<' | '<<' | '<<=' | '<='
  //                                | '=' | '==' | '=>' | '>' | '>=' | '>>=' | '?' | '[' | ']'
  //                                | '^' | '^=' | 'abstract' | 'assert' | 'break' | 'case' | 'catch' | 'class' | 'const'
  //                                | 'continue' | 'default' | 'do' | 'else' | 'factory' | 'false' | 'final' | 'finally'
  //                                | 'for' | 'get' | 'if' | 'in' | 'native' | 'new' | 'null' | 'operator' | 'rethrow'
  //                                | 'return' | 'set' | 'static' | 'super' | 'switch' | 'this' | 'throw' | 'true' | 'try'
  //                                | 'typedef' | 'var' | 'while' | '{' | '|' | '|=' | '||' | '}' | '~' | '~/=' | '.'
  //                                | HEX_NUMBER | <<nonStrictID>> | NUMBER | OPEN_QUOTE | RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING
  //                                | LONG_TEMPLATE_ENTRY_END | shiftRightOperator | ('.' '.')
  private static boolean expression_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, NOT);
    if (!result_) result_ = consumeToken(builder_, NEQ);
    if (!result_) result_ = consumeToken(builder_, IS);
    if (!result_) result_ = consumeToken(builder_, REM);
    if (!result_) result_ = consumeToken(builder_, REM_EQ);
    if (!result_) result_ = consumeToken(builder_, AND_AND);
    if (!result_) result_ = consumeToken(builder_, AND);
    if (!result_) result_ = consumeToken(builder_, AND_EQ);
    if (!result_) result_ = consumeToken(builder_, LPAREN);
    if (!result_) result_ = consumeToken(builder_, RPAREN);
    if (!result_) result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, MUL_EQ);
    if (!result_) result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, PLUS_PLUS);
    if (!result_) result_ = consumeToken(builder_, PLUS_EQ);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, MINUS_MINUS);
    if (!result_) result_ = consumeToken(builder_, MINUS_EQ);
    if (!result_) result_ = consumeToken(builder_, DIV);
    if (!result_) result_ = consumeToken(builder_, DIV_EQ);
    if (!result_) result_ = consumeToken(builder_, COLON);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, LT);
    if (!result_) result_ = consumeToken(builder_, LT_LT);
    if (!result_) result_ = consumeToken(builder_, LT_LT_EQ);
    if (!result_) result_ = consumeToken(builder_, LT_EQ);
    if (!result_) result_ = consumeToken(builder_, EQ);
    if (!result_) result_ = consumeToken(builder_, EQ_EQ);
    if (!result_) result_ = consumeToken(builder_, EXPRESSION_BODY_DEF);
    if (!result_) result_ = consumeToken(builder_, GT);
    if (!result_) result_ = consumeToken(builder_, GT_EQ);
    if (!result_) result_ = consumeToken(builder_, GT_GT_EQ);
    if (!result_) result_ = consumeToken(builder_, QUEST);
    if (!result_) result_ = consumeToken(builder_, LBRACKET);
    if (!result_) result_ = consumeToken(builder_, RBRACKET);
    if (!result_) result_ = consumeToken(builder_, XOR);
    if (!result_) result_ = consumeToken(builder_, XOR_EQ);
    if (!result_) result_ = consumeToken(builder_, ABSTRACT);
    if (!result_) result_ = consumeToken(builder_, ASSERT);
    if (!result_) result_ = consumeToken(builder_, BREAK);
    if (!result_) result_ = consumeToken(builder_, CASE);
    if (!result_) result_ = consumeToken(builder_, CATCH);
    if (!result_) result_ = consumeToken(builder_, CLASS);
    if (!result_) result_ = consumeToken(builder_, CONST);
    if (!result_) result_ = consumeToken(builder_, CONTINUE);
    if (!result_) result_ = consumeToken(builder_, DEFAULT);
    if (!result_) result_ = consumeToken(builder_, DO);
    if (!result_) result_ = consumeToken(builder_, ELSE);
    if (!result_) result_ = consumeToken(builder_, FACTORY);
    if (!result_) result_ = consumeToken(builder_, FALSE);
    if (!result_) result_ = consumeToken(builder_, FINAL);
    if (!result_) result_ = consumeToken(builder_, FINALLY);
    if (!result_) result_ = consumeToken(builder_, FOR);
    if (!result_) result_ = consumeToken(builder_, GET);
    if (!result_) result_ = consumeToken(builder_, IF);
    if (!result_) result_ = consumeToken(builder_, IN);
    if (!result_) result_ = consumeToken(builder_, NATIVE);
    if (!result_) result_ = consumeToken(builder_, NEW);
    if (!result_) result_ = consumeToken(builder_, NULL);
    if (!result_) result_ = consumeToken(builder_, OPERATOR);
    if (!result_) result_ = consumeToken(builder_, RETHROW);
    if (!result_) result_ = consumeToken(builder_, RETURN);
    if (!result_) result_ = consumeToken(builder_, SET);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    if (!result_) result_ = consumeToken(builder_, SUPER);
    if (!result_) result_ = consumeToken(builder_, SWITCH);
    if (!result_) result_ = consumeToken(builder_, THIS);
    if (!result_) result_ = consumeToken(builder_, THROW);
    if (!result_) result_ = consumeToken(builder_, TRUE);
    if (!result_) result_ = consumeToken(builder_, TRY);
    if (!result_) result_ = consumeToken(builder_, TYPEDEF);
    if (!result_) result_ = consumeToken(builder_, VAR);
    if (!result_) result_ = consumeToken(builder_, WHILE);
    if (!result_) result_ = consumeToken(builder_, LBRACE);
    if (!result_) result_ = consumeToken(builder_, OR);
    if (!result_) result_ = consumeToken(builder_, OR_EQ);
    if (!result_) result_ = consumeToken(builder_, OR_OR);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, BIN_NOT);
    if (!result_) result_ = consumeToken(builder_, INT_DIV_EQ);
    if (!result_) result_ = consumeToken(builder_, DOT);
    if (!result_) result_ = consumeToken(builder_, HEX_NUMBER);
    if (!result_) result_ = nonStrictID(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, NUMBER);
    if (!result_) result_ = consumeToken(builder_, OPEN_QUOTE);
    if (!result_) result_ = consumeToken(builder_, RAW_SINGLE_QUOTED_STRING);
    if (!result_) result_ = consumeToken(builder_, RAW_TRIPLE_QUOTED_STRING);
    if (!result_) result_ = consumeToken(builder_, LONG_TEMPLATE_ENTRY_END);
    if (!result_) result_ = shiftRightOperator(builder_, level_ + 1);
    if (!result_) result_ = expression_recover_0_90(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '.' '.'
  private static boolean expression_recover_0_90(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_recover_0_90")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && consumeToken(builder_, DOT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'const')* 'factory' referenceExpression ('.' componentName)? formalParameterList factoryTail?
  public static boolean factoryConstructorDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "factoryConstructorDeclaration")) return false;
    if (!nextTokenIs(builder_, "<factory constructor declaration>", AT, CONST, EXTERNAL, FACTORY)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<factory constructor declaration>");
    result_ = factoryConstructorDeclaration_0(builder_, level_ + 1);
    result_ = result_ && factoryConstructorDeclaration_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, FACTORY);
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, referenceExpression(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, factoryConstructorDeclaration_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, formalParameterList(builder_, level_ + 1)) && result_;
    result_ = pinned_ && factoryConstructorDeclaration_6(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, FACTORY_CONSTRUCTOR_DECLARATION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean factoryConstructorDeclaration_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "factoryConstructorDeclaration_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "factoryConstructorDeclaration_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('external' | 'const')*
  private static boolean factoryConstructorDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "factoryConstructorDeclaration_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!factoryConstructorDeclaration_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "factoryConstructorDeclaration_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external' | 'const'
  private static boolean factoryConstructorDeclaration_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "factoryConstructorDeclaration_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EXTERNAL);
    if (!result_) result_ = consumeToken(builder_, CONST);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ('.' componentName)?
  private static boolean factoryConstructorDeclaration_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "factoryConstructorDeclaration_4")) return false;
    factoryConstructorDeclaration_4_0(builder_, level_ + 1);
    return true;
  }

  // '.' componentName
  private static boolean factoryConstructorDeclaration_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "factoryConstructorDeclaration_4_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // factoryTail?
  private static boolean factoryConstructorDeclaration_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "factoryConstructorDeclaration_6")) return false;
    factoryTail(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // varFactoryDeclaration ';' | functionBodyOrNative | ';'
  static boolean factoryTail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "factoryTail")) return false;
    if (!nextTokenIs(builder_, "", SEMICOLON, EQ,
      EXPRESSION_BODY_DEF, NATIVE, LBRACE)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = factoryTail_0(builder_, level_ + 1);
    if (!result_) result_ = functionBodyOrNative(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // varFactoryDeclaration ';'
  private static boolean factoryTail_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "factoryTail_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = varFactoryDeclaration(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // metadata* finalConstVarOrType? 'this' '.' referenceExpression formalParameterList?
  public static boolean fieldFormalParameter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fieldFormalParameter")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<field formal parameter>");
    result_ = fieldFormalParameter_0(builder_, level_ + 1);
    result_ = result_ && fieldFormalParameter_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, THIS);
    result_ = result_ && consumeToken(builder_, DOT);
    result_ = result_ && referenceExpression(builder_, level_ + 1);
    result_ = result_ && fieldFormalParameter_5(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FIELD_FORMAL_PARAMETER, result_, false, null);
    return result_;
  }

  // metadata*
  private static boolean fieldFormalParameter_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fieldFormalParameter_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "fieldFormalParameter_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // finalConstVarOrType?
  private static boolean fieldFormalParameter_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fieldFormalParameter_1")) return false;
    finalConstVarOrType(builder_, level_ + 1);
    return true;
  }

  // formalParameterList?
  private static boolean fieldFormalParameter_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fieldFormalParameter_5")) return false;
    formalParameterList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ('this' '.')? referenceExpression '=' expression
  public static boolean fieldInitializer(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fieldInitializer")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<field initializer>");
    result_ = fieldInitializer_0(builder_, level_ + 1);
    result_ = result_ && referenceExpression(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, consumeToken(builder_, EQ));
    result_ = pinned_ && expression(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, FIELD_INITIALIZER, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ('this' '.')?
  private static boolean fieldInitializer_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fieldInitializer_0")) return false;
    fieldInitializer_0_0(builder_, level_ + 1);
    return true;
  }

  // 'this' '.'
  private static boolean fieldInitializer_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fieldInitializer_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, THIS);
    result_ = result_ && consumeToken(builder_, DOT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'final' type? | 'const' type? | 'var' | type
  static boolean finalConstVarOrType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrType")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = finalConstVarOrType_0(builder_, level_ + 1);
    if (!result_) result_ = finalConstVarOrType_1(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, VAR);
    if (!result_) result_ = type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'final' type?
  private static boolean finalConstVarOrType_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrType_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, FINAL);
    result_ = result_ && finalConstVarOrType_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type?
  private static boolean finalConstVarOrType_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrType_0_1")) return false;
    type(builder_, level_ + 1);
    return true;
  }

  // 'const' type?
  private static boolean finalConstVarOrType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrType_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CONST);
    result_ = result_ && finalConstVarOrType_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type?
  private static boolean finalConstVarOrType_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrType_1_1")) return false;
    type(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'final' type componentName |
  //                                                 'final'      componentName |
  //                                                 'const' type componentName |
  //                                                 'const'      componentName |
  //                                                 'var'        componentName |
  //                                                         type componentName
  static boolean finalConstVarOrTypeAndComponentName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrTypeAndComponentName")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = finalConstVarOrTypeAndComponentName_0(builder_, level_ + 1);
    if (!result_) result_ = finalConstVarOrTypeAndComponentName_1(builder_, level_ + 1);
    if (!result_) result_ = finalConstVarOrTypeAndComponentName_2(builder_, level_ + 1);
    if (!result_) result_ = finalConstVarOrTypeAndComponentName_3(builder_, level_ + 1);
    if (!result_) result_ = finalConstVarOrTypeAndComponentName_4(builder_, level_ + 1);
    if (!result_) result_ = finalConstVarOrTypeAndComponentName_5(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'final' type componentName
  private static boolean finalConstVarOrTypeAndComponentName_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrTypeAndComponentName_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, FINAL);
    result_ = result_ && type(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'final'      componentName
  private static boolean finalConstVarOrTypeAndComponentName_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrTypeAndComponentName_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, FINAL);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'const' type componentName
  private static boolean finalConstVarOrTypeAndComponentName_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrTypeAndComponentName_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CONST);
    result_ = result_ && type(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'const'      componentName
  private static boolean finalConstVarOrTypeAndComponentName_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrTypeAndComponentName_3")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CONST);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'var'        componentName
  private static boolean finalConstVarOrTypeAndComponentName_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrTypeAndComponentName_4")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, VAR);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type componentName
  private static boolean finalConstVarOrTypeAndComponentName_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalConstVarOrTypeAndComponentName_5")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = type(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'final' | 'const'
  static boolean finalOrConst(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finalOrConst")) return false;
    if (!nextTokenIs(builder_, "", CONST, FINAL)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, FINAL);
    if (!result_) result_ = consumeToken(builder_, CONST);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'finally' block
  public static boolean finallyPart(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "finallyPart")) return false;
    if (!nextTokenIs(builder_, FINALLY)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, FINALLY);
    pinned_ = result_; // pin = 1
    result_ = result_ && block(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FINALLY_PART, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // (varAccessDeclaration | componentName) 'in' expression
  public static boolean forInPart(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forInPart")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<for in part>");
    result_ = forInPart_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IN);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FOR_IN_PART, result_, false, null);
    return result_;
  }

  // varAccessDeclaration | componentName
  private static boolean forInPart_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forInPart_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = varAccessDeclaration(builder_, level_ + 1);
    if (!result_) result_ = componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // varDeclarationList ';' expression? ';' expressionList?
  //               | forInPart
  //               | expressionList? ';' expression? ';' expressionList?
  public static boolean forLoopParts(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forLoopParts")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<for loop parts>");
    result_ = forLoopParts_0(builder_, level_ + 1);
    if (!result_) result_ = forInPart(builder_, level_ + 1);
    if (!result_) result_ = forLoopParts_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FOR_LOOP_PARTS, result_, false, for_loops_parts_recover_parser_);
    return result_;
  }

  // varDeclarationList ';' expression? ';' expressionList?
  private static boolean forLoopParts_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forLoopParts_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = varDeclarationList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    result_ = result_ && forLoopParts_0_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    result_ = result_ && forLoopParts_0_4(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expression?
  private static boolean forLoopParts_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forLoopParts_0_2")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  // expressionList?
  private static boolean forLoopParts_0_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forLoopParts_0_4")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  // expressionList? ';' expression? ';' expressionList?
  private static boolean forLoopParts_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forLoopParts_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = forLoopParts_2_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    result_ = result_ && forLoopParts_2_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    result_ = result_ && forLoopParts_2_4(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expressionList?
  private static boolean forLoopParts_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forLoopParts_2_0")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  // expression?
  private static boolean forLoopParts_2_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forLoopParts_2_2")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  // expressionList?
  private static boolean forLoopParts_2_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forLoopParts_2_4")) return false;
    expressionList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' forLoopParts ')'
  public static boolean forLoopPartsInBraces(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forLoopPartsInBraces")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && forLoopParts(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, FOR_LOOP_PARTS_IN_BRACES, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'for' forLoopPartsInBraces statement
  public static boolean forStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "forStatement")) return false;
    if (!nextTokenIs(builder_, FOR)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, FOR);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, forLoopPartsInBraces(builder_, level_ + 1));
    result_ = pinned_ && statement(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, FOR_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // !')'
  static boolean for_loops_parts_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "for_loops_parts_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !consumeToken(builder_, RPAREN);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '(' normalFormalParameter (',' normalFormalParameter)* (',' namedFormalParameters)? ')'
  //                       | '(' namedFormalParameters? ')'
  public static boolean formalParameterList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "formalParameterList")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = formalParameterList_0(builder_, level_ + 1);
    if (!result_) result_ = formalParameterList_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, FORMAL_PARAMETER_LIST, result_);
    return result_;
  }

  // '(' normalFormalParameter (',' normalFormalParameter)* (',' namedFormalParameters)? ')'
  private static boolean formalParameterList_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "formalParameterList_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && normalFormalParameter(builder_, level_ + 1);
    result_ = result_ && formalParameterList_0_2(builder_, level_ + 1);
    result_ = result_ && formalParameterList_0_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (',' normalFormalParameter)*
  private static boolean formalParameterList_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "formalParameterList_0_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!formalParameterList_0_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "formalParameterList_0_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' normalFormalParameter
  private static boolean formalParameterList_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "formalParameterList_0_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && normalFormalParameter(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (',' namedFormalParameters)?
  private static boolean formalParameterList_0_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "formalParameterList_0_3")) return false;
    formalParameterList_0_3_0(builder_, level_ + 1);
    return true;
  }

  // ',' namedFormalParameters
  private static boolean formalParameterList_0_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "formalParameterList_0_3_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && namedFormalParameters(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' namedFormalParameters? ')'
  private static boolean formalParameterList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "formalParameterList_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && formalParameterList_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // namedFormalParameters?
  private static boolean formalParameterList_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "formalParameterList_1_1")) return false;
    namedFormalParameters(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // arrowBodyWithSemi | block
  public static boolean functionBody(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionBody")) return false;
    if (!nextTokenIs(builder_, "<function body>", EXPRESSION_BODY_DEF, LBRACE)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<function body>");
    result_ = arrowBodyWithSemi(builder_, level_ + 1);
    if (!result_) result_ = block(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FUNCTION_BODY, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'native' functionBody
  //                                | functionNative
  //                                | functionBody
  static boolean functionBodyOrNative(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionBodyOrNative")) return false;
    if (!nextTokenIs(builder_, "", EXPRESSION_BODY_DEF, NATIVE, LBRACE)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = functionBodyOrNative_0(builder_, level_ + 1);
    if (!result_) result_ = functionNative(builder_, level_ + 1);
    if (!result_) result_ = functionBody(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'native' functionBody
  private static boolean functionBodyOrNative_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionBodyOrNative_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, NATIVE);
    result_ = result_ && functionBody(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // returnType componentName formalParameterList | componentName formalParameterList
  static boolean functionDeclarationPrivate(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationPrivate")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = functionDeclarationPrivate_0(builder_, level_ + 1);
    if (!result_) result_ = functionDeclarationPrivate_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // returnType componentName formalParameterList
  private static boolean functionDeclarationPrivate_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationPrivate_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = returnType(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    result_ = result_ && formalParameterList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // componentName formalParameterList
  private static boolean functionDeclarationPrivate_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationPrivate_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = componentName(builder_, level_ + 1);
    result_ = result_ && formalParameterList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata*  (returnType componentName formalParameterList | componentName formalParameterList) functionBody
  public static boolean functionDeclarationWithBody(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBody")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<function declaration with body>");
    result_ = functionDeclarationWithBody_0(builder_, level_ + 1);
    result_ = result_ && functionDeclarationWithBody_1(builder_, level_ + 1);
    result_ = result_ && functionBody(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FUNCTION_DECLARATION_WITH_BODY, result_, false, null);
    return result_;
  }

  // metadata*
  private static boolean functionDeclarationWithBody_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBody_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "functionDeclarationWithBody_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // returnType componentName formalParameterList | componentName formalParameterList
  private static boolean functionDeclarationWithBody_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBody_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = functionDeclarationWithBody_1_0(builder_, level_ + 1);
    if (!result_) result_ = functionDeclarationWithBody_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // returnType componentName formalParameterList
  private static boolean functionDeclarationWithBody_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBody_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = returnType(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    result_ = result_ && formalParameterList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // componentName formalParameterList
  private static boolean functionDeclarationWithBody_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBody_1_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = componentName(builder_, level_ + 1);
    result_ = result_ && formalParameterList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata* 'external'? (returnType componentName formalParameterList | componentName formalParameterList) ( ';' | functionBodyOrNative)
  public static boolean functionDeclarationWithBodyOrNative(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBodyOrNative")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<function declaration with body or native>");
    result_ = functionDeclarationWithBodyOrNative_0(builder_, level_ + 1);
    result_ = result_ && functionDeclarationWithBodyOrNative_1(builder_, level_ + 1);
    result_ = result_ && functionDeclarationWithBodyOrNative_2(builder_, level_ + 1);
    result_ = result_ && functionDeclarationWithBodyOrNative_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FUNCTION_DECLARATION_WITH_BODY_OR_NATIVE, result_, false, null);
    return result_;
  }

  // metadata*
  private static boolean functionDeclarationWithBodyOrNative_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBodyOrNative_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "functionDeclarationWithBodyOrNative_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external'?
  private static boolean functionDeclarationWithBodyOrNative_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBodyOrNative_1")) return false;
    consumeToken(builder_, EXTERNAL);
    return true;
  }

  // returnType componentName formalParameterList | componentName formalParameterList
  private static boolean functionDeclarationWithBodyOrNative_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBodyOrNative_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = functionDeclarationWithBodyOrNative_2_0(builder_, level_ + 1);
    if (!result_) result_ = functionDeclarationWithBodyOrNative_2_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // returnType componentName formalParameterList
  private static boolean functionDeclarationWithBodyOrNative_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBodyOrNative_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = returnType(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    result_ = result_ && formalParameterList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // componentName formalParameterList
  private static boolean functionDeclarationWithBodyOrNative_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBodyOrNative_2_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = componentName(builder_, level_ + 1);
    result_ = result_ && formalParameterList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';' | functionBodyOrNative
  private static boolean functionDeclarationWithBodyOrNative_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionDeclarationWithBodyOrNative_3")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = functionBodyOrNative(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // formalParameterList functionExpressionBody
  public static boolean functionExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionExpression")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = formalParameterList(builder_, level_ + 1);
    result_ = result_ && functionExpressionBody(builder_, level_ + 1);
    exit_section_(builder_, marker_, FUNCTION_EXPRESSION, result_);
    return result_;
  }

  /* ********************************************************** */
  // arrowBody | block
  public static boolean functionExpressionBody(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionExpressionBody")) return false;
    if (!nextTokenIs(builder_, "<function expression body>", EXPRESSION_BODY_DEF, LBRACE)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<function expression body>");
    result_ = arrowBody(builder_, level_ + 1);
    if (!result_) result_ = block(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FUNCTION_EXPRESSION_BODY, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'native' (stringLiteralExpression ';' | ';' | stringLiteralExpression functionBody)
  static boolean functionNative(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionNative")) return false;
    if (!nextTokenIs(builder_, NATIVE)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, NATIVE);
    result_ = result_ && functionNative_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // stringLiteralExpression ';' | ';' | stringLiteralExpression functionBody
  private static boolean functionNative_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionNative_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = functionNative_1_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = functionNative_1_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // stringLiteralExpression ';'
  private static boolean functionNative_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionNative_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = stringLiteralExpression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // stringLiteralExpression functionBody
  private static boolean functionNative_1_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionNative_1_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = stringLiteralExpression(builder_, level_ + 1);
    result_ = result_ && functionBody(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // returnType componentName | componentName
  static boolean functionPrefix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionPrefix")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = functionPrefix_0(builder_, level_ + 1);
    if (!result_) result_ = componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // returnType componentName
  private static boolean functionPrefix_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionPrefix_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = returnType(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata* functionDeclarationPrivate
  public static boolean functionSignature(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionSignature")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<function signature>");
    result_ = functionSignature_0(builder_, level_ + 1);
    result_ = result_ && functionDeclarationPrivate(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FUNCTION_SIGNATURE, result_, false, null);
    return result_;
  }

  // metadata*
  private static boolean functionSignature_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionSignature_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "functionSignature_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* 'typedef' functionPrefix typeParameters? formalParameterList ';'
  public static boolean functionTypeAlias(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionTypeAlias")) return false;
    if (!nextTokenIs(builder_, "<function type alias>", AT, TYPEDEF)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<function type alias>");
    result_ = functionTypeAlias_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, TYPEDEF);
    result_ = result_ && functionPrefix(builder_, level_ + 1);
    result_ = result_ && functionTypeAlias_3(builder_, level_ + 1);
    result_ = result_ && formalParameterList(builder_, level_ + 1);
    pinned_ = result_; // pin = 5
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, FUNCTION_TYPE_ALIAS, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean functionTypeAlias_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionTypeAlias_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "functionTypeAlias_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // typeParameters?
  private static boolean functionTypeAlias_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionTypeAlias_3")) return false;
    typeParameters(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // getterDeclarationWithReturnType | getterDeclarationWithoutReturnType
  public static boolean getterDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclaration")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<getter declaration>");
    result_ = getterDeclarationWithReturnType(builder_, level_ + 1);
    if (!result_) result_ = getterDeclarationWithoutReturnType(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, GETTER_DECLARATION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')* returnType 'get' componentName formalParameterList? (';' | functionBodyOrNative)
  static boolean getterDeclarationWithReturnType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithReturnType")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = getterDeclarationWithReturnType_0(builder_, level_ + 1);
    result_ = result_ && getterDeclarationWithReturnType_1(builder_, level_ + 1);
    result_ = result_ && returnType(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, GET);
    result_ = result_ && componentName(builder_, level_ + 1);
    pinned_ = result_; // pin = 5
    result_ = result_ && report_error_(builder_, getterDeclarationWithReturnType_5(builder_, level_ + 1));
    result_ = pinned_ && getterDeclarationWithReturnType_6(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean getterDeclarationWithReturnType_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithReturnType_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "getterDeclarationWithReturnType_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean getterDeclarationWithReturnType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithReturnType_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!getterDeclarationWithReturnType_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "getterDeclarationWithReturnType_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean getterDeclarationWithReturnType_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithReturnType_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EXTERNAL);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // formalParameterList?
  private static boolean getterDeclarationWithReturnType_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithReturnType_5")) return false;
    formalParameterList(builder_, level_ + 1);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean getterDeclarationWithReturnType_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithReturnType_6")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = functionBodyOrNative(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')*            'get' componentName formalParameterList? (';' | functionBodyOrNative)
  static boolean getterDeclarationWithoutReturnType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithoutReturnType")) return false;
    if (!nextTokenIs(builder_, "", AT, EXTERNAL, GET, STATIC)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = getterDeclarationWithoutReturnType_0(builder_, level_ + 1);
    result_ = result_ && getterDeclarationWithoutReturnType_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, GET);
    result_ = result_ && componentName(builder_, level_ + 1);
    pinned_ = result_; // pin = 4
    result_ = result_ && report_error_(builder_, getterDeclarationWithoutReturnType_4(builder_, level_ + 1));
    result_ = pinned_ && getterDeclarationWithoutReturnType_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean getterDeclarationWithoutReturnType_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithoutReturnType_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "getterDeclarationWithoutReturnType_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean getterDeclarationWithoutReturnType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithoutReturnType_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!getterDeclarationWithoutReturnType_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "getterDeclarationWithoutReturnType_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean getterDeclarationWithoutReturnType_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithoutReturnType_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EXTERNAL);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // formalParameterList?
  private static boolean getterDeclarationWithoutReturnType_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithoutReturnType_4")) return false;
    formalParameterList(builder_, level_ + 1);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean getterDeclarationWithoutReturnType_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterDeclarationWithoutReturnType_5")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = functionBodyOrNative(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // getterDeclaration | setterDeclaration
  static boolean getterOrSetterDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "getterOrSetterDeclaration")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = getterDeclaration(builder_, level_ + 1);
    if (!result_) result_ = setterDeclaration(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'hide' libraryReferenceList
  public static boolean hideCombinator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "hideCombinator")) return false;
    if (!nextTokenIs(builder_, HIDE)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, HIDE);
    result_ = result_ && libraryReferenceList(builder_, level_ + 1);
    exit_section_(builder_, marker_, HIDE_COMBINATOR, result_);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean id(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "id")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, ID, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'if' '(' expressionWithRecoverUntilParen ')' statement ('else' statement)?
  public static boolean ifStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifStatement")) return false;
    if (!nextTokenIs(builder_, IF)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, IF);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, LPAREN));
    result_ = pinned_ && report_error_(builder_, expressionWithRecoverUntilParen(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RPAREN)) && result_;
    result_ = pinned_ && report_error_(builder_, statement(builder_, level_ + 1)) && result_;
    result_ = pinned_ && ifStatement_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, IF_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ('else' statement)?
  private static boolean ifStatement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifStatement_5")) return false;
    ifStatement_5_0(builder_, level_ + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifStatement_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ifStatement_5_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ELSE);
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata* 'import' pathOrLibraryReference ('as' componentName )? combinator* ';'
  public static boolean importStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "importStatement")) return false;
    if (!nextTokenIs(builder_, "<import statement>", AT, IMPORT)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<import statement>");
    result_ = importStatement_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IMPORT);
    result_ = result_ && pathOrLibraryReference(builder_, level_ + 1);
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, importStatement_3(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, importStatement_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, IMPORT_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean importStatement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "importStatement_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "importStatement_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('as' componentName )?
  private static boolean importStatement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "importStatement_3")) return false;
    importStatement_3_0(builder_, level_ + 1);
    return true;
  }

  // 'as' componentName
  private static boolean importStatement_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "importStatement_3_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, AS);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // combinator*
  private static boolean importStatement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "importStatement_4")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!combinator(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "importStatement_4", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static' | 'final' | 'const')* type | metadata+
  public static boolean incompleteDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "incompleteDeclaration")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<incomplete declaration>");
    result_ = incompleteDeclaration_0(builder_, level_ + 1);
    if (!result_) result_ = incompleteDeclaration_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, INCOMPLETE_DECLARATION, result_, false, null);
    return result_;
  }

  // metadata* ('external' | 'static' | 'final' | 'const')* type
  private static boolean incompleteDeclaration_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "incompleteDeclaration_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = incompleteDeclaration_0_0(builder_, level_ + 1);
    result_ = result_ && incompleteDeclaration_0_1(builder_, level_ + 1);
    result_ = result_ && type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // metadata*
  private static boolean incompleteDeclaration_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "incompleteDeclaration_0_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "incompleteDeclaration_0_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('external' | 'static' | 'final' | 'const')*
  private static boolean incompleteDeclaration_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "incompleteDeclaration_0_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!incompleteDeclaration_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "incompleteDeclaration_0_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external' | 'static' | 'final' | 'const'
  private static boolean incompleteDeclaration_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "incompleteDeclaration_0_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EXTERNAL);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    if (!result_) result_ = consumeToken(builder_, FINAL);
    if (!result_) result_ = consumeToken(builder_, CONST);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // metadata+
  private static boolean incompleteDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "incompleteDeclaration_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = metadata(builder_, level_ + 1);
    int pos_ = current_position_(builder_);
    while (result_) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "incompleteDeclaration_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ':' superCallOrFieldInitializer (',' superCallOrFieldInitializer)*
  public static boolean initializers(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "initializers")) return false;
    if (!nextTokenIs(builder_, COLON)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COLON);
    result_ = result_ && superCallOrFieldInitializer(builder_, level_ + 1);
    result_ = result_ && initializers_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, INITIALIZERS, result_);
    return result_;
  }

  // (',' superCallOrFieldInitializer)*
  private static boolean initializers_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "initializers_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!initializers_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "initializers_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' superCallOrFieldInitializer
  private static boolean initializers_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "initializers_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && superCallOrFieldInitializer(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'implements' typeList
  public static boolean interfaces(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "interfaces")) return false;
    if (!nextTokenIs(builder_, IMPLEMENTS)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, IMPLEMENTS);
    pinned_ = result_; // pin = 1
    result_ = result_ && typeList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, INTERFACES, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // 'is' '!'? type
  public static boolean isExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "isExpression")) return false;
    if (!nextTokenIs(builder_, IS)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = consumeToken(builder_, IS);
    result_ = result_ && isExpression_1(builder_, level_ + 1);
    result_ = result_ && type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, IS_EXPRESSION, result_, false, null);
    return result_;
  }

  // '!'?
  private static boolean isExpression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "isExpression_1")) return false;
    consumeToken(builder_, NOT);
    return true;
  }

  /* ********************************************************** */
  // componentName ':'
  public static boolean label(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<label>");
    result_ = componentName(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, level_, marker_, LABEL, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean libraryComponentReferenceExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "libraryComponentReferenceExpression")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<library component reference expression>");
    result_ = nonStrictID(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, LIBRARY_COMPONENT_REFERENCE_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // <<nonStrictID>> ('.' <<nonStrictID>>)*
  public static boolean libraryId(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "libraryId")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<library id>");
    result_ = nonStrictID(builder_, level_ + 1);
    result_ = result_ && libraryId_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, LIBRARY_ID, result_, false, semicolon_recover_parser_);
    return result_;
  }

  // ('.' <<nonStrictID>>)*
  private static boolean libraryId_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "libraryId_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!libraryId_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "libraryId_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // '.' <<nonStrictID>>
  private static boolean libraryId_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "libraryId_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && nonStrictID(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // libraryComponentReferenceExpression (',' libraryComponentReferenceExpression)*
  public static boolean libraryReferenceList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "libraryReferenceList")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<library reference list>");
    result_ = libraryComponentReferenceExpression(builder_, level_ + 1);
    result_ = result_ && libraryReferenceList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, LIBRARY_REFERENCE_LIST, result_, false, null);
    return result_;
  }

  // (',' libraryComponentReferenceExpression)*
  private static boolean libraryReferenceList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "libraryReferenceList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!libraryReferenceList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "libraryReferenceList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' libraryComponentReferenceExpression
  private static boolean libraryReferenceList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "libraryReferenceList_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && libraryComponentReferenceExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata* 'library' qualifiedComponentName ';'
  public static boolean libraryStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "libraryStatement")) return false;
    if (!nextTokenIs(builder_, "<library statement>", AT, LIBRARY)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<library statement>");
    result_ = libraryStatement_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LIBRARY);
    result_ = result_ && qualifiedComponentName(builder_, level_ + 1);
    pinned_ = result_; // pin = 3
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, LIBRARY_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean libraryStatement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "libraryStatement_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "libraryStatement_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // 'const'? typeArguments? '[' (expressionList ','?)? ']'
  public static boolean listLiteralExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteralExpression")) return false;
    if (!nextTokenIs(builder_, "<list literal expression>", LT, LBRACKET, CONST)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<list literal expression>");
    result_ = listLiteralExpression_0(builder_, level_ + 1);
    result_ = result_ && listLiteralExpression_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACKET);
    result_ = result_ && listLiteralExpression_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACKET);
    exit_section_(builder_, level_, marker_, LIST_LITERAL_EXPRESSION, result_, false, null);
    return result_;
  }

  // 'const'?
  private static boolean listLiteralExpression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteralExpression_0")) return false;
    consumeToken(builder_, CONST);
    return true;
  }

  // typeArguments?
  private static boolean listLiteralExpression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteralExpression_1")) return false;
    typeArguments(builder_, level_ + 1);
    return true;
  }

  // (expressionList ','?)?
  private static boolean listLiteralExpression_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteralExpression_3")) return false;
    listLiteralExpression_3_0(builder_, level_ + 1);
    return true;
  }

  // expressionList ','?
  private static boolean listLiteralExpression_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteralExpression_3_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = expressionList(builder_, level_ + 1);
    result_ = result_ && listLiteralExpression_3_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ','?
  private static boolean listLiteralExpression_3_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteralExpression_3_0_1")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // NULL | TRUE | FALSE | NUMBER | HEX_NUMBER | stringLiteralExpression | symbolLiteralExpression | mapLiteralExpression | listLiteralExpression
  public static boolean literalExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "literalExpression")) return false;
    if (!nextTokenIs(builder_, "<literal expression>", LT, LBRACKET,
      CONST, LBRACE, FALSE, HASH, HEX_NUMBER, NULL,
      NUMBER, OPEN_QUOTE, RAW_SINGLE_QUOTED_STRING, RAW_TRIPLE_QUOTED_STRING, TRUE)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<literal expression>");
    result_ = consumeToken(builder_, NULL);
    if (!result_) result_ = consumeToken(builder_, TRUE);
    if (!result_) result_ = consumeToken(builder_, FALSE);
    if (!result_) result_ = consumeToken(builder_, NUMBER);
    if (!result_) result_ = consumeToken(builder_, HEX_NUMBER);
    if (!result_) result_ = stringLiteralExpression(builder_, level_ + 1);
    if (!result_) result_ = symbolLiteralExpression(builder_, level_ + 1);
    if (!result_) result_ = mapLiteralExpression(builder_, level_ + 1);
    if (!result_) result_ = listLiteralExpression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, LITERAL_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '&&' compareExpressionWrapper
  public static boolean logicAndExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicAndExpression")) return false;
    if (!nextTokenIs(builder_, AND_AND)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = consumeToken(builder_, AND_AND);
    result_ = result_ && compareExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, LOGIC_AND_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // compareExpressionWrapper logicAndExpression*
  static boolean logicAndExpressionWrapper(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicAndExpressionWrapper")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = compareExpressionWrapper(builder_, level_ + 1);
    result_ = result_ && logicAndExpressionWrapper_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // logicAndExpression*
  private static boolean logicAndExpressionWrapper_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicAndExpressionWrapper_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!logicAndExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "logicAndExpressionWrapper_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // '||' logicAndExpressionWrapper
  public static boolean logicOrExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicOrExpression")) return false;
    if (!nextTokenIs(builder_, OR_OR)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = consumeToken(builder_, OR_OR);
    result_ = result_ && logicAndExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, LOGIC_OR_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // logicAndExpressionWrapper logicOrExpression*
  static boolean logicOrExpressionWrapper(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicOrExpressionWrapper")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = logicAndExpressionWrapper(builder_, level_ + 1);
    result_ = result_ && logicOrExpressionWrapper_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // logicOrExpression*
  private static boolean logicOrExpressionWrapper_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "logicOrExpressionWrapper_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!logicOrExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "logicOrExpressionWrapper_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // LONG_TEMPLATE_ENTRY_START expression LONG_TEMPLATE_ENTRY_END
  public static boolean longTemplateEntry(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "longTemplateEntry")) return false;
    if (!nextTokenIs(builder_, LONG_TEMPLATE_ENTRY_START)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LONG_TEMPLATE_ENTRY_START);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, expression(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, LONG_TEMPLATE_ENTRY_END) && result_;
    exit_section_(builder_, level_, marker_, LONG_TEMPLATE_ENTRY, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // expression ':' expression
  public static boolean mapLiteralEntry(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteralEntry")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<map literal entry>");
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, MAP_LITERAL_ENTRY, result_, false, map_literal_entry_recover_parser_);
    return result_;
  }

  /* ********************************************************** */
  // 'const'? typeArguments? '{' (mapLiteralEntry (',' mapLiteralEntry)* ','? )? '}'
  public static boolean mapLiteralExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteralExpression")) return false;
    if (!nextTokenIs(builder_, "<map literal expression>", LT, CONST, LBRACE)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<map literal expression>");
    result_ = mapLiteralExpression_0(builder_, level_ + 1);
    result_ = result_ && mapLiteralExpression_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACE);
    result_ = result_ && mapLiteralExpression_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, level_, marker_, MAP_LITERAL_EXPRESSION, result_, false, null);
    return result_;
  }

  // 'const'?
  private static boolean mapLiteralExpression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteralExpression_0")) return false;
    consumeToken(builder_, CONST);
    return true;
  }

  // typeArguments?
  private static boolean mapLiteralExpression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteralExpression_1")) return false;
    typeArguments(builder_, level_ + 1);
    return true;
  }

  // (mapLiteralEntry (',' mapLiteralEntry)* ','? )?
  private static boolean mapLiteralExpression_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteralExpression_3")) return false;
    mapLiteralExpression_3_0(builder_, level_ + 1);
    return true;
  }

  // mapLiteralEntry (',' mapLiteralEntry)* ','?
  private static boolean mapLiteralExpression_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteralExpression_3_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = mapLiteralEntry(builder_, level_ + 1);
    result_ = result_ && mapLiteralExpression_3_0_1(builder_, level_ + 1);
    result_ = result_ && mapLiteralExpression_3_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (',' mapLiteralEntry)*
  private static boolean mapLiteralExpression_3_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteralExpression_3_0_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!mapLiteralExpression_3_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "mapLiteralExpression_3_0_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' mapLiteralEntry
  private static boolean mapLiteralExpression_3_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteralExpression_3_0_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && mapLiteralEntry(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ','?
  private static boolean mapLiteralExpression_3_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteralExpression_3_0_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // !('!=' | 'is' | '%' | '&&' | '&' | '(' | ')' | '*' | '+' | ',' | '-' | '.' | '/' | ':' | ';' | '<'
  //                                       | '<<' | '<=' | '==' | '=>' | '>' | '>=' | '?' | '[' | ']' | '^'
  //                                       | 'native' | '{' | '|' | '||' | '}' | '~/' | shiftRightOperator | ('.' '.'))
  static boolean map_literal_entry_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "map_literal_entry_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !map_literal_entry_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // '!=' | 'is' | '%' | '&&' | '&' | '(' | ')' | '*' | '+' | ',' | '-' | '.' | '/' | ':' | ';' | '<'
  //                                       | '<<' | '<=' | '==' | '=>' | '>' | '>=' | '?' | '[' | ']' | '^'
  //                                       | 'native' | '{' | '|' | '||' | '}' | '~/' | shiftRightOperator | ('.' '.')
  private static boolean map_literal_entry_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "map_literal_entry_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, NEQ);
    if (!result_) result_ = consumeToken(builder_, IS);
    if (!result_) result_ = consumeToken(builder_, REM);
    if (!result_) result_ = consumeToken(builder_, AND_AND);
    if (!result_) result_ = consumeToken(builder_, AND);
    if (!result_) result_ = consumeToken(builder_, LPAREN);
    if (!result_) result_ = consumeToken(builder_, RPAREN);
    if (!result_) result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, DOT);
    if (!result_) result_ = consumeToken(builder_, DIV);
    if (!result_) result_ = consumeToken(builder_, COLON);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, LT);
    if (!result_) result_ = consumeToken(builder_, LT_LT);
    if (!result_) result_ = consumeToken(builder_, LT_EQ);
    if (!result_) result_ = consumeToken(builder_, EQ_EQ);
    if (!result_) result_ = consumeToken(builder_, EXPRESSION_BODY_DEF);
    if (!result_) result_ = consumeToken(builder_, GT);
    if (!result_) result_ = consumeToken(builder_, GT_EQ);
    if (!result_) result_ = consumeToken(builder_, QUEST);
    if (!result_) result_ = consumeToken(builder_, LBRACKET);
    if (!result_) result_ = consumeToken(builder_, RBRACKET);
    if (!result_) result_ = consumeToken(builder_, XOR);
    if (!result_) result_ = consumeToken(builder_, NATIVE);
    if (!result_) result_ = consumeToken(builder_, LBRACE);
    if (!result_) result_ = consumeToken(builder_, OR);
    if (!result_) result_ = consumeToken(builder_, OR_OR);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, INT_DIV);
    if (!result_) result_ = shiftRightOperator(builder_, level_ + 1);
    if (!result_) result_ = map_literal_entry_recover_0_33(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '.' '.'
  private static boolean map_literal_entry_recover_0_33(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "map_literal_entry_recover_0_33")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && consumeToken(builder_, DOT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '@' simpleQualifiedReferenceExpression arguments?
  public static boolean metadata(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "metadata")) return false;
    if (!nextTokenIs(builder_, AT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, AT);
    result_ = result_ && simpleQualifiedReferenceExpression(builder_, level_ + 1);
    result_ = result_ && metadata_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, METADATA, result_);
    return result_;
  }

  // arguments?
  private static boolean metadata_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "metadata_2")) return false;
    arguments(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static' | 'const')* functionDeclarationPrivate initializers? (';' | functionBodyOrNative | redirection)?
  public static boolean methodDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "methodDeclaration")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<method declaration>");
    result_ = methodDeclaration_0(builder_, level_ + 1);
    result_ = result_ && methodDeclaration_1(builder_, level_ + 1);
    result_ = result_ && functionDeclarationPrivate(builder_, level_ + 1);
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, methodDeclaration_3(builder_, level_ + 1));
    result_ = pinned_ && methodDeclaration_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, METHOD_DECLARATION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean methodDeclaration_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "methodDeclaration_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "methodDeclaration_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('external' | 'static' | 'const')*
  private static boolean methodDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "methodDeclaration_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!methodDeclaration_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "methodDeclaration_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external' | 'static' | 'const'
  private static boolean methodDeclaration_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "methodDeclaration_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EXTERNAL);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    if (!result_) result_ = consumeToken(builder_, CONST);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // initializers?
  private static boolean methodDeclaration_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "methodDeclaration_3")) return false;
    initializers(builder_, level_ + 1);
    return true;
  }

  // (';' | functionBodyOrNative | redirection)?
  private static boolean methodDeclaration_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "methodDeclaration_4")) return false;
    methodDeclaration_4_0(builder_, level_ + 1);
    return true;
  }

  // ';' | functionBodyOrNative | redirection
  private static boolean methodDeclaration_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "methodDeclaration_4_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = functionBodyOrNative(builder_, level_ + 1);
    if (!result_) result_ = redirection(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // type mixins? interfaces?
  public static boolean mixinApplication(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mixinApplication")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<mixin application>");
    result_ = type(builder_, level_ + 1);
    result_ = result_ && mixinApplication_1(builder_, level_ + 1);
    result_ = result_ && mixinApplication_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, MIXIN_APPLICATION, result_, false, null);
    return result_;
  }

  // mixins?
  private static boolean mixinApplication_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mixinApplication_1")) return false;
    mixins(builder_, level_ + 1);
    return true;
  }

  // interfaces?
  private static boolean mixinApplication_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mixinApplication_2")) return false;
    interfaces(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'with' typeList
  public static boolean mixins(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mixins")) return false;
    if (!nextTokenIs(builder_, WITH)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, WITH);
    pinned_ = result_; // pin = 1
    result_ = result_ && typeList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, MIXINS, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // multiplicativeOperator prefixExpression
  public static boolean multiplicativeExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicativeExpression")) return false;
    if (!nextTokenIs(builder_, "<multiplicative expression>", REM, MUL, DIV, INT_DIV)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, "<multiplicative expression>");
    result_ = multiplicativeOperator(builder_, level_ + 1);
    result_ = result_ && prefixExpression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, MULTIPLICATIVE_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // prefixExpression multiplicativeExpression*
  static boolean multiplicativeExpressionWrapper(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicativeExpressionWrapper")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = prefixExpression(builder_, level_ + 1);
    result_ = result_ && multiplicativeExpressionWrapper_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // multiplicativeExpression*
  private static boolean multiplicativeExpressionWrapper_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicativeExpressionWrapper_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!multiplicativeExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "multiplicativeExpressionWrapper_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // '*' | '/' | '%' | '~/'
  public static boolean multiplicativeOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "multiplicativeOperator")) return false;
    if (!nextTokenIs(builder_, "<multiplicative operator>", REM, MUL, DIV, INT_DIV)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<multiplicative operator>");
    result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, DIV);
    if (!result_) result_ = consumeToken(builder_, REM);
    if (!result_) result_ = consumeToken(builder_, INT_DIV);
    exit_section_(builder_, level_, marker_, MULTIPLICATIVE_OPERATOR, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // parameterNameReferenceExpression ':' expression
  public static boolean namedArgument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedArgument")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<named argument>");
    result_ = parameterNameReferenceExpression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, NAMED_ARGUMENT, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'const')* referenceExpression '.' componentName formalParameterList initializers? (';' | functionBodyOrNative | redirection)?
  public static boolean namedConstructorDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedConstructorDeclaration")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<named constructor declaration>");
    result_ = namedConstructorDeclaration_0(builder_, level_ + 1);
    result_ = result_ && namedConstructorDeclaration_1(builder_, level_ + 1);
    result_ = result_ && referenceExpression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, DOT);
    result_ = result_ && componentName(builder_, level_ + 1);
    result_ = result_ && formalParameterList(builder_, level_ + 1);
    pinned_ = result_; // pin = 6
    result_ = result_ && report_error_(builder_, namedConstructorDeclaration_6(builder_, level_ + 1));
    result_ = pinned_ && namedConstructorDeclaration_7(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, NAMED_CONSTRUCTOR_DECLARATION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean namedConstructorDeclaration_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedConstructorDeclaration_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "namedConstructorDeclaration_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('external' | 'const')*
  private static boolean namedConstructorDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedConstructorDeclaration_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!namedConstructorDeclaration_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "namedConstructorDeclaration_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external' | 'const'
  private static boolean namedConstructorDeclaration_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedConstructorDeclaration_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EXTERNAL);
    if (!result_) result_ = consumeToken(builder_, CONST);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // initializers?
  private static boolean namedConstructorDeclaration_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedConstructorDeclaration_6")) return false;
    initializers(builder_, level_ + 1);
    return true;
  }

  // (';' | functionBodyOrNative | redirection)?
  private static boolean namedConstructorDeclaration_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedConstructorDeclaration_7")) return false;
    namedConstructorDeclaration_7_0(builder_, level_ + 1);
    return true;
  }

  // ';' | functionBodyOrNative | redirection
  private static boolean namedConstructorDeclaration_7_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedConstructorDeclaration_7_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = functionBodyOrNative(builder_, level_ + 1);
    if (!result_) result_ = redirection(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '[' defaultFormalNamedParameter (',' defaultFormalNamedParameter)* ']' |
  //                           '{' defaultFormalNamedParameter (',' defaultFormalNamedParameter)* '}'
  public static boolean namedFormalParameters(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedFormalParameters")) return false;
    if (!nextTokenIs(builder_, "<named formal parameters>", LBRACKET, LBRACE)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<named formal parameters>");
    result_ = namedFormalParameters_0(builder_, level_ + 1);
    if (!result_) result_ = namedFormalParameters_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, NAMED_FORMAL_PARAMETERS, result_, false, null);
    return result_;
  }

  // '[' defaultFormalNamedParameter (',' defaultFormalNamedParameter)* ']'
  private static boolean namedFormalParameters_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedFormalParameters_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACKET);
    result_ = result_ && defaultFormalNamedParameter(builder_, level_ + 1);
    result_ = result_ && namedFormalParameters_0_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACKET);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (',' defaultFormalNamedParameter)*
  private static boolean namedFormalParameters_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedFormalParameters_0_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!namedFormalParameters_0_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "namedFormalParameters_0_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' defaultFormalNamedParameter
  private static boolean namedFormalParameters_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedFormalParameters_0_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && defaultFormalNamedParameter(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '{' defaultFormalNamedParameter (',' defaultFormalNamedParameter)* '}'
  private static boolean namedFormalParameters_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedFormalParameters_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACE);
    result_ = result_ && defaultFormalNamedParameter(builder_, level_ + 1);
    result_ = result_ && namedFormalParameters_1_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (',' defaultFormalNamedParameter)*
  private static boolean namedFormalParameters_1_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedFormalParameters_1_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!namedFormalParameters_1_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "namedFormalParameters_1_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' defaultFormalNamedParameter
  private static boolean namedFormalParameters_1_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "namedFormalParameters_1_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && defaultFormalNamedParameter(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'new' type ('.' referenceExpression)? arguments
  public static boolean newExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpression")) return false;
    if (!nextTokenIs(builder_, NEW)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, NEW);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, type(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, newExpression_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && arguments(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, NEW_EXPRESSION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ('.' referenceExpression)?
  private static boolean newExpression_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpression_2")) return false;
    newExpression_2_0(builder_, level_ + 1);
    return true;
  }

  // '.' referenceExpression
  private static boolean newExpression_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "newExpression_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && referenceExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
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
  //                                | throwStatementWithSemicolon
  //                                | assertStatement
  //                                | statementFollowedBySemiColon
  //                                | ';'
  static boolean nonLabelledStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = block(builder_, level_ + 1);
    if (!result_) result_ = nonLabelledStatement_1(builder_, level_ + 1);
    if (!result_) result_ = nonLabelledStatement_2(builder_, level_ + 1);
    if (!result_) result_ = nonLabelledStatement_3(builder_, level_ + 1);
    if (!result_) result_ = nonLabelledStatement_4(builder_, level_ + 1);
    if (!result_) result_ = nonLabelledStatement_5(builder_, level_ + 1);
    if (!result_) result_ = nonLabelledStatement_6(builder_, level_ + 1);
    if (!result_) result_ = rethrowStatement(builder_, level_ + 1);
    if (!result_) result_ = tryStatement(builder_, level_ + 1);
    if (!result_) result_ = breakStatement(builder_, level_ + 1);
    if (!result_) result_ = continueStatement(builder_, level_ + 1);
    if (!result_) result_ = returnStatement(builder_, level_ + 1);
    if (!result_) result_ = throwStatementWithSemicolon(builder_, level_ + 1);
    if (!result_) result_ = assertStatement(builder_, level_ + 1);
    if (!result_) result_ = statementFollowedBySemiColon(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // functionDeclarationWithBody ';'?
  private static boolean nonLabelledStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = functionDeclarationWithBody(builder_, level_ + 1);
    result_ = result_ && nonLabelledStatement_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';'?
  private static boolean nonLabelledStatement_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_1_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  // forStatement ';'?
  private static boolean nonLabelledStatement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = forStatement(builder_, level_ + 1);
    result_ = result_ && nonLabelledStatement_2_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';'?
  private static boolean nonLabelledStatement_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_2_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  // whileStatement ';'?
  private static boolean nonLabelledStatement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_3")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = whileStatement(builder_, level_ + 1);
    result_ = result_ && nonLabelledStatement_3_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';'?
  private static boolean nonLabelledStatement_3_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_3_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  // doWhileStatement ';'?
  private static boolean nonLabelledStatement_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_4")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = doWhileStatement(builder_, level_ + 1);
    result_ = result_ && nonLabelledStatement_4_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';'?
  private static boolean nonLabelledStatement_4_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_4_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  // switchStatement ';'?
  private static boolean nonLabelledStatement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_5")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = switchStatement(builder_, level_ + 1);
    result_ = result_ && nonLabelledStatement_5_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';'?
  private static boolean nonLabelledStatement_5_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_5_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  // ifStatement ';'?
  private static boolean nonLabelledStatement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_6")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ifStatement(builder_, level_ + 1);
    result_ = result_ && nonLabelledStatement_6_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';'?
  private static boolean nonLabelledStatement_6_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nonLabelledStatement_6_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // functionSignature
  //                         | fieldFormalParameter
  //                         | simpleFormalParameter
  public static boolean normalFormalParameter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "normalFormalParameter")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<normal formal parameter>");
    result_ = functionSignature(builder_, level_ + 1);
    if (!result_) result_ = fieldFormalParameter(builder_, level_ + 1);
    if (!result_) result_ = simpleFormalParameter(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, NORMAL_FORMAL_PARAMETER, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // !')'
  static boolean not_paren_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "not_paren_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !consumeToken(builder_, RPAREN);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'on' type catchPart?
  public static boolean onPart(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "onPart")) return false;
    if (!nextTokenIs(builder_, ON)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, ON);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, type(builder_, level_ + 1));
    result_ = pinned_ && onPart_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, ON_PART, result_, pinned_, null);
    return result_ || pinned_;
  }

  // catchPart?
  private static boolean onPart_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "onPart_2")) return false;
    catchPart(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // operatorDeclarationWithReturnType | operatorDeclarationWithoutReturnType
  public static boolean operatorDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorDeclaration")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<operator declaration>");
    result_ = operatorDeclarationWithReturnType(builder_, level_ + 1);
    if (!result_) result_ = operatorDeclarationWithoutReturnType(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, OPERATOR_DECLARATION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // metadata* 'external'? returnType 'operator' userDefinableOperator formalParameterList (';' | functionBodyOrNative)
  static boolean operatorDeclarationWithReturnType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorDeclarationWithReturnType")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = operatorDeclarationWithReturnType_0(builder_, level_ + 1);
    result_ = result_ && operatorDeclarationWithReturnType_1(builder_, level_ + 1);
    result_ = result_ && returnType(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OPERATOR);
    result_ = result_ && userDefinableOperator(builder_, level_ + 1);
    pinned_ = result_; // pin = 5
    result_ = result_ && report_error_(builder_, formalParameterList(builder_, level_ + 1));
    result_ = pinned_ && operatorDeclarationWithReturnType_6(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean operatorDeclarationWithReturnType_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorDeclarationWithReturnType_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "operatorDeclarationWithReturnType_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external'?
  private static boolean operatorDeclarationWithReturnType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorDeclarationWithReturnType_1")) return false;
    consumeToken(builder_, EXTERNAL);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean operatorDeclarationWithReturnType_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorDeclarationWithReturnType_6")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = functionBodyOrNative(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata* 'external'?            'operator' userDefinableOperator formalParameterList (';' | functionBodyOrNative)
  static boolean operatorDeclarationWithoutReturnType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorDeclarationWithoutReturnType")) return false;
    if (!nextTokenIs(builder_, "", AT, EXTERNAL, OPERATOR)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = operatorDeclarationWithoutReturnType_0(builder_, level_ + 1);
    result_ = result_ && operatorDeclarationWithoutReturnType_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OPERATOR);
    result_ = result_ && userDefinableOperator(builder_, level_ + 1);
    pinned_ = result_; // pin = 4
    result_ = result_ && report_error_(builder_, formalParameterList(builder_, level_ + 1));
    result_ = pinned_ && operatorDeclarationWithoutReturnType_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean operatorDeclarationWithoutReturnType_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorDeclarationWithoutReturnType_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "operatorDeclarationWithoutReturnType_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external'?
  private static boolean operatorDeclarationWithoutReturnType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorDeclarationWithoutReturnType_1")) return false;
    consumeToken(builder_, EXTERNAL);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean operatorDeclarationWithoutReturnType_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "operatorDeclarationWithoutReturnType_5")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = functionBodyOrNative(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean parameterNameReferenceExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameterNameReferenceExpression")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<parameter name reference expression>");
    result_ = nonStrictID(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, PARAMETER_NAME_REFERENCE_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // !')'
  static boolean parenthesesRecovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesesRecovery")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !consumeToken(builder_, RPAREN);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '(' expressionInParentheses ')'
  public static boolean parenthesizedExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesizedExpression")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, expressionInParentheses(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, PARENTHESIZED_EXPRESSION, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // metadata* 'part' 'of' libraryId ';'
  public static boolean partOfStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "partOfStatement")) return false;
    if (!nextTokenIs(builder_, "<part of statement>", AT, PART)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<part of statement>");
    result_ = partOfStatement_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PART);
    result_ = result_ && consumeToken(builder_, OF);
    result_ = result_ && libraryId(builder_, level_ + 1);
    pinned_ = result_; // pin = 4
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, PART_OF_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean partOfStatement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "partOfStatement_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "partOfStatement_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* 'part' pathOrLibraryReference ';'
  public static boolean partStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "partStatement")) return false;
    if (!nextTokenIs(builder_, "<part statement>", AT, PART)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<part statement>");
    result_ = partStatement_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PART);
    result_ = result_ && pathOrLibraryReference(builder_, level_ + 1);
    pinned_ = result_; // pin = 3
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, PART_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean partStatement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "partStatement_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "partStatement_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // stringLiteralExpression
  public static boolean pathOrLibraryReference(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pathOrLibraryReference")) return false;
    if (!nextTokenIs(builder_, "<path or library reference>", OPEN_QUOTE, RAW_SINGLE_QUOTED_STRING, RAW_TRIPLE_QUOTED_STRING)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<path or library reference>");
    result_ = stringLiteralExpression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, PATH_OR_LIBRARY_REFERENCE, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // (prefixOperator prefixExpression) | suffixExpressionWrapper
  public static boolean prefixExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "prefixExpression")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<prefix expression>");
    result_ = prefixExpression_0(builder_, level_ + 1);
    if (!result_) result_ = suffixExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, PREFIX_EXPRESSION, result_, false, null);
    return result_;
  }

  // prefixOperator prefixExpression
  private static boolean prefixExpression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "prefixExpression_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = prefixOperator(builder_, level_ + 1);
    result_ = result_ && prefixExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '-' | '+' | '--' | '++' | '!' | '~' | '?'
  public static boolean prefixOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "prefixOperator")) return false;
    if (!nextTokenIs(builder_, "<prefix operator>", NOT, PLUS,
      PLUS_PLUS, MINUS, MINUS_MINUS, QUEST, BIN_NOT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<prefix operator>");
    result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS_MINUS);
    if (!result_) result_ = consumeToken(builder_, PLUS_PLUS);
    if (!result_) result_ = consumeToken(builder_, NOT);
    if (!result_) result_ = consumeToken(builder_, BIN_NOT);
    if (!result_) result_ = consumeToken(builder_, QUEST);
    exit_section_(builder_, level_, marker_, PREFIX_OPERATOR, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // literalExpression |
  //                      functionExpression |
  //                      newExpression |
  //                      constConstructorExpression |
  //                      refOrThisOrSuperOrParenExpression
  static boolean primary(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "primary")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = literalExpression(builder_, level_ + 1);
    if (!result_) result_ = functionExpression(builder_, level_ + 1);
    if (!result_) result_ = newExpression(builder_, level_ + 1);
    if (!result_) result_ = constConstructorExpression(builder_, level_ + 1);
    if (!result_) result_ = refOrThisOrSuperOrParenExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // << nonStrictID >> ('.' << nonStrictID >>)*
  public static boolean qualifiedComponentName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualifiedComponentName")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<qualified component name>");
    result_ = nonStrictID(builder_, level_ + 1);
    result_ = result_ && qualifiedComponentName_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, QUALIFIED_COMPONENT_NAME, result_, false, null);
    return result_;
  }

  // ('.' << nonStrictID >>)*
  private static boolean qualifiedComponentName_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualifiedComponentName_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!qualifiedComponentName_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "qualifiedComponentName_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // '.' << nonStrictID >>
  private static boolean qualifiedComponentName_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualifiedComponentName_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && nonStrictID(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '.' referenceExpression
  public static boolean qualifiedReferenceExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualifiedReferenceExpression")) return false;
    if (!nextTokenIs(builder_, DOT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && referenceExpression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, REFERENCE_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ':' 'this' ('.' referenceExpression)? arguments
  public static boolean redirection(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "redirection")) return false;
    if (!nextTokenIs(builder_, COLON)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, COLON);
    result_ = result_ && consumeToken(builder_, THIS);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, redirection_2(builder_, level_ + 1));
    result_ = pinned_ && arguments(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, REDIRECTION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ('.' referenceExpression)?
  private static boolean redirection_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "redirection_2")) return false;
    redirection_2_0(builder_, level_ + 1);
    return true;
  }

  // '.' referenceExpression
  private static boolean redirection_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "redirection_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && referenceExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // referenceExpression | thisExpression | superExpression | << parenthesizedExpressionWrapper >>
  static boolean refOrThisOrSuperOrParenExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "refOrThisOrSuperOrParenExpression")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = referenceExpression(builder_, level_ + 1);
    if (!result_) result_ = thisExpression(builder_, level_ + 1);
    if (!result_) result_ = superExpression(builder_, level_ + 1);
    if (!result_) result_ = parenthesizedExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean referenceExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "referenceExpression")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<reference expression>");
    result_ = nonStrictID(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, REFERENCE_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '>=' | '>' | '<=' | '<'
  public static boolean relationalOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "relationalOperator")) return false;
    if (!nextTokenIs(builder_, "<relational operator>", LT, LT_EQ, GT, GT_EQ)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<relational operator>");
    result_ = consumeToken(builder_, GT_EQ);
    if (!result_) result_ = consumeToken(builder_, GT);
    if (!result_) result_ = consumeToken(builder_, LT_EQ);
    if (!result_) result_ = consumeToken(builder_, LT);
    exit_section_(builder_, level_, marker_, RELATIONAL_OPERATOR, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'rethrow' ';'
  public static boolean rethrowStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rethrowStatement")) return false;
    if (!nextTokenIs(builder_, RETHROW)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, RETHROW);
    pinned_ = result_; // pin = 1
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, RETHROW_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // 'return' expression? ';'
  public static boolean returnStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "returnStatement")) return false;
    if (!nextTokenIs(builder_, RETURN)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, RETURN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, returnStatement_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, RETURN_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // expression?
  private static boolean returnStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "returnStatement_1")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'void' | type
  public static boolean returnType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "returnType")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<return type>");
    result_ = consumeToken(builder_, VOID);
    if (!result_) result_ = type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, RETURN_TYPE, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // !(';')
  static boolean semicolon_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "semicolon_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !semicolon_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // (';')
  private static boolean semicolon_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "semicolon_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // setterDeclarationWithReturnType | setterDeclarationWithoutReturnType
  public static boolean setterDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclaration")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<setter declaration>");
    result_ = setterDeclarationWithReturnType(builder_, level_ + 1);
    if (!result_) result_ = setterDeclarationWithoutReturnType(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SETTER_DECLARATION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')* returnType 'set' componentName formalParameterList (';' | functionBodyOrNative)
  static boolean setterDeclarationWithReturnType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclarationWithReturnType")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = setterDeclarationWithReturnType_0(builder_, level_ + 1);
    result_ = result_ && setterDeclarationWithReturnType_1(builder_, level_ + 1);
    result_ = result_ && returnType(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SET);
    result_ = result_ && componentName(builder_, level_ + 1);
    pinned_ = result_; // pin = 5
    result_ = result_ && report_error_(builder_, formalParameterList(builder_, level_ + 1));
    result_ = pinned_ && setterDeclarationWithReturnType_6(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean setterDeclarationWithReturnType_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclarationWithReturnType_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "setterDeclarationWithReturnType_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean setterDeclarationWithReturnType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclarationWithReturnType_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!setterDeclarationWithReturnType_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "setterDeclarationWithReturnType_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean setterDeclarationWithReturnType_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclarationWithReturnType_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EXTERNAL);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';' | functionBodyOrNative
  private static boolean setterDeclarationWithReturnType_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclarationWithReturnType_6")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = functionBodyOrNative(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')*            'set' componentName formalParameterList (';' | functionBodyOrNative)
  static boolean setterDeclarationWithoutReturnType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclarationWithoutReturnType")) return false;
    if (!nextTokenIs(builder_, "", AT, EXTERNAL, SET, STATIC)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = setterDeclarationWithoutReturnType_0(builder_, level_ + 1);
    result_ = result_ && setterDeclarationWithoutReturnType_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SET);
    result_ = result_ && componentName(builder_, level_ + 1);
    pinned_ = result_; // pin = 4
    result_ = result_ && report_error_(builder_, formalParameterList(builder_, level_ + 1));
    result_ = pinned_ && setterDeclarationWithoutReturnType_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata*
  private static boolean setterDeclarationWithoutReturnType_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclarationWithoutReturnType_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "setterDeclarationWithoutReturnType_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean setterDeclarationWithoutReturnType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclarationWithoutReturnType_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!setterDeclarationWithoutReturnType_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "setterDeclarationWithoutReturnType_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean setterDeclarationWithoutReturnType_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclarationWithoutReturnType_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EXTERNAL);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';' | functionBodyOrNative
  private static boolean setterDeclarationWithoutReturnType_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "setterDeclarationWithoutReturnType_5")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = functionBodyOrNative(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // shiftOperator additiveExpressionWrapper
  public static boolean shiftExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shiftExpression")) return false;
    if (!nextTokenIs(builder_, "<shift expression>", LT_LT, GT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, "<shift expression>");
    result_ = shiftOperator(builder_, level_ + 1);
    result_ = result_ && additiveExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SHIFT_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // additiveExpressionWrapper shiftExpression*
  static boolean shiftExpressionWrapper(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shiftExpressionWrapper")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = additiveExpressionWrapper(builder_, level_ + 1);
    result_ = result_ && shiftExpressionWrapper_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // shiftExpression*
  private static boolean shiftExpressionWrapper_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shiftExpressionWrapper_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!shiftExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "shiftExpressionWrapper_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // '<<' | shiftRightOperator
  public static boolean shiftOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shiftOperator")) return false;
    if (!nextTokenIs(builder_, "<shift operator>", LT_LT, GT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<shift operator>");
    result_ = consumeToken(builder_, LT_LT);
    if (!result_) result_ = shiftRightOperator(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SHIFT_OPERATOR, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '>' '>'
  public static boolean shiftRightOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shiftRightOperator")) return false;
    if (!nextTokenIs(builder_, GT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, GT);
    result_ = result_ && consumeToken(builder_, GT);
    exit_section_(builder_, marker_, SHIFT_RIGHT_OPERATOR, result_);
    return result_;
  }

  /* ********************************************************** */
  // SHORT_TEMPLATE_ENTRY_START (thisExpression | referenceExpression)
  public static boolean shortTemplateEntry(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shortTemplateEntry")) return false;
    if (!nextTokenIs(builder_, SHORT_TEMPLATE_ENTRY_START)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, SHORT_TEMPLATE_ENTRY_START);
    pinned_ = result_; // pin = 1
    result_ = result_ && shortTemplateEntry_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SHORT_TEMPLATE_ENTRY, result_, pinned_, null);
    return result_ || pinned_;
  }

  // thisExpression | referenceExpression
  private static boolean shortTemplateEntry_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "shortTemplateEntry_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = thisExpression(builder_, level_ + 1);
    if (!result_) result_ = referenceExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'show' libraryReferenceList
  public static boolean showCombinator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "showCombinator")) return false;
    if (!nextTokenIs(builder_, SHOW)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SHOW);
    result_ = result_ && libraryReferenceList(builder_, level_ + 1);
    exit_section_(builder_, marker_, SHOW_COMBINATOR, result_);
    return result_;
  }

  /* ********************************************************** */
  // declaredIdentifier | metadata* componentName
  public static boolean simpleFormalParameter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simpleFormalParameter")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<simple formal parameter>");
    result_ = declaredIdentifier(builder_, level_ + 1);
    if (!result_) result_ = simpleFormalParameter_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SIMPLE_FORMAL_PARAMETER, result_, false, null);
    return result_;
  }

  // metadata* componentName
  private static boolean simpleFormalParameter_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simpleFormalParameter_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = simpleFormalParameter_1_0(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // metadata*
  private static boolean simpleFormalParameter_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simpleFormalParameter_1_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "simpleFormalParameter_1_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // referenceExpression qualifiedReferenceExpression*
  public static boolean simpleQualifiedReferenceExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simpleQualifiedReferenceExpression")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<simple qualified reference expression>");
    result_ = referenceExpression(builder_, level_ + 1);
    result_ = result_ && simpleQualifiedReferenceExpression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, REFERENCE_EXPRESSION, result_, false, null);
    return result_;
  }

  // qualifiedReferenceExpression*
  private static boolean simpleQualifiedReferenceExpression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simpleQualifiedReferenceExpression_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!qualifiedReferenceExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "simpleQualifiedReferenceExpression_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // !'}'
  static boolean simple_scope_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_scope_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !consumeToken(builder_, RBRACE);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // label* nonLabelledStatement
  static boolean statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = statement_0(builder_, level_ + 1);
    result_ = result_ && nonLabelledStatement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, statement_recover_parser_);
    return result_;
  }

  // label*
  private static boolean statement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!label(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "statement_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // (metadata* varDeclarationList | expression) ';'
  static boolean statementFollowedBySemiColon(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statementFollowedBySemiColon")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = statementFollowedBySemiColon_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // metadata* varDeclarationList | expression
  private static boolean statementFollowedBySemiColon_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statementFollowedBySemiColon_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = statementFollowedBySemiColon_0_0(builder_, level_ + 1);
    if (!result_) result_ = expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // metadata* varDeclarationList
  private static boolean statementFollowedBySemiColon_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statementFollowedBySemiColon_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = statementFollowedBySemiColon_0_0_0(builder_, level_ + 1);
    result_ = result_ && varDeclarationList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // metadata*
  private static boolean statementFollowedBySemiColon_0_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statementFollowedBySemiColon_0_0_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "statementFollowedBySemiColon_0_0_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // !('!' | '(' | ')' | '+' | '++' | '-' | '--' | ';' | '<' | '[' | 'assert' | 'break' | 'case' | 'const'
  //                               | 'continue' | 'default' | 'do' | 'else' | 'false' | 'final' | 'for' | 'if' | 'new' | 'null' | 'rethrow' | 'return'
  //                               | 'static' | 'super' | 'switch' | 'this' | 'throw' | 'true' | 'try' | 'var' | 'while' | '{' | '}' | '~'
  //                               | HEX_NUMBER | <<nonStrictID>> | NUMBER | OPEN_QUOTE | RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING
  //                               | "abstract" | "assert" | "class"  | "extends" | "factory" | "get" | "implements" | "import"
  //                               | "is" | "@" | "library" | "native" | "as" | "on" | "set" | "static" | "typedef" | "operator")
  static boolean statement_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !statement_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // '!' | '(' | ')' | '+' | '++' | '-' | '--' | ';' | '<' | '[' | 'assert' | 'break' | 'case' | 'const'
  //                               | 'continue' | 'default' | 'do' | 'else' | 'false' | 'final' | 'for' | 'if' | 'new' | 'null' | 'rethrow' | 'return'
  //                               | 'static' | 'super' | 'switch' | 'this' | 'throw' | 'true' | 'try' | 'var' | 'while' | '{' | '}' | '~'
  //                               | HEX_NUMBER | <<nonStrictID>> | NUMBER | OPEN_QUOTE | RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING
  //                               | "abstract" | "assert" | "class"  | "extends" | "factory" | "get" | "implements" | "import"
  //                               | "is" | "@" | "library" | "native" | "as" | "on" | "set" | "static" | "typedef" | "operator"
  private static boolean statement_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, NOT);
    if (!result_) result_ = consumeToken(builder_, LPAREN);
    if (!result_) result_ = consumeToken(builder_, RPAREN);
    if (!result_) result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, PLUS_PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, MINUS_MINUS);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, LT);
    if (!result_) result_ = consumeToken(builder_, LBRACKET);
    if (!result_) result_ = consumeToken(builder_, ASSERT);
    if (!result_) result_ = consumeToken(builder_, BREAK);
    if (!result_) result_ = consumeToken(builder_, CASE);
    if (!result_) result_ = consumeToken(builder_, CONST);
    if (!result_) result_ = consumeToken(builder_, CONTINUE);
    if (!result_) result_ = consumeToken(builder_, DEFAULT);
    if (!result_) result_ = consumeToken(builder_, DO);
    if (!result_) result_ = consumeToken(builder_, ELSE);
    if (!result_) result_ = consumeToken(builder_, FALSE);
    if (!result_) result_ = consumeToken(builder_, FINAL);
    if (!result_) result_ = consumeToken(builder_, FOR);
    if (!result_) result_ = consumeToken(builder_, IF);
    if (!result_) result_ = consumeToken(builder_, NEW);
    if (!result_) result_ = consumeToken(builder_, NULL);
    if (!result_) result_ = consumeToken(builder_, RETHROW);
    if (!result_) result_ = consumeToken(builder_, RETURN);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    if (!result_) result_ = consumeToken(builder_, SUPER);
    if (!result_) result_ = consumeToken(builder_, SWITCH);
    if (!result_) result_ = consumeToken(builder_, THIS);
    if (!result_) result_ = consumeToken(builder_, THROW);
    if (!result_) result_ = consumeToken(builder_, TRUE);
    if (!result_) result_ = consumeToken(builder_, TRY);
    if (!result_) result_ = consumeToken(builder_, VAR);
    if (!result_) result_ = consumeToken(builder_, WHILE);
    if (!result_) result_ = consumeToken(builder_, LBRACE);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, BIN_NOT);
    if (!result_) result_ = consumeToken(builder_, HEX_NUMBER);
    if (!result_) result_ = nonStrictID(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, NUMBER);
    if (!result_) result_ = consumeToken(builder_, OPEN_QUOTE);
    if (!result_) result_ = consumeToken(builder_, RAW_SINGLE_QUOTED_STRING);
    if (!result_) result_ = consumeToken(builder_, RAW_TRIPLE_QUOTED_STRING);
    if (!result_) result_ = consumeToken(builder_, ABSTRACT);
    if (!result_) result_ = consumeToken(builder_, ASSERT);
    if (!result_) result_ = consumeToken(builder_, CLASS);
    if (!result_) result_ = consumeToken(builder_, EXTENDS);
    if (!result_) result_ = consumeToken(builder_, FACTORY);
    if (!result_) result_ = consumeToken(builder_, GET);
    if (!result_) result_ = consumeToken(builder_, IMPLEMENTS);
    if (!result_) result_ = consumeToken(builder_, IMPORT);
    if (!result_) result_ = consumeToken(builder_, IS);
    if (!result_) result_ = consumeToken(builder_, AT);
    if (!result_) result_ = consumeToken(builder_, LIBRARY);
    if (!result_) result_ = consumeToken(builder_, NATIVE);
    if (!result_) result_ = consumeToken(builder_, AS);
    if (!result_) result_ = consumeToken(builder_, ON);
    if (!result_) result_ = consumeToken(builder_, SET);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    if (!result_) result_ = consumeToken(builder_, TYPEDEF);
    if (!result_) result_ = consumeToken(builder_, OPERATOR);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // statement*
  public static boolean statements(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statements")) return false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<statements>");
    int pos_ = current_position_(builder_);
    while (true) {
      if (!statement(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "statements", pos_)) break;
      pos_ = current_position_(builder_);
    }
    exit_section_(builder_, level_, marker_, STATEMENTS, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // (RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING | stringTemplate)+
  public static boolean stringLiteralExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "stringLiteralExpression")) return false;
    if (!nextTokenIs(builder_, "<string literal expression>", OPEN_QUOTE, RAW_SINGLE_QUOTED_STRING, RAW_TRIPLE_QUOTED_STRING)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<string literal expression>");
    result_ = stringLiteralExpression_0(builder_, level_ + 1);
    int pos_ = current_position_(builder_);
    while (result_) {
      if (!stringLiteralExpression_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "stringLiteralExpression", pos_)) break;
      pos_ = current_position_(builder_);
    }
    exit_section_(builder_, level_, marker_, STRING_LITERAL_EXPRESSION, result_, false, null);
    return result_;
  }

  // RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING | stringTemplate
  private static boolean stringLiteralExpression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "stringLiteralExpression_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RAW_SINGLE_QUOTED_STRING);
    if (!result_) result_ = consumeToken(builder_, RAW_TRIPLE_QUOTED_STRING);
    if (!result_) result_ = stringTemplate(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // OPEN_QUOTE (REGULAR_STRING_PART | shortTemplateEntry | longTemplateEntry)* CLOSING_QUOTE
  static boolean stringTemplate(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "stringTemplate")) return false;
    if (!nextTokenIs(builder_, OPEN_QUOTE)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, OPEN_QUOTE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, stringTemplate_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, CLOSING_QUOTE) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (REGULAR_STRING_PART | shortTemplateEntry | longTemplateEntry)*
  private static boolean stringTemplate_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "stringTemplate_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!stringTemplate_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "stringTemplate_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // REGULAR_STRING_PART | shortTemplateEntry | longTemplateEntry
  private static boolean stringTemplate_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "stringTemplate_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, REGULAR_STRING_PART);
    if (!result_) result_ = shortTemplateEntry(builder_, level_ + 1);
    if (!result_) result_ = longTemplateEntry(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '--' | '++'
  public static boolean suffixExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "suffixExpression")) return false;
    if (!nextTokenIs(builder_, "<suffix expression>", PLUS_PLUS, MINUS_MINUS)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, "<suffix expression>");
    result_ = consumeToken(builder_, MINUS_MINUS);
    if (!result_) result_ = consumeToken(builder_, PLUS_PLUS);
    exit_section_(builder_, level_, marker_, SUFFIX_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // valueExpression suffixExpression*
  static boolean suffixExpressionWrapper(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "suffixExpressionWrapper")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = valueExpression(builder_, level_ + 1);
    result_ = result_ && suffixExpressionWrapper_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // suffixExpression*
  private static boolean suffixExpressionWrapper_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "suffixExpressionWrapper_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!suffixExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "suffixExpressionWrapper_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // ('super' | 'this') ('.' referenceExpression)? arguments
  //                               | fieldInitializer
  public static boolean superCallOrFieldInitializer(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "superCallOrFieldInitializer")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<super call or field initializer>");
    result_ = superCallOrFieldInitializer_0(builder_, level_ + 1);
    if (!result_) result_ = fieldInitializer(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SUPER_CALL_OR_FIELD_INITIALIZER, result_, false, super_call_or_field_initializer_recover_parser_);
    return result_;
  }

  // ('super' | 'this') ('.' referenceExpression)? arguments
  private static boolean superCallOrFieldInitializer_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "superCallOrFieldInitializer_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = superCallOrFieldInitializer_0_0(builder_, level_ + 1);
    result_ = result_ && superCallOrFieldInitializer_0_1(builder_, level_ + 1);
    result_ = result_ && arguments(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'super' | 'this'
  private static boolean superCallOrFieldInitializer_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "superCallOrFieldInitializer_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SUPER);
    if (!result_) result_ = consumeToken(builder_, THIS);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ('.' referenceExpression)?
  private static boolean superCallOrFieldInitializer_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "superCallOrFieldInitializer_0_1")) return false;
    superCallOrFieldInitializer_0_1_0(builder_, level_ + 1);
    return true;
  }

  // '.' referenceExpression
  private static boolean superCallOrFieldInitializer_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "superCallOrFieldInitializer_0_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && referenceExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'super'
  public static boolean superExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "superExpression")) return false;
    if (!nextTokenIs(builder_, SUPER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SUPER);
    exit_section_(builder_, marker_, SUPER_EXPRESSION, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(')' | ',' | ':' | ';' | '=' | '=>' | ']' | 'abstract' | 'const' | 'factory'
  //                                                     | 'final' | 'get' | 'native' | 'operator' | 'set' | 'static' | 'var' | '{' | '}'
  //                                                     | <<nonStrictID>>)
  static boolean super_call_or_field_initializer_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "super_call_or_field_initializer_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !super_call_or_field_initializer_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // ')' | ',' | ':' | ';' | '=' | '=>' | ']' | 'abstract' | 'const' | 'factory'
  //                                                     | 'final' | 'get' | 'native' | 'operator' | 'set' | 'static' | 'var' | '{' | '}'
  //                                                     | <<nonStrictID>>
  private static boolean super_call_or_field_initializer_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "super_call_or_field_initializer_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RPAREN);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, COLON);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, EQ);
    if (!result_) result_ = consumeToken(builder_, EXPRESSION_BODY_DEF);
    if (!result_) result_ = consumeToken(builder_, RBRACKET);
    if (!result_) result_ = consumeToken(builder_, ABSTRACT);
    if (!result_) result_ = consumeToken(builder_, CONST);
    if (!result_) result_ = consumeToken(builder_, FACTORY);
    if (!result_) result_ = consumeToken(builder_, FINAL);
    if (!result_) result_ = consumeToken(builder_, GET);
    if (!result_) result_ = consumeToken(builder_, NATIVE);
    if (!result_) result_ = consumeToken(builder_, OPERATOR);
    if (!result_) result_ = consumeToken(builder_, SET);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    if (!result_) result_ = consumeToken(builder_, VAR);
    if (!result_) result_ = consumeToken(builder_, LBRACE);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = nonStrictID(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'extends' type
  public static boolean superclass(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "superclass")) return false;
    if (!nextTokenIs(builder_, EXTENDS)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, EXTENDS);
    pinned_ = result_; // pin = 1
    result_ = result_ && type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SUPERCLASS, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // label? ('case' expression ':')+ statements
  public static boolean switchCase(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "switchCase")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<switch case>");
    result_ = switchCase_0(builder_, level_ + 1);
    result_ = result_ && switchCase_1(builder_, level_ + 1);
    result_ = result_ && statements(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SWITCH_CASE, result_, false, switch_case_recover_parser_);
    return result_;
  }

  // label?
  private static boolean switchCase_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "switchCase_0")) return false;
    label(builder_, level_ + 1);
    return true;
  }

  // ('case' expression ':')+
  private static boolean switchCase_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "switchCase_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = switchCase_1_0(builder_, level_ + 1);
    int pos_ = current_position_(builder_);
    while (result_) {
      if (!switchCase_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "switchCase_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'case' expression ':'
  private static boolean switchCase_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "switchCase_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CASE);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'switch' '(' expressionWithRecoverUntilParen ')' '{' switchCase* defaultCase? '}'
  public static boolean switchStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "switchStatement")) return false;
    if (!nextTokenIs(builder_, SWITCH)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, SWITCH);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, LPAREN));
    result_ = pinned_ && report_error_(builder_, expressionWithRecoverUntilParen(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RPAREN)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, LBRACE)) && result_;
    result_ = pinned_ && report_error_(builder_, switchStatement_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, switchStatement_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, SWITCH_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // switchCase*
  private static boolean switchStatement_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "switchStatement_5")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!switchCase(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "switchStatement_5", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // defaultCase?
  private static boolean switchStatement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "switchStatement_6")) return false;
    defaultCase(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // !('case' | 'default' | '}' | <<nonStrictID>>)
  static boolean switch_case_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "switch_case_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !switch_case_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // 'case' | 'default' | '}' | <<nonStrictID>>
  private static boolean switch_case_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "switch_case_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CASE);
    if (!result_) result_ = consumeToken(builder_, DEFAULT);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = nonStrictID(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // HASH (userDefinableOperator | simpleQualifiedReferenceExpression)
  public static boolean symbolLiteralExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbolLiteralExpression")) return false;
    if (!nextTokenIs(builder_, HASH)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, HASH);
    pinned_ = result_; // pin = 1
    result_ = result_ && symbolLiteralExpression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SYMBOL_LITERAL_EXPRESSION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // userDefinableOperator | simpleQualifiedReferenceExpression
  private static boolean symbolLiteralExpression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbolLiteralExpression_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = userDefinableOperator(builder_, level_ + 1);
    if (!result_) result_ = simpleQualifiedReferenceExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '?' expression ':' ternaryExpressionWrapper
  public static boolean ternaryExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ternaryExpression")) return false;
    if (!nextTokenIs(builder_, QUEST)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = consumeToken(builder_, QUEST);
    result_ = result_ && expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && ternaryExpressionWrapper(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, TERNARY_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // logicOrExpressionWrapper ternaryExpression?
  static boolean ternaryExpressionWrapper(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ternaryExpressionWrapper")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = logicOrExpressionWrapper(builder_, level_ + 1);
    result_ = result_ && ternaryExpressionWrapper_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ternaryExpression?
  private static boolean ternaryExpressionWrapper_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ternaryExpressionWrapper_1")) return false;
    ternaryExpression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'this'
  public static boolean thisExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "thisExpression")) return false;
    if (!nextTokenIs(builder_, THIS)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, THIS);
    exit_section_(builder_, marker_, THIS_EXPRESSION, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'throw' expression?
  public static boolean throwStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "throwStatement")) return false;
    if (!nextTokenIs(builder_, THROW)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, THROW);
    pinned_ = result_; // pin = 1
    result_ = result_ && throwStatement_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, THROW_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // expression?
  private static boolean throwStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "throwStatement_1")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // throwStatement ';'
  static boolean throwStatementWithSemicolon(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "throwStatementWithSemicolon")) return false;
    if (!nextTokenIs(builder_, THROW)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = throwStatement(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // libraryStatement
  //                              | partOfStatement
  //                              | importStatement
  //                              | exportStatement
  //                              | partStatement
  //                              | classDefinition
  //                              | classTypeAlias
  //                              | functionTypeAlias
  //                              | getterOrSetterDeclaration
  //                              | functionDeclarationWithBodyOrNative
  //                              | varDeclarationListWithSemicolon
  //                              | incompleteDeclaration
  static boolean topLevelDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "topLevelDefinition")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = libraryStatement(builder_, level_ + 1);
    if (!result_) result_ = partOfStatement(builder_, level_ + 1);
    if (!result_) result_ = importStatement(builder_, level_ + 1);
    if (!result_) result_ = exportStatement(builder_, level_ + 1);
    if (!result_) result_ = partStatement(builder_, level_ + 1);
    if (!result_) result_ = classDefinition(builder_, level_ + 1);
    if (!result_) result_ = classTypeAlias(builder_, level_ + 1);
    if (!result_) result_ = functionTypeAlias(builder_, level_ + 1);
    if (!result_) result_ = getterOrSetterDeclaration(builder_, level_ + 1);
    if (!result_) result_ = functionDeclarationWithBodyOrNative(builder_, level_ + 1);
    if (!result_) result_ = varDeclarationListWithSemicolon(builder_, level_ + 1);
    if (!result_) result_ = incompleteDeclaration(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, top_level_recover_parser_);
    return result_;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> | '@' | 'abstract' | 'class' | 'const' | 'export' | 'external' | 'final' | 'get' | 'import' | 'library' | 'part' | 'set' | 'static' | 'typedef' | 'var' | 'void')
  static boolean top_level_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_level_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !top_level_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // <<nonStrictID>> | '@' | 'abstract' | 'class' | 'const' | 'export' | 'external' | 'final' | 'get' | 'import' | 'library' | 'part' | 'set' | 'static' | 'typedef' | 'var' | 'void'
  private static boolean top_level_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "top_level_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = nonStrictID(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, AT);
    if (!result_) result_ = consumeToken(builder_, ABSTRACT);
    if (!result_) result_ = consumeToken(builder_, CLASS);
    if (!result_) result_ = consumeToken(builder_, CONST);
    if (!result_) result_ = consumeToken(builder_, EXPORT);
    if (!result_) result_ = consumeToken(builder_, EXTERNAL);
    if (!result_) result_ = consumeToken(builder_, FINAL);
    if (!result_) result_ = consumeToken(builder_, GET);
    if (!result_) result_ = consumeToken(builder_, IMPORT);
    if (!result_) result_ = consumeToken(builder_, LIBRARY);
    if (!result_) result_ = consumeToken(builder_, PART);
    if (!result_) result_ = consumeToken(builder_, SET);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    if (!result_) result_ = consumeToken(builder_, TYPEDEF);
    if (!result_) result_ = consumeToken(builder_, VAR);
    if (!result_) result_ = consumeToken(builder_, VOID);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'try' block ((onPart | catchPart)+ finallyPart? | finallyPart)
  public static boolean tryStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tryStatement")) return false;
    if (!nextTokenIs(builder_, TRY)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, TRY);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, block(builder_, level_ + 1));
    result_ = pinned_ && tryStatement_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, TRY_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (onPart | catchPart)+ finallyPart? | finallyPart
  private static boolean tryStatement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tryStatement_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = tryStatement_2_0(builder_, level_ + 1);
    if (!result_) result_ = finallyPart(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (onPart | catchPart)+ finallyPart?
  private static boolean tryStatement_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tryStatement_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = tryStatement_2_0_0(builder_, level_ + 1);
    result_ = result_ && tryStatement_2_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (onPart | catchPart)+
  private static boolean tryStatement_2_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tryStatement_2_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = tryStatement_2_0_0_0(builder_, level_ + 1);
    int pos_ = current_position_(builder_);
    while (result_) {
      if (!tryStatement_2_0_0_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "tryStatement_2_0_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // onPart | catchPart
  private static boolean tryStatement_2_0_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tryStatement_2_0_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = onPart(builder_, level_ + 1);
    if (!result_) result_ = catchPart(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // finallyPart?
  private static boolean tryStatement_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "tryStatement_2_0_1")) return false;
    finallyPart(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // simpleQualifiedReferenceExpression typeArguments?
  public static boolean type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<type>");
    result_ = simpleQualifiedReferenceExpression(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && type_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, TYPE, result_, pinned_, null);
    return result_ || pinned_;
  }

  // typeArguments?
  private static boolean type_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_1")) return false;
    typeArguments(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // '<' typeList '>'
  public static boolean typeArguments(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeArguments")) return false;
    if (!nextTokenIs(builder_, LT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LT);
    result_ = result_ && typeList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, GT);
    exit_section_(builder_, marker_, TYPE_ARGUMENTS, result_);
    return result_;
  }

  /* ********************************************************** */
  // type (',' type)*
  public static boolean typeList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeList")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<type list>");
    result_ = type(builder_, level_ + 1);
    result_ = result_ && typeList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, TYPE_LIST, result_, false, null);
    return result_;
  }

  // (',' type)*
  private static boolean typeList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!typeList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "typeList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' type
  private static boolean typeList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeList_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata* componentName ('extends' type)?
  public static boolean typeParameter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeParameter")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<type parameter>");
    result_ = typeParameter_0(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    result_ = result_ && typeParameter_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, TYPE_PARAMETER, result_, false, type_parameter_recover_parser_);
    return result_;
  }

  // metadata*
  private static boolean typeParameter_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeParameter_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "typeParameter_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ('extends' type)?
  private static boolean typeParameter_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeParameter_2")) return false;
    typeParameter_2_0(builder_, level_ + 1);
    return true;
  }

  // 'extends' type
  private static boolean typeParameter_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeParameter_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EXTENDS);
    result_ = result_ && type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '<' typeParameter (',' typeParameter)* '>'
  public static boolean typeParameters(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeParameters")) return false;
    if (!nextTokenIs(builder_, LT)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LT);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, typeParameter(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, typeParameters_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, GT) && result_;
    exit_section_(builder_, level_, marker_, TYPE_PARAMETERS, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (',' typeParameter)*
  private static boolean typeParameters_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeParameters_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!typeParameters_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "typeParameters_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' typeParameter
  private static boolean typeParameters_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "typeParameters_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && typeParameter(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !('(' | ',' | '.' | '>' | 'extends' | 'factory' | 'implements' | '{')
  static boolean type_parameter_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_parameter_recover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !type_parameter_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // '(' | ',' | '.' | '>' | 'extends' | 'factory' | 'implements' | '{'
  private static boolean type_parameter_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_parameter_recover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, DOT);
    if (!result_) result_ = consumeToken(builder_, GT);
    if (!result_) result_ = consumeToken(builder_, EXTENDS);
    if (!result_) result_ = consumeToken(builder_, FACTORY);
    if (!result_) result_ = consumeToken(builder_, IMPLEMENTS);
    if (!result_) result_ = consumeToken(builder_, LBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // binaryOperator |
  //                           '~' |
  //                           '[' ']' '='?
  public static boolean userDefinableOperator(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "userDefinableOperator")) return false;
    if (!nextTokenIs(builder_, "<user definable operator>", REM, AND,
      MUL, PLUS, MINUS, DIV, LT, LT_LT,
      LT_EQ, EQ_EQ, GT, GT_EQ, LBRACKET, XOR,
      OR, BIN_NOT, INT_DIV)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<user definable operator>");
    result_ = binaryOperator(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, BIN_NOT);
    if (!result_) result_ = userDefinableOperator_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, USER_DEFINABLE_OPERATOR, result_, false, null);
    return result_;
  }

  // '[' ']' '='?
  private static boolean userDefinableOperator_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "userDefinableOperator_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACKET);
    result_ = result_ && consumeToken(builder_, RBRACKET);
    result_ = result_ && userDefinableOperator_2_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '='?
  private static boolean userDefinableOperator_2_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "userDefinableOperator_2_2")) return false;
    consumeToken(builder_, EQ);
    return true;
  }

  /* ********************************************************** */
  // primary callOrArrayAccessOrQualifiedRefExpression cascadeReferenceExpression* (isExpression | asExpression)?
  public static boolean valueExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "valueExpression")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<value expression>");
    result_ = primary(builder_, level_ + 1);
    result_ = result_ && callOrArrayAccessOrQualifiedRefExpression(builder_, level_ + 1);
    result_ = result_ && valueExpression_2(builder_, level_ + 1);
    result_ = result_ && valueExpression_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, VALUE_EXPRESSION, result_, false, null);
    return result_;
  }

  // cascadeReferenceExpression*
  private static boolean valueExpression_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "valueExpression_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!cascadeReferenceExpression(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "valueExpression_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // (isExpression | asExpression)?
  private static boolean valueExpression_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "valueExpression_3")) return false;
    valueExpression_3_0(builder_, level_ + 1);
    return true;
  }

  // isExpression | asExpression
  private static boolean valueExpression_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "valueExpression_3_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = isExpression(builder_, level_ + 1);
    if (!result_) result_ = asExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // metadata* 'static'? (finalOrConst type componentName | finalOrConst componentName | type componentName | 'var' componentName)
  public static boolean varAccessDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varAccessDeclaration")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<var access declaration>");
    result_ = varAccessDeclaration_0(builder_, level_ + 1);
    result_ = result_ && varAccessDeclaration_1(builder_, level_ + 1);
    result_ = result_ && varAccessDeclaration_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, VAR_ACCESS_DECLARATION, result_, false, null);
    return result_;
  }

  // metadata*
  private static boolean varAccessDeclaration_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varAccessDeclaration_0")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!metadata(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "varAccessDeclaration_0", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // 'static'?
  private static boolean varAccessDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varAccessDeclaration_1")) return false;
    consumeToken(builder_, STATIC);
    return true;
  }

  // finalOrConst type componentName | finalOrConst componentName | type componentName | 'var' componentName
  private static boolean varAccessDeclaration_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varAccessDeclaration_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = varAccessDeclaration_2_0(builder_, level_ + 1);
    if (!result_) result_ = varAccessDeclaration_2_1(builder_, level_ + 1);
    if (!result_) result_ = varAccessDeclaration_2_2(builder_, level_ + 1);
    if (!result_) result_ = varAccessDeclaration_2_3(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // finalOrConst type componentName
  private static boolean varAccessDeclaration_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varAccessDeclaration_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = finalOrConst(builder_, level_ + 1);
    result_ = result_ && type(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // finalOrConst componentName
  private static boolean varAccessDeclaration_2_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varAccessDeclaration_2_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = finalOrConst(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // type componentName
  private static boolean varAccessDeclaration_2_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varAccessDeclaration_2_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = type(builder_, level_ + 1);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // 'var' componentName
  private static boolean varAccessDeclaration_2_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varAccessDeclaration_2_3")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, VAR);
    result_ = result_ && componentName(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // varAccessDeclaration varInit? (',' varDeclarationListPart)*
  public static boolean varDeclarationList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varDeclarationList")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<var declaration list>");
    result_ = varAccessDeclaration(builder_, level_ + 1);
    result_ = result_ && varDeclarationList_1(builder_, level_ + 1);
    result_ = result_ && varDeclarationList_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, VAR_DECLARATION_LIST, result_, false, null);
    return result_;
  }

  // varInit?
  private static boolean varDeclarationList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varDeclarationList_1")) return false;
    varInit(builder_, level_ + 1);
    return true;
  }

  // (',' varDeclarationListPart)*
  private static boolean varDeclarationList_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varDeclarationList_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!varDeclarationList_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "varDeclarationList_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' varDeclarationListPart
  private static boolean varDeclarationList_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varDeclarationList_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && varDeclarationListPart(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // componentName varInit?
  public static boolean varDeclarationListPart(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varDeclarationListPart")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<var declaration list part>");
    result_ = componentName(builder_, level_ + 1);
    result_ = result_ && varDeclarationListPart_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, VAR_DECLARATION_LIST_PART, result_, false, null);
    return result_;
  }

  // varInit?
  private static boolean varDeclarationListPart_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varDeclarationListPart_1")) return false;
    varInit(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // varDeclarationList ';'
  static boolean varDeclarationListWithSemicolon(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varDeclarationListWithSemicolon")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = varDeclarationList(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // '=' type ['.' referenceExpression]
  static boolean varFactoryDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varFactoryDeclaration")) return false;
    if (!nextTokenIs(builder_, EQ)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, EQ);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, type(builder_, level_ + 1));
    result_ = pinned_ && varFactoryDeclaration_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ['.' referenceExpression]
  private static boolean varFactoryDeclaration_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varFactoryDeclaration_2")) return false;
    varFactoryDeclaration_2_0(builder_, level_ + 1);
    return true;
  }

  // '.' referenceExpression
  private static boolean varFactoryDeclaration_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varFactoryDeclaration_2_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, DOT);
    pinned_ = result_; // pin = 1
    result_ = result_ && referenceExpression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // '=' expression
  public static boolean varInit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "varInit")) return false;
    if (!nextTokenIs(builder_, EQ)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, EQ);
    pinned_ = result_; // pin = 1
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, VAR_INIT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // 'while' '(' expressionWithRecoverUntilParen ')' statement
  public static boolean whileStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "whileStatement")) return false;
    if (!nextTokenIs(builder_, WHILE)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, WHILE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, LPAREN));
    result_ = pinned_ && report_error_(builder_, expressionWithRecoverUntilParen(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RPAREN)) && result_;
    result_ = pinned_ && statement(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, WHILE_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  final static Parser argument_list_part_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return argument_list_part_recover(builder_, level_ + 1);
    }
  };
  final static Parser argument_list_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return argument_list_recover(builder_, level_ + 1);
    }
  };
  final static Parser class_member_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return class_member_recover(builder_, level_ + 1);
    }
  };
  final static Parser default_formal_parameter_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return default_formal_parameter_recover(builder_, level_ + 1);
    }
  };
  final static Parser expression_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return expression_recover(builder_, level_ + 1);
    }
  };
  final static Parser for_loops_parts_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return for_loops_parts_recover(builder_, level_ + 1);
    }
  };
  final static Parser map_literal_entry_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return map_literal_entry_recover(builder_, level_ + 1);
    }
  };
  final static Parser not_paren_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return not_paren_recover(builder_, level_ + 1);
    }
  };
  final static Parser parenthesesRecovery_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return parenthesesRecovery(builder_, level_ + 1);
    }
  };
  final static Parser semicolon_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return semicolon_recover(builder_, level_ + 1);
    }
  };
  final static Parser simple_scope_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return simple_scope_recover(builder_, level_ + 1);
    }
  };
  final static Parser statement_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return statement_recover(builder_, level_ + 1);
    }
  };
  final static Parser super_call_or_field_initializer_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return super_call_or_field_initializer_recover(builder_, level_ + 1);
    }
  };
  final static Parser switch_case_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return switch_case_recover(builder_, level_ + 1);
    }
  };
  final static Parser top_level_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return top_level_recover(builder_, level_ + 1);
    }
  };
  final static Parser type_parameter_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return type_parameter_recover(builder_, level_ + 1);
    }
  };
}
