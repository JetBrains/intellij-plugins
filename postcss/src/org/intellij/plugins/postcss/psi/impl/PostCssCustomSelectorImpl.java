package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import org.intellij.plugins.postcss.PostCssElementTypes;

public class PostCssCustomSelectorImpl extends CompositePsiElement {
  public PostCssCustomSelectorImpl() {
    super(PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR);
  }
}