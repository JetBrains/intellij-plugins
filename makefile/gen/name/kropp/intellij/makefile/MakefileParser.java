// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
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
    if (t == COMMANDS) {
      r = commands(b, 0);
    }
    else if (t == CONDITIONAL) {
      r = conditional(b, 0);
    }
    else if (t == DEFINE) {
      r = define(b, 0);
    }
    else if (t == ELSEBRANCH) {
      r = elsebranch(b, 0);
    }
    else if (t == EXPORT) {
      r = export(b, 0);
    }
    else if (t == FILENAME) {
      r = filename(b, 0);
    }
    else if (t == INCLUDE) {
      r = include(b, 0);
    }
    else if (t == NORMAL_PREREQUISITES) {
      r = normal_prerequisites(b, 0);
    }
    else if (t == ORDER_ONLY_PREREQUISITES) {
      r = order_only_prerequisites(b, 0);
    }
    else if (t == OVERRIDE) {
      r = override(b, 0);
    }
    else if (t == PREREQUISITE) {
      r = prerequisite(b, 0);
    }
    else if (t == PREREQUISITES) {
      r = prerequisites(b, 0);
    }
    else if (t == RECIPE) {
      r = recipe(b, 0);
    }
    else if (t == RULE) {
      r = rule(b, 0);
    }
    else if (t == TARGET) {
      r = target(b, 0);
    }
    else if (t == TARGET_LINE) {
      r = target_line(b, 0);
    }
    else if (t == TARGETS) {
      r = targets(b, 0);
    }
    else if (t == THENBRANCH) {
      r = thenbranch(b, 0);
    }
    else if (t == UNDEFINE) {
      r = undefine(b, 0);
    }
    else if (t == VARIABLE) {
      r = variable(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return makefile(b, l + 1);
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
  // command*
  public static boolean commands(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "commands")) return false;
    Marker m = enter_section_(b, l, _NONE_, COMMANDS, "<commands>");
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, COMMAND)) break;
      if (!empty_element_parsed_guard_(b, "commands", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // ('ifeq'|'ifneq'|'ifndef') condition thenbranch ('else' elsebranch)* 'endif'
  public static boolean conditional(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITIONAL, "<conditional>");
    r = conditional_0(b, l + 1);
    r = r && consumeToken(b, CONDITION);
    r = r && thenbranch(b, l + 1);
    r = r && conditional_3(b, l + 1);
    r = r && consumeToken(b, KEYWORD_ENDIF);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'ifeq'|'ifneq'|'ifndef'
  private static boolean conditional_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KEYWORD_IFEQ);
    if (!r) r = consumeToken(b, KEYWORD_IFNEQ);
    if (!r) r = consumeToken(b, KEYWORD_IFNDEF);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('else' elsebranch)*
  private static boolean conditional_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!conditional_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "conditional_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // 'else' elsebranch
  private static boolean conditional_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KEYWORD_ELSE);
    r = r && elsebranch(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'define' variable assignment? variable-value-line* 'endef'
  public static boolean define(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "define")) return false;
    if (!nextTokenIs(b, KEYWORD_DEFINE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DEFINE, null);
    r = consumeToken(b, KEYWORD_DEFINE);
    p = r; // pin = 1
    r = r && report_error_(b, variable(b, l + 1));
    r = p && report_error_(b, define_2(b, l + 1)) && r;
    r = p && report_error_(b, define_3(b, l + 1)) && r;
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

  // variable-value-line*
  private static boolean define_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "define_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, VARIABLE_VALUE_LINE)) break;
      if (!empty_element_parsed_guard_(b, "define_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // define|include|undefine|override|export
  static boolean directive(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = define(b, l + 1);
    if (!r) r = include(b, l + 1);
    if (!r) r = undefine(b, l + 1);
    if (!r) r = override(b, l + 1);
    if (!r) r = export(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // commands
  public static boolean elsebranch(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elsebranch")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ELSEBRANCH, "<elsebranch>");
    r = commands(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'export' (variable-assignment|variable)?
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

  // (variable-assignment|variable)?
  private static boolean export_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export_1")) return false;
    export_1_0(b, l + 1);
    return true;
  }

  // variable-assignment|variable
  private static boolean export_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "export_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variable_assignment(b, l + 1);
    if (!r) r = variable(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean filename(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "filename")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, FILENAME, r);
    return r;
  }

  /* ********************************************************** */
  // ('include'|'-include'|'sinclude') filename+ EOL
  public static boolean include(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INCLUDE, "<include>");
    r = include_0(b, l + 1);
    r = r && include_1(b, l + 1);
    r = r && consumeToken(b, EOL);
    exit_section_(b, l, m, r, false, null);
    return r;
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
    int c = current_position_(b);
    while (r) {
      if (!filename(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "include_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (rule|variable-assignment|directive|comment)*
  static boolean makefile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "makefile")) return false;
    int c = current_position_(b);
    while (true) {
      if (!makefile_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "makefile", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // rule|variable-assignment|directive|comment
  private static boolean makefile_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "makefile_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rule(b, l + 1);
    if (!r) r = variable_assignment(b, l + 1);
    if (!r) r = directive(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // prerequisite*
  public static boolean normal_prerequisites(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_prerequisites")) return false;
    Marker m = enter_section_(b, l, _NONE_, NORMAL_PREREQUISITES, "<normal prerequisites>");
    int c = current_position_(b);
    while (true) {
      if (!prerequisite(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "normal_prerequisites", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // prerequisite+
  public static boolean order_only_prerequisites(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "order_only_prerequisites")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prerequisite(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!prerequisite(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "order_only_prerequisites", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, ORDER_ONLY_PREREQUISITES, r);
    return r;
  }

  /* ********************************************************** */
  // 'override' variable-assignment
  public static boolean override(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "override")) return false;
    if (!nextTokenIs(b, KEYWORD_OVERRIDE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KEYWORD_OVERRIDE);
    r = r && variable_assignment(b, l + 1);
    exit_section_(b, m, OVERRIDE, r);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean prerequisite(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prerequisite")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, PREREQUISITE, r);
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
  // (conditional|commands)?
  public static boolean recipe(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recipe")) return false;
    Marker m = enter_section_(b, l, _NONE_, RECIPE, "<recipe>");
    recipe_0(b, l + 1);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // conditional|commands
  private static boolean recipe_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recipe_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = conditional(b, l + 1);
    if (!r) r = commands(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // target_line (';' EOL? | EOL) recipe
  public static boolean rule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rule")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = target_line(b, l + 1);
    r = r && rule_1(b, l + 1);
    r = r && recipe(b, l + 1);
    exit_section_(b, m, RULE, r);
    return r;
  }

  // ';' EOL? | EOL
  private static boolean rule_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rule_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rule_1_0(b, l + 1);
    if (!r) r = consumeToken(b, EOL);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';' EOL?
  private static boolean rule_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rule_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && rule_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EOL?
  private static boolean rule_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rule_1_0_1")) return false;
    consumeToken(b, EOL);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean target(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, TARGET, r);
    return r;
  }

  /* ********************************************************** */
  // targets ':' prerequisites
  public static boolean target_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TARGET_LINE, null);
    r = targets(b, l + 1);
    r = r && consumeToken(b, COLON);
    p = r; // pin = 2
    r = r && prerequisites(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // target+
  public static boolean targets(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "targets")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = target(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!target(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "targets", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, TARGETS, r);
    return r;
  }

  /* ********************************************************** */
  // commands
  public static boolean thenbranch(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "thenbranch")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, THENBRANCH, "<thenbranch>");
    r = commands(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'undefine' variable EOL
  public static boolean undefine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "undefine")) return false;
    if (!nextTokenIs(b, KEYWORD_UNDEFINE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, KEYWORD_UNDEFINE);
    r = r && variable(b, l + 1);
    r = r && consumeToken(b, EOL);
    exit_section_(b, m, UNDEFINE, r);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, VARIABLE, r);
    return r;
  }

  /* ********************************************************** */
  // variable assignment variable-value?
  public static boolean variable_assignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_assignment")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_ASSIGNMENT, null);
    r = variable(b, l + 1);
    r = r && assignment(b, l + 1);
    p = r; // pin = 2
    r = r && variable_assignment_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // variable-value?
  private static boolean variable_assignment_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_assignment_2")) return false;
    consumeToken(b, VARIABLE_VALUE);
    return true;
  }

}
