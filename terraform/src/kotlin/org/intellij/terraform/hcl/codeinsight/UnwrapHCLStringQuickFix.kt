// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLElementGenerator
import org.intellij.terraform.hcl.psi.HCLExpression
import org.intellij.terraform.hcl.psi.HCLStringLiteral

class UnwrapHCLStringQuickFix(element: HCLStringLiteral) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
  override fun getText() = HCLBundle.message("hil.literal.annotator.unwrap.string.quick.fix.text")
  override fun getFamilyName() = text
  override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
    val element = startElement as? HCLStringLiteral ?: return
    val key: HCLExpression
    try {
      key = HCLElementGenerator(project).createPropertyKey(element.value)
    } catch (e: IllegalStateException) {
      return
    }
    element.replace(key)
  }
}