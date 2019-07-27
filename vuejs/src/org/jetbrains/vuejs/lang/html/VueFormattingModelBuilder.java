// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html;

import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.lang.xml.XmlFormattingModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.xml.HtmlPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.vuejs.lang.html.psi.formatter.VueHtmlBlock;

public class VueFormattingModelBuilder implements FormattingModelBuilder {

  @Override
  @NotNull
  public FormattingModel createModel(final PsiElement element, final CodeStyleSettings settings) {
    final PsiFile psiFile = element.getContainingFile();
    final FormattingDocumentModelImpl documentModel = FormattingDocumentModelImpl.createOn(psiFile);
    return new XmlFormattingModel(
      psiFile,
      new VueHtmlBlock(psiFile.getNode(), null, null,
                       new HtmlPolicy(settings, documentModel),
                       null, null, false),
      documentModel);
  }
}
