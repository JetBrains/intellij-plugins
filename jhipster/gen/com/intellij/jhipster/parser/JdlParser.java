// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.jhipster.psi.JdlTokenTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class JdlParser implements PsiParser, LightPsiParser {

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
    create_token_set_(ARRAY_LITERAL, BOOLEAN_LITERAL, ID, NUMBER_LITERAL,
      REGEX_LITERAL, STRING_LITERAL, VALUE),
  };

  /* ********************************************************** */
  // STRUDEL annotationId (LPARENTH annotationValue RPARENTH)?
  public static boolean annotation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation")) return false;
    if (!nextTokenIs(b, STRUDEL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION, null);
    r = consumeToken(b, STRUDEL);
    p = r; // pin = 1
    r = r && report_error_(b, annotationId(b, l + 1));
    r = p && annotation_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LPARENTH annotationValue RPARENTH)?
  private static boolean annotation_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation_2")) return false;
    annotation_2_0(b, l + 1);
    return true;
  }

  // LPARENTH annotationValue RPARENTH
  private static boolean annotation_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPARENTH);
    r = r && annotationValue(b, l + 1);
    r = r && consumeToken(b, RPARENTH);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean annotationId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotationId")) return false;
    if (!nextTokenIs(b, "<annotation identifier>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_ID, "<annotation identifier>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // withOptionValue | stringLiteral | numberLiteral
  public static boolean annotationValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotationValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ANNOTATION_VALUE, "<annotation value>");
    r = withOptionValue(b, l + 1);
    if (!r) r = stringLiteral(b, l + 1);
    if (!r) r = numberLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (NEWLINE | annotation)*
  static boolean annotations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotations")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotations_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "annotations", c)) break;
    }
    return true;
  }

  // NEWLINE | annotation
  private static boolean annotations_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotations_0")) return false;
    boolean r;
    r = consumeToken(b, NEWLINE);
    if (!r) r = annotation(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // APPLICATION_KEYWORD NEWLINE* LBRACE NEWLINE* applicationContent NEWLINE* RBRACE
  public static boolean application(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "application")) return false;
    if (!nextTokenIs(b, APPLICATION_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, APPLICATION, null);
    r = consumeToken(b, APPLICATION_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, application_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, LBRACE)) && r;
    r = p && report_error_(b, application_3(b, l + 1)) && r;
    r = p && report_error_(b, applicationContent(b, l + 1)) && r;
    r = p && report_error_(b, application_5(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NEWLINE*
  private static boolean application_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "application_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "application_1", c)) break;
    }
    return true;
  }

  // NEWLINE*
  private static boolean application_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "application_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "application_3", c)) break;
    }
    return true;
  }

  // NEWLINE*
  private static boolean application_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "application_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "application_5", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (NEWLINE | configBlock | useConfigurationOption | configurationOption)*
  static boolean applicationContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "applicationContent")) return false;
    while (true) {
      int c = current_position_(b);
      if (!applicationContent_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "applicationContent", c)) break;
    }
    return true;
  }

  // NEWLINE | configBlock | useConfigurationOption | configurationOption
  private static boolean applicationContent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "applicationContent_0")) return false;
    boolean r;
    r = consumeToken(b, NEWLINE);
    if (!r) r = configBlock(b, l + 1);
    if (!r) r = useConfigurationOption(b, l + 1);
    if (!r) r = configurationOption(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // value (COMMA|&RBRACKET)
  static boolean arrayElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayElement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = value(b, l + 1);
    p = r; // pin = 1
    r = r && arrayElement_1(b, l + 1);
    exit_section_(b, l, m, r, p, JdlParser::notBracketOrNextValue);
    return r || p;
  }

  // COMMA|&RBRACKET
  private static boolean arrayElement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayElement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = arrayElement_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &RBRACKET
  private static boolean arrayElement_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayElement_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, RBRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LBRACKET (arrayElement | NEWLINE)* RBRACKET
  public static boolean arrayLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayLiteral")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ARRAY_LITERAL, null);
    r = consumeToken(b, LBRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, arrayLiteral_1(b, l + 1));
    r = p && consumeToken(b, RBRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (arrayElement | NEWLINE)*
  private static boolean arrayLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayLiteral_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!arrayLiteral_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arrayLiteral_1", c)) break;
    }
    return true;
  }

  // arrayElement | NEWLINE
  private static boolean arrayLiteral_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayLiteral_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arrayElement(b, l + 1);
    if (!r) r = consumeToken(b, NEWLINE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TRUE | FALSE
  public static boolean booleanLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "booleanLiteral")) return false;
    if (!nextTokenIs(b, "<boolean literal>", FALSE, TRUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BOOLEAN_LITERAL, "<boolean literal>");
    r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // configKeyword NEWLINE* LBRACE (NEWLINE | configElement)* RBRACE
  public static boolean configBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configBlock")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONFIG_BLOCK, "<config block>");
    r = configKeyword(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, configBlock_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, LBRACE)) && r;
    r = p && report_error_(b, configBlock_3(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NEWLINE*
  private static boolean configBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "configBlock_1", c)) break;
    }
    return true;
  }

  // (NEWLINE | configElement)*
  private static boolean configBlock_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configBlock_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!configBlock_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "configBlock_3", c)) break;
    }
    return true;
  }

  // NEWLINE | configElement
  private static boolean configBlock_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configBlock_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEWLINE);
    if (!r) r = configElement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // optionNameValue (COMMA|&NEWLINE)
  static boolean configElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configElement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = optionNameValue(b, l + 1);
    p = r; // pin = 1
    r = r && configElement_1(b, l + 1);
    exit_section_(b, l, m, r, p, JdlParser::notRBraceOrNextOption);
    return r || p;
  }

  // COMMA|&NEWLINE
  private static boolean configElement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configElement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = configElement_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &NEWLINE
  private static boolean configElement_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configElement_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, NEWLINE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "config"
  public static boolean configKeyword(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configKeyword")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONFIG_KEYWORD, "<config keyword>");
    r = consumeToken(b, "config");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // configurationOptionName (wildcardLiteral | entitiesList) withOption? exceptEntities?
  public static boolean configurationOption(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configurationOption")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONFIGURATION_OPTION, null);
    r = configurationOptionName(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, configurationOption_1(b, l + 1));
    r = p && report_error_(b, configurationOption_2(b, l + 1)) && r;
    r = p && configurationOption_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // wildcardLiteral | entitiesList
  private static boolean configurationOption_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configurationOption_1")) return false;
    boolean r;
    r = wildcardLiteral(b, l + 1);
    if (!r) r = entitiesList(b, l + 1);
    return r;
  }

  // withOption?
  private static boolean configurationOption_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configurationOption_2")) return false;
    withOption(b, l + 1);
    return true;
  }

  // exceptEntities?
  private static boolean configurationOption_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configurationOption_3")) return false;
    exceptEntities(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean configurationOptionName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configurationOptionName")) return false;
    if (!nextTokenIs(b, "<configuration option>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONFIGURATION_OPTION_NAME, "<configuration option>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // withOptionValue withOptionValueItem*
  public static boolean configurationOptionValues(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configurationOptionValues")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONFIGURATION_OPTION_VALUES, null);
    r = withOptionValue(b, l + 1);
    p = r; // pin = 1
    r = r && configurationOptionValues_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // withOptionValueItem*
  private static boolean configurationOptionValues_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "configurationOptionValues_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!withOptionValueItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "configurationOptionValues_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // constantName ASSIGN value
  public static boolean constant(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constant")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONSTANT, null);
    r = constantName(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 2
    r = r && value(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean constantName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantName")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, CONSTANT_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // DEPLOYMENT_KEYWORD NEWLINE* LBRACE (NEWLINE | deploymentElement)* RBRACE
  public static boolean deployment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deployment")) return false;
    if (!nextTokenIs(b, DEPLOYMENT_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DEPLOYMENT, null);
    r = consumeToken(b, DEPLOYMENT_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, deployment_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, LBRACE)) && r;
    r = p && report_error_(b, deployment_3(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NEWLINE*
  private static boolean deployment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deployment_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "deployment_1", c)) break;
    }
    return true;
  }

  // (NEWLINE | deploymentElement)*
  private static boolean deployment_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deployment_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!deployment_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "deployment_3", c)) break;
    }
    return true;
  }

  // NEWLINE | deploymentElement
  private static boolean deployment_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deployment_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEWLINE);
    if (!r) r = deploymentElement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // optionNameValue (COMMA|&NEWLINE)
  static boolean deploymentElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deploymentElement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = optionNameValue(b, l + 1);
    p = r; // pin = 1
    r = r && deploymentElement_1(b, l + 1);
    exit_section_(b, l, m, r, p, JdlParser::notRBraceOrNextOption);
    return r || p;
  }

  // COMMA|&NEWLINE
  private static boolean deploymentElement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deploymentElement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = deploymentElement_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &NEWLINE
  private static boolean deploymentElement_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "deploymentElement_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, NEWLINE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // id entityIdItem*
  public static boolean entitiesList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entitiesList")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENTITIES_LIST, null);
    r = id(b, l + 1);
    p = r; // pin = 1
    r = r && entitiesList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // entityIdItem*
  private static boolean entitiesList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entitiesList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!entityIdItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "entitiesList_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // annotations ENTITY_KEYWORD NEWLINE* entityId NEWLINE* entityTableDeclaration? entityBody?
  public static boolean entity(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entity")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENTITY, "<entity>");
    r = annotations(b, l + 1);
    r = r && consumeToken(b, ENTITY_KEYWORD);
    p = r; // pin = 2
    r = r && report_error_(b, entity_2(b, l + 1));
    r = p && report_error_(b, entityId(b, l + 1)) && r;
    r = p && report_error_(b, entity_4(b, l + 1)) && r;
    r = p && report_error_(b, entity_5(b, l + 1)) && r;
    r = p && entity_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NEWLINE*
  private static boolean entity_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entity_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "entity_2", c)) break;
    }
    return true;
  }

  // NEWLINE*
  private static boolean entity_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entity_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "entity_4", c)) break;
    }
    return true;
  }

  // entityTableDeclaration?
  private static boolean entity_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entity_5")) return false;
    entityTableDeclaration(b, l + 1);
    return true;
  }

  // entityBody?
  private static boolean entity_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entity_6")) return false;
    entityBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // NEWLINE* LBRACE (NEWLINE | entityElement)* RBRACE
  static boolean entityBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityBody")) return false;
    if (!nextTokenIs(b, "", LBRACE, NEWLINE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = entityBody_0(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    p = r; // pin = 2
    r = r && report_error_(b, entityBody_2(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NEWLINE*
  private static boolean entityBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityBody_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "entityBody_0", c)) break;
    }
    return true;
  }

  // (NEWLINE | entityElement)*
  private static boolean entityBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityBody_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!entityBody_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "entityBody_2", c)) break;
    }
    return true;
  }

  // NEWLINE | entityElement
  private static boolean entityBody_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityBody_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEWLINE);
    if (!r) r = entityElement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // entityFieldMapping (COMMA|&NEWLINE)
  static boolean entityElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityElement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = entityFieldMapping(b, l + 1);
    p = r; // pin = 1
    r = r && entityElement_1(b, l + 1);
    exit_section_(b, l, m, r, p, JdlParser::notRBraceOrNextOption);
    return r || p;
  }

  // COMMA|&NEWLINE
  private static boolean entityElement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityElement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = entityElement_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &NEWLINE
  private static boolean entityElement_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityElement_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, NEWLINE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // annotations fieldName fieldType fieldConstraint*
  public static boolean entityFieldMapping(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityFieldMapping")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENTITY_FIELD_MAPPING, "<entity field mapping>");
    r = annotations(b, l + 1);
    r = r && fieldName(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, fieldType(b, l + 1));
    r = p && entityFieldMapping_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // fieldConstraint*
  private static boolean entityFieldMapping_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityFieldMapping_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!fieldConstraint(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "entityFieldMapping_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean entityId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityId")) return false;
    if (!nextTokenIs(b, "<entity identifier>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENTITY_ID, "<entity identifier>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // COMMA id
  static boolean entityIdItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityIdItem")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && id(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LPARENTH NEWLINE* entityTableName NEWLINE* RPARENTH
  static boolean entityTableDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityTableDeclaration")) return false;
    if (!nextTokenIs(b, LPARENTH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LPARENTH);
    p = r; // pin = 1
    r = r && report_error_(b, entityTableDeclaration_1(b, l + 1));
    r = p && report_error_(b, entityTableName(b, l + 1)) && r;
    r = p && report_error_(b, entityTableDeclaration_3(b, l + 1)) && r;
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NEWLINE*
  private static boolean entityTableDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityTableDeclaration_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "entityTableDeclaration_1", c)) break;
    }
    return true;
  }

  // NEWLINE*
  private static boolean entityTableDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityTableDeclaration_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "entityTableDeclaration_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean entityTableName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entityTableName")) return false;
    if (!nextTokenIs(b, "<table name>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENTITY_TABLE_NAME, "<table name>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ENUM_KEYWORD NEWLINE* enumId NEWLINE* LBRACE (NEWLINE | enumElement)* RBRACE
  public static boolean enum_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enum_$")) return false;
    if (!nextTokenIs(b, ENUM_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM, null);
    r = consumeToken(b, ENUM_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, enum_1(b, l + 1));
    r = p && report_error_(b, enumId(b, l + 1)) && r;
    r = p && report_error_(b, enum_3(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, LBRACE)) && r;
    r = p && report_error_(b, enum_5(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NEWLINE*
  private static boolean enum_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enum_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "enum_1", c)) break;
    }
    return true;
  }

  // NEWLINE*
  private static boolean enum_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enum_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "enum_3", c)) break;
    }
    return true;
  }

  // (NEWLINE | enumElement)*
  private static boolean enum_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enum_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!enum_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enum_5", c)) break;
    }
    return true;
  }

  // NEWLINE | enumElement
  private static boolean enum_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enum_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEWLINE);
    if (!r) r = enumElement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // enumValue (COMMA|&NEWLINE)
  static boolean enumElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumElement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = enumValue(b, l + 1);
    p = r; // pin = 1
    r = r && enumElement_1(b, l + 1);
    exit_section_(b, l, m, r, p, JdlParser::notRBraceOrNextOption);
    return r || p;
  }

  // COMMA|&NEWLINE
  private static boolean enumElement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumElement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = enumElement_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &NEWLINE
  private static boolean enumElement_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumElement_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, NEWLINE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean enumId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumId")) return false;
    if (!nextTokenIs(b, "<enum name>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENUM_ID, "<enum name>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean enumKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumKey")) return false;
    if (!nextTokenIs(b, "<enum key>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENUM_KEY, "<enum key>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // enumKey explicitEnumMapping?
  public static boolean enumValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumValue")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_VALUE, null);
    r = enumKey(b, l + 1);
    p = r; // pin = 1
    r = r && enumValue_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // explicitEnumMapping?
  private static boolean enumValue_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumValue_1")) return false;
    explicitEnumMapping(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // EXCEPT_KEYWORD entitiesList
  public static boolean exceptEntities(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exceptEntities")) return false;
    if (!nextTokenIs(b, EXCEPT_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXCEPT_ENTITIES, null);
    r = consumeToken(b, EXCEPT_KEYWORD);
    p = r; // pin = 1
    r = r && entitiesList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LPARENTH (id | stringLiteral) RPARENTH
  public static boolean explicitEnumMapping(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "explicitEnumMapping")) return false;
    if (!nextTokenIs(b, LPARENTH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPLICIT_ENUM_MAPPING, null);
    r = consumeToken(b, LPARENTH);
    p = r; // pin = 1
    r = r && report_error_(b, explicitEnumMapping_1(b, l + 1));
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // id | stringLiteral
  private static boolean explicitEnumMapping_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "explicitEnumMapping_1")) return false;
    boolean r;
    r = id(b, l + 1);
    if (!r) r = stringLiteral(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // fieldConstraintId fieldConstraintParameters?
  public static boolean fieldConstraint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldConstraint")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fieldConstraintId(b, l + 1);
    r = r && fieldConstraint_1(b, l + 1);
    exit_section_(b, m, FIELD_CONSTRAINT, r);
    return r;
  }

  // fieldConstraintParameters?
  private static boolean fieldConstraint_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldConstraint_1")) return false;
    fieldConstraintParameters(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean fieldConstraintId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldConstraintId")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, FIELD_CONSTRAINT_ID, r);
    return r;
  }

  /* ********************************************************** */
  // value (COMMA|&RPARENTH)
  static boolean fieldConstraintParameterValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldConstraintParameterValue")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = value(b, l + 1);
    p = r; // pin = 1
    r = r && fieldConstraintParameterValue_1(b, l + 1);
    exit_section_(b, l, m, r, p, JdlParser::notRParenthOrNextValue);
    return r || p;
  }

  // COMMA|&RPARENTH
  private static boolean fieldConstraintParameterValue_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldConstraintParameterValue_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = fieldConstraintParameterValue_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &RPARENTH
  private static boolean fieldConstraintParameterValue_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldConstraintParameterValue_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, RPARENTH);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LPARENTH fieldConstraintParameterValue* RPARENTH
  public static boolean fieldConstraintParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldConstraintParameters")) return false;
    if (!nextTokenIs(b, LPARENTH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_CONSTRAINT_PARAMETERS, null);
    r = consumeToken(b, LPARENTH);
    p = r; // pin = 1
    r = r && report_error_(b, fieldConstraintParameters_1(b, l + 1));
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // fieldConstraintParameterValue*
  private static boolean fieldConstraintParameters_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldConstraintParameters_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!fieldConstraintParameterValue(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fieldConstraintParameters_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean fieldName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldName")) return false;
    if (!nextTokenIs(b, "<field name>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_NAME, "<field name>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean fieldNameRef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldNameRef")) return false;
    if (!nextTokenIs(b, "<field reference>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_NAME_REF, "<field reference>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean fieldType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldType")) return false;
    if (!nextTokenIs(b, "<field type>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_TYPE, "<field type>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id")) return false;
    if (!nextTokenIs(b, "<identifier>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ID, "<identifier>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(RBRACKET|value)
  static boolean notBracketOrNextValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "notBracketOrNextValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !notBracketOrNextValue_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // RBRACKET|value
  private static boolean notBracketOrNextValue_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "notBracketOrNextValue_0")) return false;
    boolean r;
    r = consumeToken(b, RBRACKET);
    if (!r) r = value(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // !(RBRACE|NEWLINE|IDENTIFIER)
  static boolean notRBraceOrNextOption(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "notRBraceOrNextOption")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !notRBraceOrNextOption_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // RBRACE|NEWLINE|IDENTIFIER
  private static boolean notRBraceOrNextOption_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "notRBraceOrNextOption_0")) return false;
    boolean r;
    r = consumeToken(b, RBRACE);
    if (!r) r = consumeToken(b, NEWLINE);
    if (!r) r = consumeToken(b, IDENTIFIER);
    return r;
  }

  /* ********************************************************** */
  // !(RPARENTH|value)
  static boolean notRParenthOrNextValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "notRParenthOrNextValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !notRParenthOrNextValue_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // RPARENTH|value
  private static boolean notRParenthOrNextValue_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "notRParenthOrNextValue_0")) return false;
    boolean r;
    r = consumeToken(b, RPARENTH);
    if (!r) r = value(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // DOUBLE_NUMBER | INTEGER_NUMBER
  public static boolean numberLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numberLiteral")) return false;
    if (!nextTokenIs(b, "<number literal>", DOUBLE_NUMBER, INTEGER_NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NUMBER_LITERAL, "<number literal>");
    r = consumeToken(b, DOUBLE_NUMBER);
    if (!r) r = consumeToken(b, INTEGER_NUMBER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean optionName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionName")) return false;
    if (!nextTokenIs(b, "<option name>", IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPTION_NAME, "<option name>");
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // optionName value?
  public static boolean optionNameValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionNameValue")) return false;
    if (!nextTokenIs(b, "<option>", IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OPTION_NAME_VALUE, "<option>");
    r = optionName(b, l + 1);
    p = r; // pin = 1
    r = r && optionNameValue_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // value?
  private static boolean optionNameValue_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionNameValue_1")) return false;
    value(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // REGEX_STRING
  public static boolean regexLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "regexLiteral")) return false;
    if (!nextTokenIs(b, "<regex literal>", REGEX_STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REGEX_LITERAL, "<regex literal>");
    r = consumeToken(b, REGEX_STRING);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LBRACE fieldNameRef (LPARENTH fieldNameRef RPARENTH)? fieldConstraint? RBRACE
  public static boolean relationshipDetails(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipDetails")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && fieldNameRef(b, l + 1);
    r = r && relationshipDetails_2(b, l + 1);
    r = r && relationshipDetails_3(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, RELATIONSHIP_DETAILS, r);
    return r;
  }

  // (LPARENTH fieldNameRef RPARENTH)?
  private static boolean relationshipDetails_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipDetails_2")) return false;
    relationshipDetails_2_0(b, l + 1);
    return true;
  }

  // LPARENTH fieldNameRef RPARENTH
  private static boolean relationshipDetails_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipDetails_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPARENTH);
    r = r && fieldNameRef(b, l + 1);
    r = r && consumeToken(b, RPARENTH);
    exit_section_(b, m, null, r);
    return r;
  }

  // fieldConstraint?
  private static boolean relationshipDetails_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipDetails_3")) return false;
    fieldConstraint(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // relationshipMapping (COMMA|&NEWLINE)
  static boolean relationshipElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipElement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = relationshipMapping(b, l + 1);
    p = r; // pin = 1
    r = r && relationshipElement_1(b, l + 1);
    exit_section_(b, l, m, r, p, JdlParser::notRBraceOrNextOption);
    return r || p;
  }

  // COMMA|&NEWLINE
  private static boolean relationshipElement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipElement_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = relationshipElement_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &NEWLINE
  private static boolean relationshipElement_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipElement_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, NEWLINE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // relationshipOption? id relationshipDetails?
  public static boolean relationshipEntity(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipEntity")) return false;
    if (!nextTokenIs(b, "<relationship entity>", IDENTIFIER, STRUDEL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RELATIONSHIP_ENTITY, "<relationship entity>");
    r = relationshipEntity_0(b, l + 1);
    r = r && id(b, l + 1);
    p = r; // pin = 2
    r = r && relationshipEntity_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // relationshipOption?
  private static boolean relationshipEntity_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipEntity_0")) return false;
    relationshipOption(b, l + 1);
    return true;
  }

  // relationshipDetails?
  private static boolean relationshipEntity_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipEntity_2")) return false;
    relationshipDetails(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // RELATIONSHIP_KEYWORD relationshipType NEWLINE* LBRACE (NEWLINE | relationshipElement)* RBRACE
  public static boolean relationshipGroup(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipGroup")) return false;
    if (!nextTokenIs(b, RELATIONSHIP_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RELATIONSHIP_GROUP, null);
    r = consumeToken(b, RELATIONSHIP_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, relationshipType(b, l + 1));
    r = p && report_error_(b, relationshipGroup_2(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, LBRACE)) && r;
    r = p && report_error_(b, relationshipGroup_4(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NEWLINE*
  private static boolean relationshipGroup_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipGroup_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "relationshipGroup_2", c)) break;
    }
    return true;
  }

  // (NEWLINE | relationshipElement)*
  private static boolean relationshipGroup_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipGroup_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!relationshipGroup_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "relationshipGroup_4", c)) break;
    }
    return true;
  }

  // NEWLINE | relationshipElement
  private static boolean relationshipGroup_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipGroup_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEWLINE);
    if (!r) r = relationshipElement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // relationshipEntity NEWLINE* TO_KEYWORD NEWLINE* relationshipEntity withOption?
  public static boolean relationshipMapping(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipMapping")) return false;
    if (!nextTokenIs(b, "<relationship mapping>", IDENTIFIER, STRUDEL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RELATIONSHIP_MAPPING, "<relationship mapping>");
    r = relationshipEntity(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, relationshipMapping_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, TO_KEYWORD)) && r;
    r = p && report_error_(b, relationshipMapping_3(b, l + 1)) && r;
    r = p && report_error_(b, relationshipEntity(b, l + 1)) && r;
    r = p && relationshipMapping_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NEWLINE*
  private static boolean relationshipMapping_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipMapping_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "relationshipMapping_1", c)) break;
    }
    return true;
  }

  // NEWLINE*
  private static boolean relationshipMapping_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipMapping_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEWLINE)) break;
      if (!empty_element_parsed_guard_(b, "relationshipMapping_3", c)) break;
    }
    return true;
  }

  // withOption?
  private static boolean relationshipMapping_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipMapping_5")) return false;
    withOption(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // STRUDEL relationshipOptionId (LPARENTH stringLiteral RPARENTH)?
  public static boolean relationshipOption(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipOption")) return false;
    if (!nextTokenIs(b, STRUDEL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RELATIONSHIP_OPTION, null);
    r = consumeToken(b, STRUDEL);
    p = r; // pin = 1
    r = r && report_error_(b, relationshipOptionId(b, l + 1));
    r = p && relationshipOption_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (LPARENTH stringLiteral RPARENTH)?
  private static boolean relationshipOption_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipOption_2")) return false;
    relationshipOption_2_0(b, l + 1);
    return true;
  }

  // LPARENTH stringLiteral RPARENTH
  private static boolean relationshipOption_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipOption_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPARENTH);
    r = r && stringLiteral(b, l + 1);
    r = r && consumeToken(b, RPARENTH);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean relationshipOptionId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipOptionId")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, RELATIONSHIP_OPTION_ID, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean relationshipType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationshipType")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, RELATIONSHIP_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // (NEWLINE | application | entity | enum | relationshipGroup
  //           | deployment | constant | useConfigurationOption | configurationOption)*
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    while (true) {
      int c = current_position_(b);
      if (!root_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root", c)) break;
    }
    return true;
  }

  // NEWLINE | application | entity | enum | relationshipGroup
  //           | deployment | constant | useConfigurationOption | configurationOption
  private static boolean root_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_0")) return false;
    boolean r;
    r = consumeToken(b, NEWLINE);
    if (!r) r = application(b, l + 1);
    if (!r) r = entity(b, l + 1);
    if (!r) r = enum_$(b, l + 1);
    if (!r) r = relationshipGroup(b, l + 1);
    if (!r) r = deployment(b, l + 1);
    if (!r) r = constant(b, l + 1);
    if (!r) r = useConfigurationOption(b, l + 1);
    if (!r) r = configurationOption(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // DOUBLE_QUOTED_STRING
  public static boolean stringLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringLiteral")) return false;
    if (!nextTokenIs(b, DOUBLE_QUOTED_STRING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOUBLE_QUOTED_STRING);
    exit_section_(b, m, STRING_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // USE_KEYWORD configurationOptionValues FOR_KEYWORD (wildcardLiteral | entitiesList)
  public static boolean useConfigurationOption(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "useConfigurationOption")) return false;
    if (!nextTokenIs(b, USE_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USE_CONFIGURATION_OPTION, null);
    r = consumeToken(b, USE_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, configurationOptionValues(b, l + 1));
    r = p && report_error_(b, consumeToken(b, FOR_KEYWORD)) && r;
    r = p && useConfigurationOption_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // wildcardLiteral | entitiesList
  private static boolean useConfigurationOption_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "useConfigurationOption_3")) return false;
    boolean r;
    r = wildcardLiteral(b, l + 1);
    if (!r) r = entitiesList(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // id | booleanLiteral | stringLiteral | numberLiteral | arrayLiteral | regexLiteral
  public static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VALUE, "<value>");
    r = id(b, l + 1);
    if (!r) r = booleanLiteral(b, l + 1);
    if (!r) r = stringLiteral(b, l + 1);
    if (!r) r = numberLiteral(b, l + 1);
    if (!r) r = arrayLiteral(b, l + 1);
    if (!r) r = regexLiteral(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "all" | WILDCARD
  public static boolean wildcardLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "wildcardLiteral")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, WILDCARD_LITERAL, "<*>");
    r = consumeToken(b, "all");
    if (!r) r = consumeToken(b, WILDCARD);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // WITH_KEYWORD withOptionValue
  static boolean withOption(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withOption")) return false;
    if (!nextTokenIs(b, WITH_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, WITH_KEYWORD);
    p = r; // pin = 1
    r = r && withOptionValue(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean withOptionValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withOptionValue")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, WITH_OPTION_VALUE, r);
    return r;
  }

  /* ********************************************************** */
  // COMMA withOptionValue
  static boolean withOptionValueItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withOptionValueItem")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && withOptionValue(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

}
