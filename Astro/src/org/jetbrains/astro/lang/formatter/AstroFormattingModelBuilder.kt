// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.lang.xml.XmlFormattingModel
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.xml.XmlTag

class AstroFormattingModelBuilder : FormattingModelBuilder {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val psiFile = formattingContext.containingFile
    val documentModel = FormattingDocumentModelImpl.createOn(psiFile)
    val element = formattingContext.psiElement
    val settings = formattingContext.codeStyleSettings
    return if (element is XmlTag) {
      XmlFormattingModel(
        psiFile,
        AstroTagBlock(element.node, null, null, AstroFormattingPolicy(settings, documentModel), null, false),
        documentModel)
    }
    else {
      XmlFormattingModel(
        psiFile,
        AstroBlock(psiFile.node, null, null, AstroFormattingPolicy(settings, documentModel), null, null, false),
        documentModel)
    }
  }
}