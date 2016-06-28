package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.impl.source.tree.CompositePsiElement;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssElement;

public class PostCssDirectNestImpl extends CompositePsiElement implements PostCssElement {
  protected PostCssDirectNestImpl() {
    super(PostCssElementTypes.POST_CSS_DIRECT_NEST);
  }
}
