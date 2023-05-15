package org.intellij.plugins.postcss;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.impl.util.editor.CssCommenter;
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.intellij.plugins.postcss.settings.PostCssCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssCommentProvider implements MultipleLangCommentProvider {
  private static final Commenter CSS_COMMENTER = new CssCommenter();
  private static final Commenter POST_CSS_COMMENTER = new CssCommenter() {
    @Override
    public String getLineCommentPrefix() {
      return "//";
    }

    @Override
    public IElementType getLineCommentTokenType() {
      return PostCssTokenTypes.POST_CSS_COMMENT;
    }
  };

  @Override
  public @Nullable Commenter getLineCommenter(@NotNull PsiFile file,
                                              @NotNull Editor editor,
                                              @NotNull Language lineStartLanguage,
                                              @NotNull Language lineEndLanguage) {
    if (CodeStyle.getSettings(file).getCustomSettings(PostCssCodeStyleSettings.class).COMMENTS_INLINE_STYLE) {
      return POST_CSS_COMMENTER;
    }
    return CSS_COMMENTER;
  }

  @Override
  public boolean canProcess(@NotNull PsiFile file, @NotNull FileViewProvider viewProvider) {
    return file.getLanguage() == PostCssLanguage.INSTANCE;
  }
}