// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static name.kropp.intellij.makefile.MakefileParserUtil.*;
import static name.kropp.intellij.makefile.psi.MakefileTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class MakefileParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return makefile(b, l + 1);
  }

  /* ********************************************************** */
  // !<<eof>> stmt
  static boolean any(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "any")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = any_0(b, l + 1);
    p = r; // pin = 1
    r = r && stmt(b, l + 1);
    exit_section_(b, l, m, r, p, recover_parser_);
    return r || p;
  }

  // !<<eof>>
  private static boolean any_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "any_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !eof(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '='|':='|'::='|'?='|'!='|'+='
  static boolean assignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignment")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    if (!r) r = consumeToken(b, ":=");
    if (!r) r = consumeToken(b, "::=");
    if (!r) r = consumeToken(b, "?=");
    if (!r) r = consumeToken(b, "!=");
    if (!r) r = consumeToken(b, "+=");
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // stmt*
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    while (true) {
      int c = current_position_(b);
      if (!stmt(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "block", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // '\t'+ multiline EOL?
  public static boolean command(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command")) return false;
    if (!nextTokenIs(b, TAB)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMMAND, null);
    r = command_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, multiline(b, l + 1));
    r = p && command_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // '\t'+
  private static boolean command_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TAB);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, TAB)) break;
      if (!empty_element_parsed_guard_(b, "command_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // EOL?
  private static boolean command_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_2")) return false;
    consumeToken(b, EOL);
    return true;
  }

  /* ********************************************************** */
  public static boolean comment(PsiBuilder b, int l) {
    Marker m = enter_section_(b);
    exit_section_(b, m, COMMENT, true);
    return true;
  }

  /* ********************************************************** */
  // ('(' (condition-body|',')* ')') | condition-body condition-body?
  public static boolean condition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITION, "<condition>");
    r = condition_0(b, l + 1);
    if (!r) r = condition_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '(' (condition-body|',')* ')'
  private static boolean condition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_PAREN);
    r = r && condition_0_1(b, l + 1);
    r = r && consumeToken(b, CLOSE_PAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (condition-body|',')*
  private static boolean condition_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!condition_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "condition_0_1", c)) break;
    }
    return true;
  }

  // condition-body|','
  private static boolean condition_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = condition_body(b, l + 1);
    if (!r) r = consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  // condition-body condition-body?
  private static boolean condition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = condition_body(b, l + 1);
    r = r && condition_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // condition-body?
  private static boolean condition_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_1_1")) return false;
    condition_body(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // variable-usage|function|string|identifier
  static boolean condition_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "condition_body")) return false;
    boolean r;
    r = variable_usage(b, l + 1);
    if (!r) r = function(b, l + 1);
    if (!r) r = consumeToken(b, STRING);
    if (!r) r = identifier(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // conditional-keyword condition EOL block conditional-else* 'endif'
  public static boolean conditional(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONDITIONAL, "<conditional>");
    r = conditional_keyword(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, condition(b, l + 1));
    r = p && report_error_(b, consumeToken(b, EOL)) && r;
    r = p && report_error_(b, block(b, l + 1)) && r;
    r = p && report_error_(b, conditional_4(b, l + 1)) && r;
    r = p && consumeToken(b, KEYWORD_ENDIF) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // conditional-else*
  private static boolean conditional_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!conditional_else(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "conditional_4", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'else' (conditional-keyword condition block | EOL? block)
  public static boolean conditional_else(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_else")) return false;
    if (!nextTokenIs(b, KEYWORD_ELSE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONDITIONAL_ELSE, null);
    r = consumeToken(b, KEYWORD_ELSE);
    p = r; // pin = 1
    r = r && conditional_else_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // conditional-keyword condition block | EOL? block
  private static boolean conditional_else_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_else_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = conditional_else_1_0(b, l + 1);
    if (!r) r = conditional_else_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // conditional-keyword condition block
  private static boolean conditional_else_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_else_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = conditional_keyword(b, l + 1);
    r = r && condition(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EOL? block
  private static boolean conditional_else_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_else_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = conditional_else_1_1_0(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EOL?
  private static boolean conditional_else_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_else_1_1_0")) return false;
    consumeToken(b, EOL);
    return true;
  }

  /* ********************************************************** */
  // 'ifeq'|'ifneq'|'ifdef'|'ifndef'
  static boolean conditional_keyword(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_keyword")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KEYWORD_IFEQ);
    if (!r) r = consumeToken(b, KEYWORD_IFNEQ);
    if (!r) r = consumeToken(b, KEYWORD_IFDEF);
    if (!r) r = consumeToken(b, KEYWORD_IFNDEF);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'define' variable assignment? EOL (defline EOL)* 'endef'
  public static boolean define(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "define")) return false;
    if (!nextTokenIs(b, KEYWORD_DEFINE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DEFINE, null);
    r = consumeToken(b, KEYWORD_DEFINE);
    p = r; // pin = 1
    r = r && report_error_(b, variable(b, l + 1));
    r = p && report_error_(b, define_2(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, EOL)) && r;
    r = p && report_error_(b, define_4(b, l + 1)) && r;
    r = p && consumeToken(b, KEYWORD_ENDEF) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // assignment?
  private static boolean define_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "define_2")) return false;
    assignment(b, l + 1);
    return true;
  }

  // (defline EOL)*
  private static boolean define_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "define_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!define_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "define_4", c)) break;
    }
    return true;
  }

  // defline EOL
  private static boolean define_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "define_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = defline(b, l + 1);
    r = r && consumeToken(b, EOL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (<<parseLineNotEndef>>|function|variable-usage)*
  static boolean defline(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defline")) return false;
    while (true) {
      int c = current_position_(b);
      if (!defline_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "defline", c)) break;
    }
    return true;
  }

  // <<parseLineNotEndef>>|function|variable-usage
  private static boolean defline_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defline_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseLineNotEndef(b, l + 1);
    if (!r) r = function(b, l + 1);
    if (!r) r = variable_usage(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // define|include|undefine|override|export|privatevar|vpath
  public static boolean directive(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE, "<directive>");
    r = define(b, l + 1);
    if (!r) r = include(b, l + 1);
    if (!r) r = undefine(b, l + 1);
    if (!r) r = override(b, l + 1);
    if (!r) r = export(b, l + 1);
    if (!r) r = privatevar(b, l + 1);
    if (!r) r = vpath(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean directory(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directory")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTORY, "<directory>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  public static boolean doc_comment(PsiBuilder b, int l) {
    Marker m = enter_section_(b);
    exit_section_(b, m, DOC_COMMENT, true);
    return true;
  }

  /* ********************************************************** */
  // 'export' (EOL | (variable-assignment|variable) EOL?)
  public static boolean export(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export")) return false;
    if (!nextTokenIs(b, KEYWORD_EXPORT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KEYWORD_EXPORT);
    r = r && export_1(b, l + 1);
    exit_section_(b, m, EXPORT, r);
    return r;
  }

  // EOL | (variable-assignment|variable) EOL?
  private static boolean export_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EOL);
    if (!r) r = export_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (variable-assignment|variable) EOL?
  private static boolean export_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = export_1_1_0(b, l + 1);
    r = r && export_1_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // variable-assignment|variable
  private static boolean export_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export_1_1_0")) return false;
    boolean r;
    r = variable_assignment(b, l + 1);
    if (!r) r = variable(b, l + 1);
    return r;
  }

  // EOL?
  private static boolean export_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export_1_1_1")) return false;
    consumeToken(b, EOL);
    return true;
  }

  /* ********************************************************** */
  // 'export' variable-assignment
  static boolean exportvar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportvar")) return false;
    if (!nextTokenIs(b, KEYWORD_EXPORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, KEYWORD_EXPORT);
    p = r; // pin = 1
    r = r && variable_assignment(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // identifier
  public static boolean filename(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "filename")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FILENAME, "<filename>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '$' '(' function-name function-param* ')'
  public static boolean function(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function")) return false;
    if (!nextTokenIs(b, DOLLAR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION, null);
    r = consumeTokens(b, 0, DOLLAR, OPEN_PAREN);
    r = r && function_name(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, function_3(b, l + 1));
    r = p && consumeToken(b, CLOSE_PAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // function-param*
  private static boolean function_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!function_param(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "function_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'error'|'warning'|'info'|'shell'|'subst'|'patsubst'|'strip'|'findstring'|
  //                     'filter'|'filter-out'|'sort'|'word'|'wordlist'|'words'|'firstword'|'lastword'|'dir'|'notdir'|'suffix'|
  //                     'basename'|'addsuffix'|'addprefix'|'join'|'wildcard'|'realpath'|'abspath'|'if'|'or'|'and'|
  //                     'foreach'|'file'|'call'|'value'|'eval'|'origin'|'flavor'|'guile'
  public static boolean function_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_NAME, "<function name>");
    r = consumeToken(b, "error");
    if (!r) r = consumeToken(b, "warning");
    if (!r) r = consumeToken(b, "info");
    if (!r) r = consumeToken(b, "shell");
    if (!r) r = consumeToken(b, "subst");
    if (!r) r = consumeToken(b, "patsubst");
    if (!r) r = consumeToken(b, "strip");
    if (!r) r = consumeToken(b, "findstring");
    if (!r) r = consumeToken(b, "filter");
    if (!r) r = consumeToken(b, "filter-out");
    if (!r) r = consumeToken(b, "sort");
    if (!r) r = consumeToken(b, "word");
    if (!r) r = consumeToken(b, "wordlist");
    if (!r) r = consumeToken(b, "words");
    if (!r) r = consumeToken(b, "firstword");
    if (!r) r = consumeToken(b, "lastword");
    if (!r) r = consumeToken(b, "dir");
    if (!r) r = consumeToken(b, "notdir");
    if (!r) r = consumeToken(b, "suffix");
    if (!r) r = consumeToken(b, "basename");
    if (!r) r = consumeToken(b, "addsuffix");
    if (!r) r = consumeToken(b, "addprefix");
    if (!r) r = consumeToken(b, "join");
    if (!r) r = consumeToken(b, "wildcard");
    if (!r) r = consumeToken(b, "realpath");
    if (!r) r = consumeToken(b, "abspath");
    if (!r) r = consumeToken(b, "if");
    if (!r) r = consumeToken(b, "or");
    if (!r) r = consumeToken(b, "and");
    if (!r) r = consumeToken(b, "foreach");
    if (!r) r = consumeToken(b, "file");
    if (!r) r = consumeToken(b, "call");
    if (!r) r = consumeToken(b, "value");
    if (!r) r = consumeToken(b, "eval");
    if (!r) r = consumeToken(b, "origin");
    if (!r) r = consumeToken(b, "flavor");
    if (!r) r = consumeToken(b, "guile");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (string|'$''$'|variable-usage|chars|','|'|'|function-name|function|assignment|':'|';'|split)+
  public static boolean function_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_param")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_PARAM, null);
    r = function_param_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!function_param_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "function_param", c)) break;
    }
    exit_section_(b, l, m, r, false, function_recover_parser_);
    return r;
  }

  // string|'$''$'|variable-usage|chars|','|'|'|function-name|function|assignment|':'|';'|split
  private static boolean function_param_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_param_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRING);
    if (!r) r = parseTokens(b, 0, DOLLAR, DOLLAR);
    if (!r) r = variable_usage(b, l + 1);
    if (!r) r = consumeToken(b, CHARS);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, PIPE);
    if (!r) r = function_name(b, l + 1);
    if (!r) r = function(b, l + 1);
    if (!r) r = assignment(b, l + 1);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, SPLIT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !( ')' | EOL )
  static boolean function_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !function_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ')' | EOL
  private static boolean function_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CLOSE_PAREN);
    if (!r) r = consumeToken(b, EOL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier_part | (<<parseToDollarNoWhitespace>> identifier_part) | <<parseNoWhitespace>>
  public static boolean identifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IDENTIFIER, "<identifier>");
    r = identifier_part(b, l + 1);
    if (!r) r = identifier_1(b, l + 1);
    if (!r) r = parseNoWhitespace(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<parseToDollarNoWhitespace>> identifier_part
  private static boolean identifier_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseToDollarNoWhitespace(b, l + 1);
    r = r && identifier_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ((function | variable-usage) <<parseToDollarNoWhitespaceBehind>> identifier_part) | ((function | variable-usage) <<parseNoWhitespaceBehind>>)
  static boolean identifier_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier_part")) return false;
    if (!nextTokenIs(b, DOLLAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier_part_0(b, l + 1);
    if (!r) r = identifier_part_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (function | variable-usage) <<parseToDollarNoWhitespaceBehind>> identifier_part
  private static boolean identifier_part_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier_part_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier_part_0_0(b, l + 1);
    r = r && parseToDollarNoWhitespaceBehind(b, l + 1);
    r = r && identifier_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // function | variable-usage
  private static boolean identifier_part_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier_part_0_0")) return false;
    boolean r;
    r = function(b, l + 1);
    if (!r) r = variable_usage(b, l + 1);
    return r;
  }

  // (function | variable-usage) <<parseNoWhitespaceBehind>>
  private static boolean identifier_part_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier_part_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier_part_1_0(b, l + 1);
    r = r && parseNoWhitespaceBehind(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // function | variable-usage
  private static boolean identifier_part_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier_part_1_0")) return false;
    boolean r;
    r = function(b, l + 1);
    if (!r) r = variable_usage(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // ('include'|'-include'|'sinclude') filename+ EOL?
  public static boolean include(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INCLUDE, "<include>");
    r = include_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, include_1(b, l + 1));
    r = p && include_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // 'include'|'-include'|'sinclude'
  private static boolean include_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KEYWORD_INCLUDE);
    if (!r) r = consumeToken(b, "-include");
    if (!r) r = consumeToken(b, "sinclude");
    exit_section_(b, m, null, r);
    return r;
  }

  // filename+
  private static boolean include_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = filename(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!filename(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "include_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // EOL?
  private static boolean include_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_2")) return false;
    consumeToken(b, EOL);
    return true;
  }

  /* ********************************************************** */
  // multiline
  public static boolean inline_command(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_command")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INLINE_COMMAND, "<inline command>");
    r = multiline(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (<<parseLine>>|function|variable-usage)*
  static boolean line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line")) return false;
    while (true) {
      int c = current_position_(b);
      if (!line_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "line", c)) break;
    }
    return true;
  }

  // <<parseLine>>|function|variable-usage
  private static boolean line_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseLine(b, l + 1);
    if (!r) r = function(b, l + 1);
    if (!r) r = variable_usage(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // any*
  static boolean makefile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "makefile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!any(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "makefile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // split* line (split+ line)*
  static boolean multiline(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiline")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = multiline_0(b, l + 1);
    r = r && line(b, l + 1);
    r = r && multiline_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // split*
  private static boolean multiline_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiline_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPLIT)) break;
      if (!empty_element_parsed_guard_(b, "multiline_0", c)) break;
    }
    return true;
  }

  // (split+ line)*
  private static boolean multiline_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiline_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!multiline_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "multiline_2", c)) break;
    }
    return true;
  }

  // split+ line
  private static boolean multiline_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiline_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = multiline_2_0_0(b, l + 1);
    r = r && line(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // split+
  private static boolean multiline_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiline_2_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SPLIT);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, SPLIT)) break;
      if (!empty_element_parsed_guard_(b, "multiline_2_0_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // prerequisite* (split prerequisite*)*
  public static boolean normal_prerequisites(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_prerequisites")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NORMAL_PREREQUISITES, "<normal prerequisites>");
    r = normal_prerequisites_0(b, l + 1);
    r = r && normal_prerequisites_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // prerequisite*
  private static boolean normal_prerequisites_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_prerequisites_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!prerequisite(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "normal_prerequisites_0", c)) break;
    }
    return true;
  }

  // (split prerequisite*)*
  private static boolean normal_prerequisites_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_prerequisites_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!normal_prerequisites_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "normal_prerequisites_1", c)) break;
    }
    return true;
  }

  // split prerequisite*
  private static boolean normal_prerequisites_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_prerequisites_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SPLIT);
    r = r && normal_prerequisites_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // prerequisite*
  private static boolean normal_prerequisites_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_prerequisites_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!prerequisite(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "normal_prerequisites_1_0_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // prerequisite* (split prerequisite*)*
  public static boolean order_only_prerequisites(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_only_prerequisites")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ORDER_ONLY_PREREQUISITES, "<order only prerequisites>");
    r = order_only_prerequisites_0(b, l + 1);
    r = r && order_only_prerequisites_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // prerequisite*
  private static boolean order_only_prerequisites_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_only_prerequisites_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!prerequisite(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "order_only_prerequisites_0", c)) break;
    }
    return true;
  }

  // (split prerequisite*)*
  private static boolean order_only_prerequisites_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_only_prerequisites_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!order_only_prerequisites_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "order_only_prerequisites_1", c)) break;
    }
    return true;
  }

  // split prerequisite*
  private static boolean order_only_prerequisites_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_only_prerequisites_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SPLIT);
    r = r && order_only_prerequisites_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // prerequisite*
  private static boolean order_only_prerequisites_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_only_prerequisites_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!prerequisite(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "order_only_prerequisites_1_0_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'override' (undefine|variable-assignment)
  public static boolean override(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "override")) return false;
    if (!nextTokenIs(b, KEYWORD_OVERRIDE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OVERRIDE, null);
    r = consumeToken(b, KEYWORD_OVERRIDE);
    p = r; // pin = 1
    r = r && override_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // undefine|variable-assignment
  private static boolean override_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "override_1")) return false;
    boolean r;
    r = undefine(b, l + 1);
    if (!r) r = variable_assignment(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean pattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATTERN, "<pattern>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // prerequisite_part | (<<parsePrereqToDollarNoWhitespace>> prerequisite_part) | <<parsePrereqNoWhitespace>>
  public static boolean prerequisite(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisite")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PREREQUISITE, "<prerequisite>");
    r = prerequisite_part(b, l + 1);
    if (!r) r = prerequisite_1(b, l + 1);
    if (!r) r = parsePrereqNoWhitespace(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<parsePrereqToDollarNoWhitespace>> prerequisite_part
  private static boolean prerequisite_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisite_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parsePrereqToDollarNoWhitespace(b, l + 1);
    r = r && prerequisite_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ((function | variable-usage) <<parsePrereqToDollarNoWhitespaceBehind>> prerequisite_part) | ((function | variable-usage) <<parsePrereqNoWhitespaceBehind>>)
  static boolean prerequisite_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisite_part")) return false;
    if (!nextTokenIs(b, DOLLAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prerequisite_part_0(b, l + 1);
    if (!r) r = prerequisite_part_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (function | variable-usage) <<parsePrereqToDollarNoWhitespaceBehind>> prerequisite_part
  private static boolean prerequisite_part_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisite_part_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prerequisite_part_0_0(b, l + 1);
    r = r && parsePrereqToDollarNoWhitespaceBehind(b, l + 1);
    r = r && prerequisite_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // function | variable-usage
  private static boolean prerequisite_part_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisite_part_0_0")) return false;
    boolean r;
    r = function(b, l + 1);
    if (!r) r = variable_usage(b, l + 1);
    return r;
  }

  // (function | variable-usage) <<parsePrereqNoWhitespaceBehind>>
  private static boolean prerequisite_part_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisite_part_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prerequisite_part_1_0(b, l + 1);
    r = r && parsePrereqNoWhitespaceBehind(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // function | variable-usage
  private static boolean prerequisite_part_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisite_part_1_0")) return false;
    boolean r;
    r = function(b, l + 1);
    if (!r) r = variable_usage(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // normal_prerequisites ('|' order_only_prerequisites)?
  public static boolean prerequisites(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisites")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PREREQUISITES, "<prerequisites>");
    r = normal_prerequisites(b, l + 1);
    r = r && prerequisites_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('|' order_only_prerequisites)?
  private static boolean prerequisites_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisites_1")) return false;
    prerequisites_1_0(b, l + 1);
    return true;
  }

  // '|' order_only_prerequisites
  private static boolean prerequisites_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisites_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && order_only_prerequisites(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'private' variable-assignment
  public static boolean privatevar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "privatevar")) return false;
    if (!nextTokenIs(b, KEYWORD_PRIVATE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PRIVATEVAR, null);
    r = consumeToken(b, KEYWORD_PRIVATE);
    p = r; // pin = 1
    r = r && variable_assignment(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (command|conditional)+ (command|conditional|EOL)*
  public static boolean recipe(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recipe")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECIPE, "<recipe>");
    r = recipe_0(b, l + 1);
    r = r && recipe_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (command|conditional)+
  private static boolean recipe_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recipe_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recipe_0_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!recipe_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "recipe_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // command|conditional
  private static boolean recipe_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recipe_0_0")) return false;
    boolean r;
    r = command(b, l + 1);
    if (!r) r = conditional(b, l + 1);
    return r;
  }

  // (command|conditional|EOL)*
  private static boolean recipe_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recipe_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!recipe_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "recipe_1", c)) break;
    }
    return true;
  }

  // command|conditional|EOL
  private static boolean recipe_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recipe_1_0")) return false;
    boolean r;
    r = command(b, l + 1);
    if (!r) r = conditional(b, l + 1);
    if (!r) r = consumeToken(b, EOL);
    return r;
  }

  /* ********************************************************** */
  // !(any)
  static boolean recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (any)
  private static boolean recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = any(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // target_line recipe?
  public static boolean rule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rule")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RULE, "<rule>");
    r = target_line(b, l + 1);
    r = r && rule_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // recipe?
  private static boolean rule_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rule_1")) return false;
    recipe(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // directive|conditional|variable-assignment|rule|command|function|macro|EOL
  static boolean stmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stmt")) return false;
    boolean r;
    r = directive(b, l + 1);
    if (!r) r = conditional(b, l + 1);
    if (!r) r = variable_assignment(b, l + 1);
    if (!r) r = rule(b, l + 1);
    if (!r) r = command(b, l + 1);
    if (!r) r = function(b, l + 1);
    if (!r) r = consumeToken(b, MACRO);
    if (!r) r = consumeToken(b, EOL);
    return r;
  }

  /* ********************************************************** */
  // target_part | (<<parseToDollarNoWhitespaceOrColon>> target_part) | <<parseNoWhitespaceOrColon>>
  public static boolean target(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TARGET, "<target>");
    r = target_part(b, l + 1);
    if (!r) r = target_1(b, l + 1);
    if (!r) r = parseNoWhitespaceOrColon(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<parseToDollarNoWhitespaceOrColon>> target_part
  private static boolean target_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseToDollarNoWhitespaceOrColon(b, l + 1);
    r = r && target_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // targets (':'':' | ':') (target_pattern ':')? ((exportvar|override|privatevar|variable-assignment|prerequisites) (';' inline_command?)? EOL?)
  public static boolean target_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TARGET_LINE, "<target line>");
    r = targets(b, l + 1);
    r = r && target_line_1(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, target_line_2(b, l + 1));
    r = p && target_line_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ':'':' | ':'
  private static boolean target_line_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, COLON, COLON);
    if (!r) r = consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // (target_pattern ':')?
  private static boolean target_line_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line_2")) return false;
    target_line_2_0(b, l + 1);
    return true;
  }

  // target_pattern ':'
  private static boolean target_line_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = target_pattern(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // (exportvar|override|privatevar|variable-assignment|prerequisites) (';' inline_command?)? EOL?
  private static boolean target_line_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = target_line_3_0(b, l + 1);
    r = r && target_line_3_1(b, l + 1);
    r = r && target_line_3_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // exportvar|override|privatevar|variable-assignment|prerequisites
  private static boolean target_line_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line_3_0")) return false;
    boolean r;
    r = exportvar(b, l + 1);
    if (!r) r = override(b, l + 1);
    if (!r) r = privatevar(b, l + 1);
    if (!r) r = variable_assignment(b, l + 1);
    if (!r) r = prerequisites(b, l + 1);
    return r;
  }

  // (';' inline_command?)?
  private static boolean target_line_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line_3_1")) return false;
    target_line_3_1_0(b, l + 1);
    return true;
  }

  // ';' inline_command?
  private static boolean target_line_3_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line_3_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && target_line_3_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // inline_command?
  private static boolean target_line_3_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line_3_1_0_1")) return false;
    inline_command(b, l + 1);
    return true;
  }

  // EOL?
  private static boolean target_line_3_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line_3_2")) return false;
    consumeToken(b, EOL);
    return true;
  }

  /* ********************************************************** */
  // ((function | variable-usage) <<parseToDollarNoWhitespaceOrColonBehind>> target_part) | ((function | variable-usage) <<parseNoWhitespaceOrColonBehind>>)
  static boolean target_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_part")) return false;
    if (!nextTokenIs(b, DOLLAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = target_part_0(b, l + 1);
    if (!r) r = target_part_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (function | variable-usage) <<parseToDollarNoWhitespaceOrColonBehind>> target_part
  private static boolean target_part_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_part_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = target_part_0_0(b, l + 1);
    r = r && parseToDollarNoWhitespaceOrColonBehind(b, l + 1);
    r = r && target_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // function | variable-usage
  private static boolean target_part_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_part_0_0")) return false;
    boolean r;
    r = function(b, l + 1);
    if (!r) r = variable_usage(b, l + 1);
    return r;
  }

  // (function | variable-usage) <<parseNoWhitespaceOrColonBehind>>
  private static boolean target_part_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_part_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = target_part_1_0(b, l + 1);
    r = r && parseNoWhitespaceOrColonBehind(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // function | variable-usage
  private static boolean target_part_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_part_1_0")) return false;
    boolean r;
    r = function(b, l + 1);
    if (!r) r = variable_usage(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // target
  public static boolean target_pattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_pattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TARGET_PATTERN, "<target pattern>");
    r = target(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // target+
  public static boolean targets(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "targets")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TARGETS, "<targets>");
    r = target(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!target(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "targets", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'undefine' variable EOL?
  public static boolean undefine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "undefine")) return false;
    if (!nextTokenIs(b, KEYWORD_UNDEFINE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNDEFINE, null);
    r = consumeToken(b, KEYWORD_UNDEFINE);
    p = r; // pin = 1
    r = r && report_error_(b, variable(b, l + 1));
    r = p && undefine_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // EOL?
  private static boolean undefine_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "undefine_2")) return false;
    consumeToken(b, EOL);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE, "<variable>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // variable assignment variable-value
  public static boolean variable_assignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_assignment")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_ASSIGNMENT, null);
    r = variable(b, l + 1);
    r = r && assignment(b, l + 1);
    p = r; // pin = 2
    r = r && variable_value(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ('$''(' <<parseVariableUsageParen>>) | ('$''{' <<parseVariableUsageCurly>>) | ('$' chars)
  public static boolean variable_usage(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_usage")) return false;
    if (!nextTokenIs(b, DOLLAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variable_usage_0(b, l + 1);
    if (!r) r = variable_usage_1(b, l + 1);
    if (!r) r = variable_usage_2(b, l + 1);
    exit_section_(b, m, VARIABLE_USAGE, r);
    return r;
  }

  // '$''(' <<parseVariableUsageParen>>
  private static boolean variable_usage_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_usage_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOLLAR, OPEN_PAREN);
    r = r && parseVariableUsageParen(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '$''{' <<parseVariableUsageCurly>>
  private static boolean variable_usage_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_usage_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOLLAR, OPEN_CURLY);
    r = r && parseVariableUsageCurly(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '$' chars
  private static boolean variable_usage_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_usage_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOLLAR, CHARS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // multiline
  public static boolean variable_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_value")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = multiline(b, l + 1);
    exit_section_(b, m, VARIABLE_VALUE, r);
    return r;
  }

  /* ********************************************************** */
  // 'vpath' (pattern 'include'? directory*)? EOL?
  public static boolean vpath(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vpath")) return false;
    if (!nextTokenIs(b, KEYWORD_VPATH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VPATH, null);
    r = consumeToken(b, KEYWORD_VPATH);
    p = r; // pin = 1
    r = r && report_error_(b, vpath_1(b, l + 1));
    r = p && vpath_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (pattern 'include'? directory*)?
  private static boolean vpath_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vpath_1")) return false;
    vpath_1_0(b, l + 1);
    return true;
  }

  // pattern 'include'? directory*
  private static boolean vpath_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vpath_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = pattern(b, l + 1);
    r = r && vpath_1_0_1(b, l + 1);
    r = r && vpath_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'include'?
  private static boolean vpath_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vpath_1_0_1")) return false;
    consumeToken(b, KEYWORD_INCLUDE);
    return true;
  }

  // directory*
  private static boolean vpath_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vpath_1_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!directory(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "vpath_1_0_2", c)) break;
    }
    return true;
  }

  // EOL?
  private static boolean vpath_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "vpath_2")) return false;
    consumeToken(b, EOL);
    return true;
  }

  static final Parser function_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return function_recover(b, l + 1);
    }
  };
  static final Parser recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return recover(b, l + 1);
    }
  };
}
