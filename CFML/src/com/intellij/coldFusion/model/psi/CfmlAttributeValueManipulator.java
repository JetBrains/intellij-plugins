/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 12/30/11
 */
public class CfmlAttributeValueManipulator extends AbstractElementManipulator<CfmlCompositeElement> {
  @Override
  public CfmlCompositeElement handleContentChange(CfmlCompositeElement expr, TextRange range, String newContent) throws
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
        PsiFileFactory.getInstance(expr.getContainingFile().getProject()).createFileFromText("newElementFile.cfml",
                                                                                             templateText + newText + "\"; </cfscript>");
    }

    else {
      parent = PsiTreeUtil.getParentOfType(expr, CfmlTag.class);
      if (parent != null) {
        templateText = "<cfinclude template=\"";
        psiFile =
          PsiFileFactory.getInstance(expr.getContainingFile().getProject()).createFileFromText("newElementFile.cfml",
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
  public TextRange getRangeInElement(final CfmlCompositeElement element) {
    return getValueRange(element);
  }

  private static TextRange getValueRange(CfmlCompositeElement element) {
    if (StringUtil.startsWithChar(element.getText(), '\"')) {
      return TextRange.allOf(element.getFirstChild().getNextSibling().getText()).shiftRight(1);
    }
    return TextRange.allOf(element.getText());
  }
}
