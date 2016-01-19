package org.angularjs.html;

import com.intellij.lang.html.HTMLParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class Angular2HTMLParserDefinition extends HTMLParserDefinition {
  static IFileElementType HTML_FILE = new IStubFileElementType(Angular2HTMLLanguage.INSTANCE);

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new Angular2HTMLLexer();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return HTML_FILE;
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new HtmlFileImpl(viewProvider, HTML_FILE);
  }
}
