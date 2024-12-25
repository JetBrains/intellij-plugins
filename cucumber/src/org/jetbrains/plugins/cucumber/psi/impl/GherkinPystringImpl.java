// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinPystring;

public class GherkinPystringImpl extends GherkinPsiElementBase implements GherkinPystring {
  public GherkinPystringImpl(final @NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitPystring(this);
  }

  @Override
  public String toString() {
    return "GherkinPystring";
  }

  @Override
  public boolean isValidHost() {
    return true;
  }

  @Override
  public PsiLanguageInjectionHost updateText(final @NotNull String text) {
    final String docStringSep = getFirstChild().getText();
    final int startOffset = text.startsWith(docStringSep) ? docStringSep.length() : 0;
    final int endOffset = text.endsWith(docStringSep) ? docStringSep.length() : 0;
    ((LeafPsiElement) getFirstChild().getNextSibling()).
            replaceWithText(text.substring(startOffset, text.length() - endOffset));
    getFirstChild().getNextSibling().getNextSibling().replace(getLastChild());
    return this;
  }

  @Override
  public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
    return LiteralTextEscaper.createSimple(this);
  }
}
