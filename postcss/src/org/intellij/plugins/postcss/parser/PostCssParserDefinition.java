package org.intellij.plugins.postcss.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssFileImpl;
import org.jetbrains.annotations.NotNull;

public class PostCssParserDefinition extends CSSParserDefinition {

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new PostCssFileImpl(viewProvider);
  }

  @Override
  public IFileElementType getFileNodeType() {
    return PostCssElementTypes.POST_CSS_FILE;
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return PostCssElementTypes.POST_CSS_COMMENTS;
  }

  @NotNull
  @Override
  public PsiParser createParser(final Project project) {
    return new PsiParser() {
      @NotNull
      @Override
      public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        return new PostCssParser(builder).parse(root);
      }
    };
  }

}
