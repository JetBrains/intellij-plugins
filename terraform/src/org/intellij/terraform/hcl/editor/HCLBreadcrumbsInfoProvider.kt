// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.editor

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty

open class HCLBreadcrumbsInfoProvider : BreadcrumbsProvider {
  override fun getLanguages(): Array<Language> {
    return arrayOf(HCLLanguage)
  }

  override fun acceptElement(e: PsiElement): Boolean {
    return e is HCLBlock || e is HCLProperty
  }

  override fun getElementInfo(e: PsiElement): String {
    if (e is HCLBlock) {
      return e.fullName
    }
    if (e is HCLProperty) {
      return e.name
    }
    throw AssertionError("Only HCLBlock and HCLProperty supported, actual is " + e.javaClass.name)
  }

  override fun getElementTooltip(e: PsiElement): String? {
    return null
  }
}
