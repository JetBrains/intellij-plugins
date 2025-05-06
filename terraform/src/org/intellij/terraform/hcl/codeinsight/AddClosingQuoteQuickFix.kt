// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.diagnostic.Logger
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.impl.HCLStringLiteralMixin

class AddClosingQuoteQuickFix(element: HCLStringLiteral) : PsiUpdateModCommandAction<HCLStringLiteral>(element) {
  companion object {
    private val LOG = Logger.getInstance(AddClosingQuoteQuickFix::class.java)
  }

  override fun getFamilyName(): String = HCLBundle.message("hcl.literal.inspection.add.closing.quote.quick.fix.text")

  override fun invoke(context: ActionContext, element: HCLStringLiteral, updater: ModPsiUpdater) {
    val rawText = element.text
    if (element !is HCLStringLiteralMixin) {
      LOG.error("Quick fix was applied to unexpected element", rawText, element.parent.text)
      return
    }
    if (rawText.isEmpty()) {
      LOG.error("Quick fix was applied to empty string element", rawText, element.parent.text)
      return
    }
    val content = HCLPsiUtil.stripQuotes(rawText)
    val quote = element.quoteSymbol
    val document = element.containingFile.fileDocument
    val textRange = element.textRange
    document.replaceString(textRange.startOffset, textRange.endOffset, quote + content + quote)
  }
}