// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.impl.HCLStringLiteralMixin

class AddClosingQuoteQuickFix(element: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
  companion object {
    private val LOG = Logger.getInstance(AddClosingQuoteQuickFix::class.java)
  }

  override fun getText(): String {
    return HCLBundle.message("hcl.literal.annotator.add.closing.quote.quick.fix.text")
  }

  override fun getFamilyName(): String {
    return text
  }

  override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
    if (!FileModificationService.getInstance().prepareFileForWrite(file)) return
    val rawText = startElement.text
    if (startElement !is HCLStringLiteralMixin) {
      LOG.error("Quick fix was applied to unexpected element", rawText, startElement.parent.text)
      return
    }
    if (rawText.isEmpty()) {
      LOG.error("Quick fix was applied to empty string element", rawText, startElement.parent.text)
      return
    }
    val content = HCLPsiUtil.stripQuotes(rawText)
    val quote = startElement.quoteSymbol
    CodeStyleManager.getInstance(project).performActionWithFormatterDisabled {
      startElement.updateText(quote + content + quote)
    }
  }
}