package com.jetbrains.plugins.jade.todo;

import com.intellij.application.options.CodeStyle;
import com.intellij.lexer.Lexer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.search.IndexPatternBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.plugins.jade.lexer.JadeLexer;
import com.jetbrains.plugins.jade.psi.JadeFileImpl;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JadeIndexPatternBuilder implements IndexPatternBuilder {
  @Override
  public @Nullable Lexer getIndexingLexer(@NotNull PsiFile file) {
    if (file instanceof JadeFileImpl) {
      return new JadeLexer(CodeStyle.getSettings(file));
    }
    else {
      return null;
    }
  }

  @Override
  public @Nullable TokenSet getCommentTokenSet(@NotNull PsiFile file) {
    return JadeTokenTypes.COMMENTS;
  }

  @Override
  public int getCommentStartDelta(IElementType tokenType) {
    if (tokenType == JadeTokenTypes.COMMENT) {
      return 2;
    }
    else {
      return 3;
    }
  }

  @Override
  public int getCommentEndDelta(IElementType tokenType) {
    return 0;
  }
}
