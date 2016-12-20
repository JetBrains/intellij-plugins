// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static name.kropp.intellij.makefile.psi.MakefileTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

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
    else if (t == DEPENDENCIES) {
      r = dependencies(b, 0);
    }
    else if (t == DEPENDENCY) {
      r = dependency(b, 0);
    }
    else if (t == ELSE_) {
      r = else_(b, 0);
    }
    else if (t == ELSEBRANCH) {
      r = elsebranch(b, 0);
    }
    else if (t == ENDIF) {
      r = endif(b, 0);
    }
    else if (t == IFEQ) {
      r = ifeq(b, 0);
    }
    else if (t == INCLUDE) {
      r = include(b, 0);
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
    else if (t == THENBRANCH) {
      r = thenbranch(b, 0);
    }
    else if (t == VARIABLE) {
      r = variable(b, 0);
    }
    else if (t == VARIABLE_NAME) {
      r = variable_name(b, 0);
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
  // ifeq condition thenbranch else_ elsebranch endif
  public static boolean conditional(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITIONAL, "<conditional>");
    r = ifeq(b, l + 1);
    r = r && consumeToken(b, CONDITION);
    r = r && thenbranch(b, l + 1);
    r = r && else_(b, l + 1);
    r = r && elsebranch(b, l + 1);
    r = r && endif(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // dependency*
  public static boolean dependencies(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dependencies")) return false;
    Marker m = enter_section_(b, l, _NONE_, DEPENDENCIES, "<dependencies>");
    int c = current_position_(b);
    while (true) {
      if (!dependency(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "dependencies", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean dependency(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dependency")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, DEPENDENCY, r);
    return r;
  }

  /* ********************************************************** */
  // 'else'
  public static boolean else_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "else_")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ELSE_, "<else>");
    r = consumeToken(b, "else");
    exit_section_(b, l, m, r, false, null);
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
  // 'endif'
  public static boolean endif(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "endif")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENDIF, "<endif>");
    r = consumeToken(b, "endif");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'ifeq'
  public static boolean ifeq(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifeq")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IFEQ, "<ifeq>");
    r = consumeToken(b, "ifeq");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'include' filename
  public static boolean include(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INCLUDE, "<include>");
    r = consumeToken(b, "include");
    r = r && consumeToken(b, FILENAME);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (rule|variable|include|conditional|comment)*
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

  // rule|variable|include|conditional|comment
  private static boolean makefile_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "makefile_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rule(b, l + 1);
    if (!r) r = variable(b, l + 1);
    if (!r) r = include(b, l + 1);
    if (!r) r = conditional(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // target_line EOL (commands|conditional)?
  public static boolean rule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rule")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = target_line(b, l + 1);
    r = r && consumeToken(b, EOL);
    r = r && rule_2(b, l + 1);
    exit_section_(b, m, RULE, r);
    return r;
  }

  // (commands|conditional)?
  private static boolean rule_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rule_2")) return false;
    rule_2_0(b, l + 1);
    return true;
  }

  // commands|conditional
  private static boolean rule_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rule_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = commands(b, l + 1);
    if (!r) r = conditional(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
  // target ':' dependencies
  public static boolean target_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_line")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TARGET_LINE, null);
    r = target(b, l + 1);
    r = r && consumeToken(b, COLON);
    p = r; // pin = 2
    r = r && dependencies(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // variable_name ('='|':='|'::='|'?='|'!='|'+=') variable_value? EOL
  public static boolean variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE, null);
    r = variable_name(b, l + 1);
    r = r && variable_1(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, variable_2(b, l + 1));
    r = p && consumeToken(b, EOL) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // '='|':='|'::='|'?='|'!='|'+='
  private static boolean variable_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_1")) return false;
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

  // variable_value?
  private static boolean variable_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_2")) return false;
    consumeToken(b, VARIABLE_VALUE);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean variable_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_name")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, VARIABLE_NAME, r);
    return r;
  }

}
