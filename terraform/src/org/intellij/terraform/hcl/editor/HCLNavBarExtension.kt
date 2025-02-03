// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.editor

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.PsiElement
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty
import javax.swing.Icon

internal class HCLNavBarExtension : StructureAwareNavBarModelExtension() {
  override fun getLeafElement(dataContext: DataContext): PsiElement? {
    val leafElement = super.getLeafElement(dataContext)
    if (leafElement is HCLObject) {
      return leafElement.parent
    }

    return leafElement
  }

  override fun getIcon(e: Any?): Icon? {
    if (e is HCLBlock || e is HCLProperty) {
      val hclElement = e as HCLElement
      return hclElement.presentation?.getIcon(false)
    }

    return null
  }

  override fun getPresentableText(e: Any?): String? {
    if (e is HCLBlock || e is HCLProperty) {
      val hclElement = e as HCLElement
      return hclElement.presentation?.presentableText
    }

    return null
  }

  override val language: Language
    get() = HCLLanguage

  override fun isAcceptableLanguage(psiElement: PsiElement?): Boolean {
    val lang = psiElement?.language ?: return false
    return lang is HCLLanguage || lang is TerraformLanguage
  }
}
