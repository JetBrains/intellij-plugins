// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import name.kropp.intellij.makefile.psi.impl.*;

public interface MakefileTypes {

  IElementType BLOCK = new MakefileElementType("BLOCK");
  IElementType BRANCH = new MakefileElementType("BRANCH");
  IElementType COMMAND = new MakefileElementType("COMMAND");
  IElementType CONDITIONAL = new MakefileElementType("CONDITIONAL");
  IElementType DEFINE = new MakefileElementType("DEFINE");
  IElementType DIRECTORY = new MakefileElementType("DIRECTORY");
  IElementType EXPORT = new MakefileElementType("EXPORT");
  IElementType FILENAME = new MakefileElementType("FILENAME");
  IElementType INCLUDE = new MakefileElementType("INCLUDE");
  IElementType NORMAL_PREREQUISITES = new MakefileElementType("NORMAL_PREREQUISITES");
  IElementType ORDER_ONLY_PREREQUISITES = new MakefileElementType("ORDER_ONLY_PREREQUISITES");
  IElementType OVERRIDE = new MakefileElementType("OVERRIDE");
  IElementType PATTERN = new MakefileElementType("PATTERN");
  IElementType PREREQUISITE = new MakefileElementType("PREREQUISITE");
  IElementType PREREQUISITES = new MakefileElementType("PREREQUISITES");
  IElementType PRIVATEVAR = new MakefileElementType("PRIVATEVAR");
  IElementType RECIPE = new MakefileElementType("RECIPE");
  IElementType RULE = new MakefileElementType("RULE");
  IElementType TARGET = new MakefileElementType("TARGET");
  IElementType TARGETS = new MakefileElementType("TARGETS");
  IElementType TARGET_LINE = new MakefileElementType("TARGET_LINE");
  IElementType TOPCONDITIONAL = new MakefileElementType("TOPCONDITIONAL");
  IElementType UNDEFINE = new MakefileElementType("UNDEFINE");
  IElementType VARIABLE = new MakefileElementType("VARIABLE");
  IElementType VARIABLE_ASSIGNMENT = new MakefileElementType("VARIABLE_ASSIGNMENT");
  IElementType VARIABLE_VALUE = new MakefileElementType("VARIABLE_VALUE");
  IElementType VPATH = new MakefileElementType("VPATH");

  IElementType ASSIGN = new MakefileTokenType("=");
  IElementType COLON = new MakefileTokenType(":");
  IElementType COMMAND_LINE = new MakefileTokenType("command-line");
  IElementType COMMENT = new MakefileTokenType("comment");
  IElementType CONDITION = new MakefileTokenType("condition");
  IElementType EOL = new MakefileTokenType("EOL");
  IElementType IDENTIFIER = new MakefileTokenType("identifier");
  IElementType IF = new MakefileTokenType("if");
  IElementType KEYWORD_DEFINE = new MakefileTokenType("define");
  IElementType KEYWORD_ELSE = new MakefileTokenType("else");
  IElementType KEYWORD_ENDEF = new MakefileTokenType("endef");
  IElementType KEYWORD_ENDIF = new MakefileTokenType("endif");
  IElementType KEYWORD_EXPORT = new MakefileTokenType("export");
  IElementType KEYWORD_IFEQ = new MakefileTokenType("ifeq");
  IElementType KEYWORD_IFNDEF = new MakefileTokenType("ifndef");
  IElementType KEYWORD_IFNEQ = new MakefileTokenType("ifneq");
  IElementType KEYWORD_INCLUDE = new MakefileTokenType("include");
  IElementType KEYWORD_OVERRIDE = new MakefileTokenType("override");
  IElementType KEYWORD_PRIVATE = new MakefileTokenType("private");
  IElementType KEYWORD_UNDEFINE = new MakefileTokenType("undefine");
  IElementType KEYWORD_VPATH = new MakefileTokenType("vpath");
  IElementType LINE_SPLIT = new MakefileTokenType("line-split");
  IElementType PIPE = new MakefileTokenType("|");
  IElementType SEMICOLON = new MakefileTokenType(";");
  IElementType VARIABLE_VALUE_LINE = new MakefileTokenType("variable-value-line");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == BLOCK) {
        return new MakefileBlockImpl(node);
      }
      else if (type == BRANCH) {
        return new MakefileBranchImpl(node);
      }
      else if (type == COMMAND) {
        return new MakefileCommandImpl(node);
      }
      else if (type == CONDITIONAL) {
        return new MakefileConditionalImpl(node);
      }
      else if (type == DEFINE) {
        return new MakefileDefineImpl(node);
      }
      else if (type == DIRECTORY) {
        return new MakefileDirectoryImpl(node);
      }
      else if (type == EXPORT) {
        return new MakefileExportImpl(node);
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
      else if (type == OVERRIDE) {
        return new MakefileOverrideImpl(node);
      }
      else if (type == PATTERN) {
        return new MakefilePatternImpl(node);
      }
      else if (type == PREREQUISITE) {
        return new MakefilePrerequisiteImpl(node);
      }
      else if (type == PREREQUISITES) {
        return new MakefilePrerequisitesImpl(node);
      }
      else if (type == PRIVATEVAR) {
        return new MakefilePrivatevarImpl(node);
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
      else if (type == TOPCONDITIONAL) {
        return new MakefileTopconditionalImpl(node);
      }
      else if (type == UNDEFINE) {
        return new MakefileUndefineImpl(node);
      }
      else if (type == VARIABLE) {
        return new MakefileVariableImpl(node);
      }
      else if (type == VARIABLE_ASSIGNMENT) {
        return new MakefileVariableAssignmentImpl(node);
      }
      else if (type == VARIABLE_VALUE) {
        return new MakefileVariableValueImpl(node);
      }
      else if (type == VPATH) {
        return new MakefileVpathImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
