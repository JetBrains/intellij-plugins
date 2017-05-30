package org.angularjs.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiParser;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.angularjs.lang.lexer.AngularJSLexer;
import org.angularjs.lang.psi.AngularJSAsExpression;
import org.angularjs.lang.psi.AngularJSFilterExpression;
import org.angularjs.lang.psi.AngularJSMessageFormatExpression;
import org.angularjs.lang.psi.AngularJSRepeatExpression;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSParserDefinition extends JavascriptParserDefinition {
  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new AngularJSLexer();
  }

  @NotNull
  @Override
  public PsiParser createParser(Project project) {
    return new AngularParser();
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    final IElementType type = node.getElementType();
    if (type == AngularJSElementTypes.REPEAT_EXPRESSION || type == AngularJSElementTypes.FOR_EXPRESSION) {
      return new AngularJSRepeatExpression(node);
    } else if (type == AngularJSElementTypes.FILTER_EXPRESSION) {
      return new AngularJSFilterExpression(node);
    } else if (type == AngularJSElementTypes.AS_EXPRESSION) {
      return new AngularJSAsExpression(node);
    } else if (type == AngularJSElementTypes.MESSAGE_FORMAT_EXPRESSION) {
      return new AngularJSMessageFormatExpression(node);
    }
    return super.createElement(node);
  }

  @Override
  public IFileElementType getFileNodeType() {
    return AngularJSElementTypes.FILE;
  }
}
