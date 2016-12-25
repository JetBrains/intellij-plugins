// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import name.kropp.intellij.makefile.psi.impl.*;

public interface MakefileTypes {

  IElementType COMMANDS = new MakefileElementType("COMMANDS");
  IElementType CONDITIONAL = new MakefileElementType("CONDITIONAL");
  IElementType DEFINE = new MakefileElementType("DEFINE");
  IElementType ELSEBRANCH = new MakefileElementType("ELSEBRANCH");
  IElementType FILENAME = new MakefileElementType("FILENAME");
  IElementType INCLUDE = new MakefileElementType("INCLUDE");
  IElementType NORMAL_PREREQUISITES = new MakefileElementType("NORMAL_PREREQUISITES");
  IElementType ORDER_ONLY_PREREQUISITES = new MakefileElementType("ORDER_ONLY_PREREQUISITES");
  IElementType PREREQUISITE = new MakefileElementType("PREREQUISITE");
  IElementType PREREQUISITES = new MakefileElementType("PREREQUISITES");
  IElementType RECIPE = new MakefileElementType("RECIPE");
  IElementType RULE = new MakefileElementType("RULE");
  IElementType TARGET = new MakefileElementType("TARGET");
  IElementType TARGETS = new MakefileElementType("TARGETS");
  IElementType TARGET_LINE = new MakefileElementType("TARGET_LINE");
  IElementType THENBRANCH = new MakefileElementType("THENBRANCH");
  IElementType UNDEFINE = new MakefileElementType("UNDEFINE");
  IElementType VARIABLE = new MakefileElementType("VARIABLE");
  IElementType VARIABLE_NAME = new MakefileElementType("VARIABLE_NAME");

  IElementType ASSIGN = new MakefileTokenType("=");
  IElementType COLON = new MakefileTokenType(":");
  IElementType COMMAND = new MakefileTokenType("command");
  IElementType COMMENT = new MakefileTokenType("comment");
  IElementType CONDITION = new MakefileTokenType("condition");
  IElementType EOL = new MakefileTokenType("EOL");
  IElementType IDENTIFIER = new MakefileTokenType("identifier");
  IElementType KEYWORD_DEFINE = new MakefileTokenType("define");
  IElementType KEYWORD_ELSE = new MakefileTokenType("else");
  IElementType KEYWORD_ENDEF = new MakefileTokenType("endef");
  IElementType KEYWORD_ENDIF = new MakefileTokenType("endif");
  IElementType KEYWORD_IFEQ = new MakefileTokenType("ifeq");
  IElementType KEYWORD_IFNDEF = new MakefileTokenType("ifndef");
  IElementType KEYWORD_IFNEQ = new MakefileTokenType("ifneq");
  IElementType KEYWORD_INCLUDE = new MakefileTokenType("include");
  IElementType KEYWORD_UNDEFINE = new MakefileTokenType("undefine");
  IElementType PIPE = new MakefileTokenType("|");
  IElementType SEMICOLON = new MakefileTokenType(";");
  IElementType VARIABLE_VALUE = new MakefileTokenType("variable_value");
  IElementType VARIABLE_VALUE_LINE = new MakefileTokenType("variable_value_line");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == COMMANDS) {
        return new MakefileCommandsImpl(node);
      }
      else if (type == CONDITIONAL) {
        return new MakefileConditionalImpl(node);
      }
      else if (type == DEFINE) {
        return new MakefileDefineImpl(node);
      }
      else if (type == ELSEBRANCH) {
        return new MakefileElsebranchImpl(node);
      }
      else if (type == FILENAME) {
        return new MakefileFilenameImpl(node);
      }
      else if (type == INCLUDE) {
        return new MakefileIncludeImpl(node);
      }
      else if (type == NORMAL_PREREQUISITES) {
        return new MakefileNormalPrerequisitesImpl(node);
      }
      else if (type == ORDER_ONLY_PREREQUISITES) {
        return new MakefileOrderOnlyPrerequisitesImpl(node);
      }
      else if (type == PREREQUISITE) {
        return new MakefilePrerequisiteImpl(node);
      }
      else if (type == PREREQUISITES) {
        return new MakefilePrerequisitesImpl(node);
      }
      else if (type == RECIPE) {
        return new MakefileRecipeImpl(node);
      }
      else if (type == RULE) {
        return new MakefileRuleImpl(node);
      }
      else if (type == TARGET) {
        return new MakefileTargetImpl(node);
      }
      else if (type == TARGETS) {
        return new MakefileTargetsImpl(node);
      }
      else if (type == TARGET_LINE) {
        return new MakefileTargetLineImpl(node);
      }
      else if (type == THENBRANCH) {
        return new MakefileThenbranchImpl(node);
      }
      else if (type == UNDEFINE) {
        return new MakefileUndefineImpl(node);
      }
      else if (type == VARIABLE) {
        return new MakefileVariableImpl(node);
      }
      else if (type == VARIABLE_NAME) {
        return new MakefileVariableNameImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
