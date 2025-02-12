// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.HCLLanguage

internal class TfDocTargetProvider: BaseTfDocumentationProvider(), DocumentationTargetProvider {

  override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> {
    if (!file.language.`is`(HCLLanguage)) return mutableListOf()
    val element = file.findElementAt(offset) ?: return mutableListOf()

    return listOfNotNull(computeDocumentationTarget(element))
  }
}