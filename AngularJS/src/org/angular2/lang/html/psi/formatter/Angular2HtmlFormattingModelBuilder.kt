// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.lang.xml.XmlFormattingModel
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.impl.source.SourceTreeToPsiMap

class Angular2HtmlFormattingModelBuilder : FormattingModelBuilder {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val psiFile = formattingContext.containingFile
    val documentModel = FormattingDocumentModelImpl.createOn(psiFile)
    return XmlFormattingModel(
      psiFile,
      Angular2HtmlFormattingBlock(SourceTreeToPsiMap.psiElementToTree(psiFile), null, null,
                                  HtmlPolicy(formattingContext.codeStyleSettings, documentModel), null, null, false),
      documentModel)
  }
}