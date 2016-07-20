package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;

public class PostCssCustomSelectorImpl extends CompositePsiElement implements PostCssCustomSelector {
  public PostCssCustomSelectorImpl() {
    super(PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR);
  }
}