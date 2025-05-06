// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLElementGenerator
import org.intellij.terraform.hcl.psi.HCLExpression
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.jetbrains.annotations.Nls

class UnwrapHCLStringQuickFix(element: HCLStringLiteral) : PsiUpdateModCommandAction<HCLStringLiteral>(element) {
  override fun getFamilyName(): @Nls String = HCLBundle.message("hil.literal.annotator.unwrap.string.quick.fix.text")
  override fun invoke(context: ActionContext, element: HCLStringLiteral, updater: ModPsiUpdater) {
    val key: HCLExpression
    try {
      key = HCLElementGenerator(context.project).createPropertyKey(element.value)
    } catch (e: IllegalStateException) {
      return
    }
    element.replace(key)
  }
}