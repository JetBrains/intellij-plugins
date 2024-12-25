// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public final class CfmlAttributeValueManipulator extends AbstractElementManipulator<CfmlCompositeElement> {
  @Override
  public CfmlCompositeElement handleContentChange(@NotNull CfmlCompositeElement expr, @NotNull TextRange range, String newContent) throws
                                                                                                                 IncorrectOperationException {
    PsiElement parent = expr.getParent();
    PsiFile psiFile = null;
    String oldText = expr.getText();
    String newText;
    if (StringUtil.startsWithChar(oldText, '\"')) {
      newText =
        oldText.substring(1, range.getStartOffset()) + newContent + oldText.substring(range.getEndOffset(), oldText.lastIndexOf("\""));
    }
    else {
      newText = oldText.substring(0, range.getStartOffset()) + newContent + oldText.substring(range.getEndOffset());
    }


    String templateText = "";
    if (parent != null && parent.getNode().getElementType() == CfmlElementTypes.INCLUDEEXPRESSION) {
      templateText = "<cfscript> cfinclude \"";
      psiFile =
        PsiFileFactory.getInstance(expr.getContainingFile().getProject()).createFileFromText("newElementFile.cfml", CfmlFileType.INSTANCE,
                                                                                             templateText + newText + "\"; </cfscript>");
    }

    else {
      parent = PsiTreeUtil.getParentOfType(expr, CfmlTag.class);
      if (parent != null) {
        templateText = "<cfinclude template=\"";
        psiFile =
          PsiFileFactory.getInstance(expr.getContainingFile().getProject()).createFileFromText("newElementFile.cfml", CfmlFileType.INSTANCE,
                                                                                               templateText + newText + "\">");
      }
    }
    if (psiFile != null) {
      PsiElement newStringElement = psiFile.findElementAt(templateText.length());
      if (newStringElement != null && newStringElement.getParent() != null) {
        return (CfmlCompositeElement)expr.replace(newStringElement.getParent());
      }
    }
    return expr;
  }

  @Override
  public @NotNull TextRange getRangeInElement(final @NotNull CfmlCompositeElement element) {
    return getValueRange(element);
  }

  private static TextRange getValueRange(CfmlCompositeElement element) {
    if (StringUtil.startsWithChar(element.getText(), '\"')) {
      return TextRange.allOf(element.getFirstChild().getNextSibling().getText()).shiftRight(1);
    }
    return TextRange.allOf(element.getText());
  }
}
