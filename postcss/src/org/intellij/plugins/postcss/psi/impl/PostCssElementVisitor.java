package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.css.CssElementVisitor;

abstract public class PostCssElementVisitor extends CssElementVisitor {
  public void visitPostCssNest(final PostCssNestImpl postCssNest) {
    visitElement(postCssNest);
  }
}