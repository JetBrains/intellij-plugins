package org.angularjs.lang.parser;

import com.intellij.lang.PsiParser;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IFileElementType;
import org.angularjs.lang.lexer.AngularJSLexer;
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

  @Override
  public IFileElementType getFileNodeType() {
    return AngularJSElementTypes.FILE;
  }
}
