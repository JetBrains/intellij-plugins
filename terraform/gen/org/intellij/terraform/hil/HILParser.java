// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.intellij.terraform.hil.HILElementTypes.*;
import static org.intellij.terraform.hil.psi.HILParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static org.intellij.terraform.template.lexer.TerraformTemplateTokenTypes.DATA_LANGUAGE_TOKEN_UNPARSED;;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class HILParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return root(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(FOR_VARIABLE, IL_BINARY_ADDITION_EXPRESSION, IL_BINARY_AND_EXPRESSION, IL_BINARY_EQUALITY_EXPRESSION,
      IL_BINARY_MULTIPLY_EXPRESSION, IL_BINARY_OR_EXPRESSION, IL_BINARY_RELATIONAL_EXPRESSION, IL_COLLECTION_VALUE,
      IL_CONDITIONAL_EXPRESSION, IL_EXPRESSION, IL_EXPRESSION_HOLDER, IL_INDEX_SELECT_EXPRESSION,
      IL_LITERAL_EXPRESSION, IL_METHOD_CALL_EXPRESSION, IL_PARENTHESIZED_EXPRESSION, IL_SELECT_EXPRESSION,
      IL_SIMPLE_EXPRESSION, IL_TEMPLATE_FOR_BLOCK_EXPRESSION, IL_TEMPLATE_IF_BLOCK_EXPRESSION, IL_UNARY_EXPRESSION,
      IL_VARIABLE),
  };

  /* ********************************************************** */
  // OP_PLUS | OP_MINUS
  static boolean AddOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AddOp")) return false;
    if (!nextTokenIs(b, "<operator>", OP_MINUS, OP_PLUS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<operator>");
    r = consumeToken(b, OP_PLUS);
    if (!r) r = consumeToken(b, OP_MINUS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TEMPLATE_START ELSE_KEYWORD '}' ILTemplateBlock?
  public static boolean ElseBranch(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ElseBranch")) return false;
    if (!nextTokenIs(b, TEMPLATE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TEMPLATE_START, ELSE_KEYWORD, R_CURLY);
    r = r && ElseBranch_3(b, l + 1);
    exit_section_(b, m, ELSE_BRANCH, r);
    return r;
  }

  // ILTemplateBlock?
  private static boolean ElseBranch_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ElseBranch_3")) return false;
    ILTemplateBlock(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // TEMPLATE_START ENDFOR_KEYWORD '}'
  public static boolean EndFor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EndFor")) return false;
    if (!nextTokenIs(b, TEMPLATE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TEMPLATE_START, ENDFOR_KEYWORD, R_CURLY);
    exit_section_(b, m, END_FOR, r);
    return r;
  }

  /* ********************************************************** */
  // TEMPLATE_START ENDIF_KEYWORD '}'
  public static boolean EndIfBranch(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EndIfBranch")) return false;
    if (!nextTokenIs(b, TEMPLATE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TEMPLATE_START, ENDIF_KEYWORD, R_CURLY);
    exit_section_(b, m, END_IF_BRANCH, r);
    return r;
  }

  /* ********************************************************** */
  // TEMPLATE_START FOR_KEYWORD ForVariable ("," ForVariable)* IN_KEYWORD ILSimpleExpression '}' ILTemplateBlock?
  public static boolean ForLoop(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForLoop")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOR_LOOP, "<for loop>");
    r = consumeTokens(b, 2, TEMPLATE_START, FOR_KEYWORD);
    p = r; // pin = 2
    r = r && report_error_(b, ForVariable(b, l + 1));
    r = p && report_error_(b, ForLoop_3(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, IN_KEYWORD)) && r;
    r = p && report_error_(b, ILSimpleExpression(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, R_CURLY)) && r;
    r = p && ForLoop_7(b, l + 1) && r;
    exit_section_(b, l, m, r, p, HILParser::not_control_structure_symbol);
    return r || p;
  }

  // ("," ForVariable)*
  private static boolean ForLoop_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForLoop_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ForLoop_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ForLoop_3", c)) break;
    }
    return true;
  }

  // "," ForVariable
  private static boolean ForLoop_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForLoop_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ForVariable(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ILTemplateBlock?
  private static boolean ForLoop_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForLoop_7")) return false;
    ILTemplateBlock(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ID
  public static boolean ForVariable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForVariable")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ID);
    exit_section_(b, m, FOR_VARIABLE, r);
    return r;
  }

  /* ********************************************************** */
  // '[' array_element* ']'
  public static boolean ILArray(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILArray")) return false;
    if (!nextTokenIs(b, L_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IL_ARRAY, null);
    r = consumeToken(b, L_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, ILArray_1(b, l + 1));
    r = p && consumeToken(b, R_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // array_element*
  private static boolean ILArray_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILArray_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!array_element(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ILArray_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '{' object_element2* '}'
  public static boolean ILObject(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILObject")) return false;
    if (!nextTokenIs(b, L_CURLY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IL_OBJECT, null);
    r = consumeToken(b, L_CURLY);
    p = r; // pin = 1
    r = r && report_error_(b, ILObject_1(b, l + 1));
    r = p && consumeToken(b, R_CURLY) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // object_element2*
  private static boolean ILObject_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILObject_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!object_element2(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ILObject_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '(' ILParameterListElement* '...'? ')'
  public static boolean ILParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILParameterList")) return false;
    if (!nextTokenIs(b, L_PAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IL_PARAMETER_LIST, null);
    r = consumeToken(b, L_PAREN);
    p = r; // pin = 1
    r = r && report_error_(b, ILParameterList_1(b, l + 1));
    r = p && report_error_(b, ILParameterList_2(b, l + 1)) && r;
    r = p && consumeToken(b, R_PAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ILParameterListElement*
  private static boolean ILParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILParameterList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ILParameterListElement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ILParameterList_1", c)) break;
    }
    return true;
  }

  // '...'?
  private static boolean ILParameterList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILParameterList_2")) return false;
    consumeToken(b, OP_ELLIPSIS);
    return true;
  }

  /* ********************************************************** */
  // (ILExpression) (','|&')')?
  static boolean ILParameterListElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILParameterListElement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = ILParameterListElement_0(b, l + 1);
    p = r; // pin = 1
    r = r && ILParameterListElement_1(b, l + 1);
    exit_section_(b, l, m, r, p, HILParser::not_paren_or_next_value);
    return r || p;
  }

  // (ILExpression)
  private static boolean ILParameterListElement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILParameterListElement_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ILExpression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (','|&')')?
  private static boolean ILParameterListElement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILParameterListElement_1")) return false;
    ILParameterListElement_1_0(b, l + 1);
    return true;
  }

  // ','|&')'
  private static boolean ILParameterListElement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILParameterListElement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = ILParameterListElement_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &')'
  private static boolean ILParameterListElement_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILParameterListElement_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, R_PAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ('='|':') ILExpression
  public static boolean ILProperty(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILProperty")) return false;
    if (!nextTokenIs(b, "<il property>", EQUALS, OP_COLON)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _LEFT_, IL_PROPERTY, "<il property>");
    r = ILProperty_0(b, l + 1);
    p = r; // pin = 1
    r = r && ILExpression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // '='|':'
  private static boolean ILProperty_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILProperty_0")) return false;
    boolean r;
    r = consumeToken(b, EQUALS);
    if (!r) r = consumeToken(b, OP_COLON);
    return r;
  }

  /* ********************************************************** */
  // ILParenthesizedExpression
  //   | ILConditionalExpression
  //   | ILBinaryOrExpression
  //   | ILBinaryAndExpression
  //   | ILBinaryEqualityExpression
  //   | ILBinaryRelationalExpression
  //   | ILBinaryAdditionExpression
  //   | ILBinaryMultiplyExpression
  //   | ILMethodCallExpression
  //   | ILUnaryExpression
  //   | ILSelectExpression
  //   | ILIndexSelectExpression
  //   | ILCollectionValue
  //   | ILVariable
  //   | ILLiteralExpression
  public static boolean ILSimpleExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILSimpleExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, IL_SIMPLE_EXPRESSION, "<expression>");
    r = ILParenthesizedExpression(b, l + 1);
    if (!r) r = ILExpression(b, l + 1, 1);
    if (!r) r = ILExpression(b, l + 1, 2);
    if (!r) r = ILExpression(b, l + 1, 3);
    if (!r) r = ILExpression(b, l + 1, 4);
    if (!r) r = ILExpression(b, l + 1, 5);
    if (!r) r = ILExpression(b, l + 1, 6);
    if (!r) r = ILExpression(b, l + 1, 7);
    if (!r) r = ILExpression(b, l + 1, 8);
    if (!r) r = ILUnaryExpression(b, l + 1);
    if (!r) r = ILExpression(b, l + 1, 10);
    if (!r) r = ILExpression(b, l + 1, 11);
    if (!r) r = ILCollectionValue(b, l + 1);
    if (!r) r = ILVariable(b, l + 1);
    if (!r) r = ILLiteralExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ((shouldParseAsTemplate data_language_segment | ILExpression) | ILTemplateHolder | ILExpressionHolder)+
  public static boolean ILTemplateBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILTemplateBlock")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IL_TEMPLATE_BLOCK, "<il template block>");
    r = ILTemplateBlock_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!ILTemplateBlock_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ILTemplateBlock", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (shouldParseAsTemplate data_language_segment | ILExpression) | ILTemplateHolder | ILExpressionHolder
  private static boolean ILTemplateBlock_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILTemplateBlock_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ILTemplateBlock_0_0(b, l + 1);
    if (!r) r = ILTemplateHolder(b, l + 1);
    if (!r) r = ILExpressionHolder(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // shouldParseAsTemplate data_language_segment | ILExpression
  private static boolean ILTemplateBlock_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILTemplateBlock_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ILTemplateBlock_0_0_0(b, l + 1);
    if (!r) r = ILExpression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // shouldParseAsTemplate data_language_segment
  private static boolean ILTemplateBlock_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILTemplateBlock_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isTemplatingSupported(b, l + 1);
    r = r && parseDataLanguageToken(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ForLoop EndFor
  public static boolean ILTemplateForBlockExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILTemplateForBlockExpression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IL_TEMPLATE_FOR_BLOCK_EXPRESSION, "<il template for block expression>");
    r = ForLoop(b, l + 1);
    p = r; // pin = 1
    r = r && EndFor(b, l + 1);
    exit_section_(b, l, m, r, p, HILParser::not_another_complete_control_structure);
    return r || p;
  }

  /* ********************************************************** */
  // ILTemplateForBlockExpression | ILTemplateIfBlockExpression
  public static boolean ILTemplateHolder(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILTemplateHolder")) return false;
    if (!nextTokenIs(b, TEMPLATE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ILTemplateForBlockExpression(b, l + 1);
    if (!r) r = ILTemplateIfBlockExpression(b, l + 1);
    exit_section_(b, m, IL_TEMPLATE_HOLDER, r);
    return r;
  }

  /* ********************************************************** */
  // IfBranch ElseBranch? EndIfBranch
  public static boolean ILTemplateIfBlockExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILTemplateIfBlockExpression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IL_TEMPLATE_IF_BLOCK_EXPRESSION, "<il template if block expression>");
    r = IfBranch(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, ILTemplateIfBlockExpression_1(b, l + 1));
    r = p && EndIfBranch(b, l + 1) && r;
    exit_section_(b, l, m, r, p, HILParser::not_another_complete_control_structure);
    return r || p;
  }

  // ElseBranch?
  private static boolean ILTemplateIfBlockExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILTemplateIfBlockExpression_1")) return false;
    ElseBranch(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // TEMPLATE_START IF_KEYWORD (ILSimpleExpression) '}' ILTemplateBlock?
  public static boolean IfBranch(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfBranch")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_BRANCH, "<if branch>");
    r = consumeTokens(b, 2, TEMPLATE_START, IF_KEYWORD);
    p = r; // pin = 2
    r = r && report_error_(b, IfBranch_2(b, l + 1));
    r = p && report_error_(b, consumeToken(b, R_CURLY)) && r;
    r = p && IfBranch_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, HILParser::not_control_structure_symbol);
    return r || p;
  }

  // (ILSimpleExpression)
  private static boolean IfBranch_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfBranch_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ILSimpleExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ILTemplateBlock?
  private static boolean IfBranch_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfBranch_4")) return false;
    ILTemplateBlock(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // OP_MUL | OP_DIV | OP_MOD
  static boolean MulOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "MulOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<operator>");
    r = consumeToken(b, OP_MUL);
    if (!r) r = consumeToken(b, OP_DIV);
    if (!r) r = consumeToken(b, OP_MOD);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ILExpression ILProperty (','|&'}')?
  static boolean ObjectElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectElement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = ILExpression(b, l + 1, -1);
    r = r && ILProperty(b, l + 1);
    p = r; // pin = 2
    r = r && ObjectElement_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (','|&'}')?
  private static boolean ObjectElement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectElement_2")) return false;
    ObjectElement_2_0(b, l + 1);
    return true;
  }

  // ','|&'}'
  private static boolean ObjectElement_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectElement_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = ObjectElement_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &'}'
  private static boolean ObjectElement_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectElement_2_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, R_CURLY);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AddOp | OP_NOT
  static boolean UnaryOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnaryOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<operator>");
    r = AddOp(b, l + 1);
    if (!r) r = consumeToken(b, OP_NOT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OP_AND_AND
  static boolean andOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "andOp")) return false;
    if (!nextTokenIs(b, "<operator>", OP_AND_AND)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<operator>");
    r = consumeToken(b, OP_AND_AND);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (ILExpression) (','|&']')
  static boolean array_element(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_element")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = array_element_0(b, l + 1);
    p = r; // pin = 1
    r = r && array_element_1(b, l + 1);
    exit_section_(b, l, m, r, p, HILParser::not_bracket_or_next_value);
    return r || p;
  }

  // (ILExpression)
  private static boolean array_element_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_element_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ILExpression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','|&']'
  private static boolean array_element_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_element_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = array_element_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &']'
  private static boolean array_element_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_element_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, R_BRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OP_EQUAL
  //                     | OP_NOT_EQUAL
  static boolean equalityOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityOp")) return false;
    if (!nextTokenIs(b, "<operator>", OP_EQUAL, OP_NOT_EQUAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<operator>");
    r = consumeToken(b, OP_EQUAL);
    if (!r) r = consumeToken(b, OP_NOT_EQUAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ID
  static boolean identifier(PsiBuilder b, int l) {
    return consumeToken(b, ID);
  }

  /* ********************************************************** */
  // DOUBLE_QUOTED_STRING
  static boolean literal(PsiBuilder b, int l) {
    return consumeToken(b, DOUBLE_QUOTED_STRING);
  }

  /* ********************************************************** */
  // !(ForLoop|EndFor|IfBranch|ElseBranch|EndIfBranch|isDataLanguageSegment)
  static boolean not_another_complete_control_structure(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_another_complete_control_structure")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !not_another_complete_control_structure_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ForLoop|EndFor|IfBranch|ElseBranch|EndIfBranch|isDataLanguageSegment
  private static boolean not_another_complete_control_structure_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_another_complete_control_structure_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ForLoop(b, l + 1);
    if (!r) r = EndFor(b, l + 1);
    if (!r) r = IfBranch(b, l + 1);
    if (!r) r = ElseBranch(b, l + 1);
    if (!r) r = EndIfBranch(b, l + 1);
    if (!r) r = isDataLanguageToken(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !('}'|ObjectElement)
  static boolean not_brace_or_next_value_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_brace_or_next_value_2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !not_brace_or_next_value_2_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '}'|ObjectElement
  private static boolean not_brace_or_next_value_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_brace_or_next_value_2_0")) return false;
    boolean r;
    r = consumeToken(b, R_CURLY);
    if (!r) r = ObjectElement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // !(']'|ILExpression)
  static boolean not_bracket_or_next_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_bracket_or_next_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !not_bracket_or_next_value_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ']'|ILExpression
  private static boolean not_bracket_or_next_value_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_bracket_or_next_value_0")) return false;
    boolean r;
    r = consumeToken(b, R_BRACKET);
    if (!r) r = ILExpression(b, l + 1, -1);
    return r;
  }

  /* ********************************************************** */
  // !('}'|'{'|'${'|'%{'|isDataLanguageSegment)
  static boolean not_control_structure_symbol(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_control_structure_symbol")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !not_control_structure_symbol_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '}'|'{'|'${'|'%{'|isDataLanguageSegment
  private static boolean not_control_structure_symbol_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_control_structure_symbol_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, R_CURLY);
    if (!r) r = consumeToken(b, L_CURLY);
    if (!r) r = consumeToken(b, INTERPOLATION_START);
    if (!r) r = consumeToken(b, TEMPLATE_START);
    if (!r) r = isDataLanguageToken(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(')'|'...'|ILExpression)
  static boolean not_paren_or_next_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_paren_or_next_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !not_paren_or_next_value_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ')'|'...'|ILExpression
  private static boolean not_paren_or_next_value_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_paren_or_next_value_0")) return false;
    boolean r;
    r = consumeToken(b, R_PAREN);
    if (!r) r = consumeToken(b, OP_ELLIPSIS);
    if (!r) r = ILExpression(b, l + 1, -1);
    return r;
  }

  /* ********************************************************** */
  // NUMBER
  static boolean number(PsiBuilder b, int l) {
    return consumeToken(b, NUMBER);
  }

  /* ********************************************************** */
  // (ObjectElement) (','|&'}')?
  static boolean object_element2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element2")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = object_element2_0(b, l + 1);
    p = r; // pin = 1
    r = r && object_element2_1(b, l + 1);
    exit_section_(b, l, m, r, p, HILParser::not_brace_or_next_value_2);
    return r || p;
  }

  // (ObjectElement)
  private static boolean object_element2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ObjectElement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (','|&'}')?
  private static boolean object_element2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element2_1")) return false;
    object_element2_1_0(b, l + 1);
    return true;
  }

  // ','|&'}'
  private static boolean object_element2_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element2_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = object_element2_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &'}'
  private static boolean object_element2_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element2_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, R_CURLY);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OP_OR_OR
  static boolean orOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "orOp")) return false;
    if (!nextTokenIs(b, "<operator>", OP_OR_OR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<operator>");
    r = consumeToken(b, OP_OR_OR);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OP_LESS
  //                        | OP_GREATER
  //                        | OP_LESS_OR_EQUAL
  //                        | OP_GREATER_OR_EQUAL
  static boolean relationalOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<operator>");
    r = consumeToken(b, OP_LESS);
    if (!r) r = consumeToken(b, OP_GREATER);
    if (!r) r = consumeToken(b, OP_LESS_OR_EQUAL);
    if (!r) r = consumeToken(b, OP_GREATER_OR_EQUAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // shouldParseAsTemplate (data_language_segment | template_segment)* | ILTemplateHolder | ILExpressionHolder
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_0(b, l + 1);
    if (!r) r = ILTemplateHolder(b, l + 1);
    if (!r) r = ILExpressionHolder(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // shouldParseAsTemplate (data_language_segment | template_segment)*
  private static boolean root_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = isTemplatingSupported(b, l + 1);
    r = r && root_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (data_language_segment | template_segment)*
  private static boolean root_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!root_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root_0_1", c)) break;
    }
    return true;
  }

  // data_language_segment | template_segment
  private static boolean root_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseDataLanguageToken(b, l + 1);
    if (!r) r = template_segment(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ILTemplateHolder | ILExpressionHolder
  static boolean template_segment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "template_segment")) return false;
    if (!nextTokenIs(b, "", INTERPOLATION_START, TEMPLATE_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ILTemplateHolder(b, l + 1);
    if (!r) r = ILExpressionHolder(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expression root: ILExpression
  // Operator priority table:
  // 0: PREFIX(ILParenthesizedExpression)
  // 1: PREFIX(ILExpressionHolder)
  // 2: POSTFIX(ILConditionalExpression)
  // 3: BINARY(ILBinaryOrExpression)
  // 4: BINARY(ILBinaryAndExpression)
  // 5: BINARY(ILBinaryEqualityExpression)
  // 6: BINARY(ILBinaryRelationalExpression)
  // 7: BINARY(ILBinaryAdditionExpression)
  // 8: BINARY(ILBinaryMultiplyExpression)
  // 9: POSTFIX(ILMethodCallExpression)
  // 10: PREFIX(ILUnaryExpression)
  // 11: BINARY(ILSelectExpression)
  // 12: POSTFIX(ILIndexSelectExpression)
  // 13: ATOM(ILCollectionValue)
  // 14: ATOM(ILVariable)
  // 15: ATOM(ILLiteralExpression)
  public static boolean ILExpression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "ILExpression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = ILParenthesizedExpression(b, l + 1);
    if (!r) r = ILExpressionHolder(b, l + 1);
    if (!r) r = ILUnaryExpression(b, l + 1);
    if (!r) r = ILCollectionValue(b, l + 1);
    if (!r) r = ILVariable(b, l + 1);
    if (!r) r = ILLiteralExpression(b, l + 1);
    p = r;
    r = r && ILExpression_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean ILExpression_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "ILExpression_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 2 && ILConditionalExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, IL_CONDITIONAL_EXPRESSION, r, true, null);
      }
      else if (g < 3 && orOp(b, l + 1)) {
        r = ILExpression(b, l, 3);
        exit_section_(b, l, m, IL_BINARY_OR_EXPRESSION, r, true, null);
      }
      else if (g < 4 && andOp(b, l + 1)) {
        r = ILExpression(b, l, 4);
        exit_section_(b, l, m, IL_BINARY_AND_EXPRESSION, r, true, null);
      }
      else if (g < 5 && equalityOp(b, l + 1)) {
        r = ILExpression(b, l, 5);
        exit_section_(b, l, m, IL_BINARY_EQUALITY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && relationalOp(b, l + 1)) {
        r = ILExpression(b, l, 6);
        exit_section_(b, l, m, IL_BINARY_RELATIONAL_EXPRESSION, r, true, null);
      }
      else if (g < 7 && AddOp(b, l + 1)) {
        r = ILExpression(b, l, 7);
        exit_section_(b, l, m, IL_BINARY_ADDITION_EXPRESSION, r, true, null);
      }
      else if (g < 8 && MulOp(b, l + 1)) {
        r = ILExpression(b, l, 8);
        exit_section_(b, l, m, IL_BINARY_MULTIPLY_EXPRESSION, r, true, null);
      }
      else if (g < 9 && ILParameterList(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, IL_METHOD_CALL_EXPRESSION, r, true, null);
      }
      else if (g < 11 && consumeTokenSmart(b, OP_DOT)) {
        r = ILExpression(b, l, 13);
        exit_section_(b, l, m, IL_SELECT_EXPRESSION, r, true, null);
      }
      else if (g < 12 && ILIndexSelectExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, IL_INDEX_SELECT_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  public static boolean ILParenthesizedExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILParenthesizedExpression")) return false;
    if (!nextTokenIsSmart(b, L_PAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, L_PAREN);
    p = r;
    r = p && ILExpression(b, l, 0);
    r = p && report_error_(b, consumeToken(b, R_PAREN)) && r;
    exit_section_(b, l, m, IL_PARENTHESIZED_EXPRESSION, r, p, null);
    return r || p;
  }

  public static boolean ILExpressionHolder(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILExpressionHolder")) return false;
    if (!nextTokenIsSmart(b, INTERPOLATION_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, INTERPOLATION_START);
    p = r;
    r = p && ILExpression(b, l, 1);
    r = p && report_error_(b, consumeToken(b, R_CURLY)) && r;
    exit_section_(b, l, m, IL_EXPRESSION_HOLDER, r, p, null);
    return r || p;
  }

  // '?' ILExpression (':' ILExpression)
  private static boolean ILConditionalExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILConditionalExpression_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokenSmart(b, OP_QUEST);
    p = r; // pin = '\?'|'\:'
    r = r && report_error_(b, ILExpression(b, l + 1, -1));
    r = p && ILConditionalExpression_0_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ':' ILExpression
  private static boolean ILConditionalExpression_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILConditionalExpression_0_2")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokenSmart(b, OP_COLON);
    p = r; // pin = '\?'|'\:'
    r = r && ILExpression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  public static boolean ILUnaryExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILUnaryExpression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = UnaryOp(b, l + 1);
    p = r;
    r = p && ILExpression(b, l, 10);
    exit_section_(b, l, m, IL_UNARY_EXPRESSION, r, p, null);
    return r || p;
  }

  // '[' ILExpression ']'
  private static boolean ILIndexSelectExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILIndexSelectExpression_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokenSmart(b, L_BRACKET);
    p = r; // pin = '\['
    r = r && report_error_(b, ILExpression(b, l + 1, -1));
    r = p && consumeToken(b, R_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ILArray | ILObject
  public static boolean ILCollectionValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILCollectionValue")) return false;
    if (!nextTokenIsSmart(b, L_BRACKET, L_CURLY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IL_COLLECTION_VALUE, "<il collection value>");
    r = ILArray(b, l + 1);
    if (!r) r = ILObject(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // identifier | '*'
  public static boolean ILVariable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILVariable")) return false;
    if (!nextTokenIsSmart(b, ID, OP_MUL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IL_VARIABLE, "<Identifier>");
    r = identifier(b, l + 1);
    if (!r) r = consumeTokenSmart(b, OP_MUL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // literal /*| identifier*/ | number | 'true' | 'false' | 'null'
  public static boolean ILLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ILLiteralExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IL_LITERAL_EXPRESSION, "<Literal>");
    r = literal(b, l + 1);
    if (!r) r = number(b, l + 1);
    if (!r) r = consumeTokenSmart(b, TRUE);
    if (!r) r = consumeTokenSmart(b, FALSE);
    if (!r) r = consumeTokenSmart(b, NULL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
