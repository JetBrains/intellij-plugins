// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html;

import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.lang.xml.XmlFormattingModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.xml.HtmlPolicy;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import org.angular2.lang.html.psi.formatter.Angular2HtmlBlock;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlFormattingModelBuilder implements FormattingModelBuilder {

  @Override
  public @NotNull FormattingModel createModel(final PsiElement element, final CodeStyleSettings settings) {
    final PsiFile psiFile = element.getContainingFile();
    final FormattingDocumentModelImpl documentModel = FormattingDocumentModelImpl.createOn(psiFile);
    return new XmlFormattingModel(
      psiFile,
      new Angular2HtmlBlock(SourceTreeToPsiMap.psiElementToTree(psiFile), null, null,
                            new HtmlPolicy(settings, documentModel), null, null, false),
      documentModel);
  }
}
