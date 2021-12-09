package org.intellij.plugins.postcss.parser;

import com.intellij.lang.PsiParser;
import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.lexer.PostCssLexer;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.intellij.plugins.postcss.psi.impl.PostCssFileImpl;
import org.jetbrains.annotations.NotNull;

public class PostCssParserDefinition extends CSSParserDefinition {

  @Override
  @NotNull
  public Lexer createLexer(Project project) {
    return new PostCssLexer();
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new PostCssFileImpl(viewProvider);
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return PostCssElementTypes.POST_CSS_FILE;
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return PostCssTokenTypes.POST_CSS_COMMENTS;
  }

  @NotNull
  @Override
  public PsiParser createParser(final Project project) {
    return new PostCssParser();
  }
}
