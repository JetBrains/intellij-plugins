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
  IElementType RULE = new MakefileElementType("RULE");
  IElementType TARGET = new MakefileElementType("TARGET");
  IElementType TARGET_LINE = new MakefileElementType("TARGET_LINE");

  IElementType COMMAND = new MakefileTokenType("command");
  IElementType COMMENT = new MakefileTokenType("comment");
  IElementType EOL = new MakefileTokenType("EOL");
  IElementType IDENTIFIER = new MakefileTokenType("identifier");
  IElementType SEPARATOR = new MakefileTokenType("separator");

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
      else if (type == RULE) {
        return new MakefileRuleImpl(node);
      }
      else if (type == TARGET) {
        return new MakefileTargetImpl(node);
      }
      else if (type == TARGET_LINE) {
        return new MakefileTargetLineImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
