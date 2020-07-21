// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.lang.xml.XmlFormattingModel
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.html.VueFileType

class VueFormattingModelBuilder : FormattingModelBuilder {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val psiFile = formattingContext.containingFile
    val vueFile = psiFile.originalFile.virtualFile?.let { it.fileType === VueFileType.INSTANCE } ?: true
    val documentModel = FormattingDocumentModelImpl.createOn(psiFile)
    val element = formattingContext.psiElement
    val settings = formattingContext.codeStyleSettings
    return if (element is XmlTag) {
      XmlFormattingModel(
        psiFile,
        VueHtmlTagBlock(element.node, null, null, HtmlPolicy(settings, documentModel),
                        null, false),
        documentModel)
    }
    else {
      XmlFormattingModel(
        psiFile,
        if (vueFile)
          VueBlock(psiFile.node, null, null, VueRootFormattingPolicy(settings, documentModel),
                   null, null, false)
        else
          VueHtmlBlock(psiFile.node, null, null, HtmlPolicy(settings, documentModel),
                       null, null, false),
        documentModel)
    }
  }
}