package org.intellij.plugins.postcss.parser;

import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
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
}
