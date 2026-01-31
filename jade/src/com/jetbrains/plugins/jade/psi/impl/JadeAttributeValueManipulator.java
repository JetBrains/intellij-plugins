// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public final class JadeAttributeValueManipulator extends AbstractElementManipulator<JadeAttributeValueImpl> {

  @Override
  public JadeAttributeValueImpl handleContentChange(final @NotNull JadeAttributeValueImpl attrValue, final @NotNull TextRange range, final String newContent)
    throws IncorrectOperationException {
    if (!attrValue.isSyntheticValue()) {
      return handleContentChangeAttr(attrValue, range, newContent);
    }

    handleContentChangeSynthetic(attrValue, range, newContent);
    return attrValue;
  }

  private static JadeAttributeValueImpl handleContentChangeAttr(JadeAttributeValueImpl value, TextRange range, String content) {
    final String oldText = value.getText();
    final String newText = oldText.substring(0, range.getStartOffset()) + content + oldText.substring(range.getEndOffset());
    if (newText.isEmpty()) {
      return null;
    }

    String s = "dummy(a=" + newText + ")";
    PsiFile dummyFile =
      PsiFileFactory.getInstance(value.getProject()).createFileFromText(s, value.getContainingFile());
    if (dummyFile == null) {
      return null;
    }

    final PsiElement leafElement = dummyFile.findElementAt("dummy(a=".length());
    JadeAttributeValueImpl replacement = PsiTreeUtil.getParentOfType(leafElement, JadeAttributeValueImpl.class);
    if (replacement == null) {
      return null;
    }

    return ((JadeAttributeValueImpl)value.replace(replacement));
  }

  public static void handleContentChangeSynthetic(final JadeAttributeValueImpl attrValue, final TextRange range, final String newContent) {
    PsiElement element = attrValue;
    while (!(element instanceof LeafPsiElement)) {
      element = element.getLastChild();
    }

    final int deltaOffset = attrValue.getStartOffset() - ((LeafPsiElement)element).getStartOffset();
    ElementManipulators.handleContentChange(element, range.shiftRight(deltaOffset), newContent);
  }
}
