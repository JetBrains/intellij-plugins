// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import name.kropp.intellij.makefile.psi.impl.*;

public interface MakefileTypes {

  IElementType COMMANDS = new MakefileElementType("COMMANDS");
  IElementType DEPENDENCIES = new MakefileElementType("DEPENDENCIES");
  IElementType RULE = new MakefileElementType("RULE");
  IElementType TARGET_LINE = new MakefileElementType("TARGET_LINE");

  IElementType COMMAND = new MakefileTokenType("command");
  IElementType COMMENT = new MakefileTokenType("comment");
  IElementType DEPENDENCY = new MakefileTokenType("dependency");
  IElementType EOL = new MakefileTokenType("EOL");
  IElementType SEPARATOR = new MakefileTokenType("separator");
  IElementType TARGET = new MakefileTokenType("target");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == COMMANDS) {
        return new MakefileCommandsImpl(node);
      }
      else if (type == DEPENDENCIES) {
        return new MakefileDependenciesImpl(node);
      }
      else if (type == RULE) {
        return new MakefileRuleImpl(node);
      }
      else if (type == TARGET_LINE) {
        return new MakefileTargetLineImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
