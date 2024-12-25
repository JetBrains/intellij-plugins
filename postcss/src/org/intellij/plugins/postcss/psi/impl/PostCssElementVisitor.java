package org.intellij.plugins.postcss.psi.impl;

import com.intellij.psi.css.CssElementVisitor;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariable;
import org.intellij.plugins.postcss.psi.PostCssSimpleVariableDeclaration;

public abstract class PostCssElementVisitor extends CssElementVisitor {
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

  public void visitPostCssSimpleVariable(final PostCssSimpleVariable postCssSimpleVariable) {
    visitElement(postCssSimpleVariable);
  }

  public void visitPostCssSimpleVariableDeclaration(final PostCssSimpleVariableDeclaration postCssSimpleVariableDeclaration) {
    visitElement(postCssSimpleVariableDeclaration);
  }
}