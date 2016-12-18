// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import name.kropp.intellij.makefile.psi.impl.*;

public interface MakefileTypes {

  IElementType COMMANDS = new MakefileElementType("COMMANDS");
  IElementType DEPENDENCIES = new MakefileElementType("DEPENDENCIES");
  IElementType DEPENDENCY = new MakefileElementType("DEPENDENCY");
  IElementType INCLUDE = new MakefileElementType("INCLUDE");
  IElementType RULE = new MakefileElementType("RULE");
  IElementType TARGET = new MakefileElementType("TARGET");
  IElementType TARGET_LINE = new MakefileElementType("TARGET_LINE");
  IElementType VARIABLE = new MakefileElementType("VARIABLE");
  IElementType VARIABLE_NAME = new MakefileElementType("VARIABLE_NAME");

  IElementType ASSIGN = new MakefileTokenType("=");
  IElementType COLON = new MakefileTokenType(":");
  IElementType COMMAND = new MakefileTokenType("command");
  IElementType COMMENT = new MakefileTokenType("comment");
  IElementType EOL = new MakefileTokenType("EOL");
  IElementType FILENAME = new MakefileTokenType("filename");
  IElementType IDENTIFIER = new MakefileTokenType("identifier");
  IElementType VARIABLE_VALUE = new MakefileTokenType("variable_value");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == COMMANDS) {
        return new MakefileCommandsImpl(node);
      }
      else if (type == DEPENDENCIES) {
        return new MakefileDependenciesImpl(node);
      }
      else if (type == DEPENDENCY) {
        return new MakefileDependencyImpl(node);
      }
      else if (type == INCLUDE) {
        return new MakefileIncludeImpl(node);
      }
      else if (type == RULE) {
        return new MakefileRuleImpl(node);
      }
      else if (type == TARGET) {
        return new MakefileTargetImpl(node);
      }
      else if (type == TARGET_LINE) {
        return new MakefileTargetLineImpl(node);
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
