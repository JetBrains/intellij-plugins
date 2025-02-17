// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import org.jetbrains.annotations.NotNull;

public class JadeJsCodeBlockImpl extends CompositePsiElement implements PsiLanguageInjectionHost {

  public JadeJsCodeBlockImpl() {
    super(JadeElementTypes.JS_CODE_BLOCK);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + getElementType().toString() + ")";
  }

  @Override
  public boolean isValidHost() {
    return true;
  }

  @Override
  public PsiLanguageInjectionHost updateText(@NotNull String text) {
    return ElementManipulators.handleContentChange(this, text);
  }

  @Override
  public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
    return new LiteralTextEscaper<>(this) {
      @Override
      public boolean decode(@NotNull TextRange rangeInsideHost, @NotNull StringBuilder outChars) {
        outChars.append(getText(), rangeInsideHost.getStartOffset(), rangeInsideHost.getEndOffset());
        return true;
      }

      @Override
      public int getOffsetInHost(int offsetInDecoded, @NotNull TextRange rangeInsideHost) {
        return offsetInDecoded;
      }

      @Override
      public boolean isOneLine() {
        return false;
      }
    };
  }

  public static final class Manipulator extends AbstractElementManipulator<JadeJsCodeBlockImpl> {
    @Override
    public JadeJsCodeBlockImpl handleContentChange(@NotNull JadeJsCodeBlockImpl element, @NotNull TextRange range, String newContent)
      throws IncorrectOperationException {
      ASTNode valueNode = element.getNode().getFirstChildNode();
      assert valueNode instanceof LeafElement;
      ((LeafElement)valueNode).replaceWithText(newContent);
      return element;
    }
  }
}
