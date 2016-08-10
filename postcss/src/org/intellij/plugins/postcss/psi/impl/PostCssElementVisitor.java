package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.css.CssElementVisitor;

abstract public class PostCssElementVisitor extends CssElementVisitor {
  public void visitPostCssNest(final PostCssNestImpl postCssNest) {
    visitElement(postCssNest);
  }

  public void visitPostCssCustomSelectorAtRule(final PostCssCustomSelectorAtRuleImpl postCssCustomSelectorAtRule) {
    visitElement(postCssCustomSelectorAtRule);
  }

  public void visitPostCssCustomSelector(final PostCssCustomSelectorImpl postCssCustomSelector) {
    visitElement(postCssCustomSelector);
  }

  public void visitPostCssCustomMediaAtRule(final PostCssCustomMediaAtRuleImpl postCssCustomMediaAtRule) {
    visitElement(postCssCustomMediaAtRule);
  }

  public void visitPostCssApplyAtRule(final PostCssApplyAtRuleImpl postCssApplyAtRule) {
    visitElement(postCssApplyAtRule);
  }
}