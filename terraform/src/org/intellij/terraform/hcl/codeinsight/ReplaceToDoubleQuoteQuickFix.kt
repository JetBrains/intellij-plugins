// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLElementGenerator
import org.intellij.terraform.hcl.psi.HCLStringLiteral

class ReplaceToDoubleQuoteQuickFix(element: HCLStringLiteral) : PsiUpdateModCommandAction<HCLStringLiteral>(element) {
  override fun getFamilyName(): String = HCLBundle.message("hcl.literal.inspection.replace.quotes.quick.fix.text")

  override fun invoke(context: ActionContext, element: HCLStringLiteral, updater: ModPsiUpdater) {
    element.replace(HCLElementGenerator(context.project).createStringLiteral(element.value))
  }
}