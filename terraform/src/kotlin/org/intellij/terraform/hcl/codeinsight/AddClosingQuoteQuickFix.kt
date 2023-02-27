/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
    val element = startElement
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
    CodeStyleManager.getInstance(project).performActionWithFormatterDisabled {
      element.updateText(quote + content + quote)
    }
  }
}