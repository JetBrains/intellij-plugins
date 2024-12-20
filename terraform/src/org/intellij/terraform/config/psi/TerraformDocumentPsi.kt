// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi

import com.intellij.ide.BrowserUtil
import com.intellij.lang.Language
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.util.parentsOfType
import org.intellij.terraform.config.documentation.TerraformWebDocUrlProvider
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement

internal class TerraformDocumentPsi(val element: PsiElement,
                                    private val text: String) : FakePsiElement(), HCLElement {

  private val parentElement: HCLBlock? = element.parentsOfType<HCLBlock>(true).firstOrNull()

  override fun getParent(): PsiElement = parentElement as PsiElement

  override fun getLanguage(): Language = HCLLanguage

  override fun navigate(requestFocus: Boolean) {
    parentElement ?: return

    runWithModalProgressBlocking(project, HCLBundle.message("progress.title.opening.terraform.documentation")) {
      val url = TerraformWebDocUrlProvider.getDocumentationUrl(parentElement.createSmartPointer()).firstOrNull()
      url?.let { BrowserUtil.browse(it) }
    }
  }

  override fun setName(name: String): PsiElement {
    val manipulator = ElementManipulators.getManipulator(element) ?: return this
    val element = manipulator.handleContentChange(element, name) ?: return this
    return TerraformDocumentPsi(element, name)
  }

  override fun getPresentableText(): String {
    return text
  }

  override fun getName(): String {
    return text
  }
}
