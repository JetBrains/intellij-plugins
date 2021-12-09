// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinPsiElement;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

import javax.swing.*;


public abstract class GherkinPsiElementBase extends ASTWrapperPsiElement implements GherkinPsiElement {
  private static final TokenSet TEXT_FILTER = TokenSet.create(GherkinTokenTypes.TEXT);

  public GherkinPsiElementBase(@NotNull final ASTNode node) {
    super(node);
  }

  @NotNull
  protected String getElementText() {
    final ASTNode node = getNode();
    final ASTNode[] children = node.getChildren(TEXT_FILTER);
    return StringUtil.join(children, astNode -> astNode.getText(), " ").trim();
  }

  @Override
  public ItemPresentation getPresentation() {
    return new ItemPresentation() {
      @Override
      public String getPresentableText() {
        return GherkinPsiElementBase.this.getPresentableText();
      }

      @Override
      public Icon getIcon(final boolean open) {
        return GherkinPsiElementBase.this.getIcon(Iconable.ICON_FLAG_VISIBILITY);
      }
    };
  }

  protected @NlsSafe String getPresentableText() {
    return toString();
  }

  protected String buildPresentableText(final String prefix) {
    final StringBuilder result = new StringBuilder(prefix);
    final String name = getElementText();
    if (!StringUtil.isEmpty(name)) {
      result.append(": ").append(name);
    }
    return result.toString();
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GherkinElementVisitor) {
      acceptGherkin((GherkinElementVisitor) visitor);
    }
    else {
      super.accept(visitor);
    }
  }

  protected abstract void acceptGherkin(GherkinElementVisitor gherkinElementVisitor);
}
