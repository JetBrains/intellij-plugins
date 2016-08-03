package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;

public class PostCssAmpersandImpl extends LeafPsiElement {
  public PostCssAmpersandImpl() {
    super(PostCssTokenTypes.AMPERSAND, "&");
  }
}