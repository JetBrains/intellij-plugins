// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.util.parentsOfType
import com.intellij.util.concurrency.annotations.RequiresReadLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.documentation.TerraformWebDocUrlProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement

internal class TerraformDocumentPsi(val element: PsiElement,
                                    private val text: String) : FakePsiElement(), HCLElement {

  private val docUrlProvider: TerraformWebDocUrlProvider = element.project.service<TerraformWebDocUrlProvider>()

  @RequiresReadLock
  override fun getParent(): PsiElement {
    return element.parentsOfType<HCLBlock>(true).first()
  }

  override fun navigate(requestFocus: Boolean) {
    docUrlProvider.coroutineScope.launch {
      val docUrl = docUrlProvider.getDocumentationUrl(readAction { parent }).firstOrNull()
      withContext(Dispatchers.Main) {
        docUrl?.let { BrowserUtil.browse(it) }
      }
    }
  }

  override fun getPresentableText(): String {
    return text
  }

  override fun getName(): String {
    return text
  }


}
