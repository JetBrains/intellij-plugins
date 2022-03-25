// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.inspections

import com.intellij.codeInspection.DefaultXmlSuppressionProvider
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.xml.util.XmlDuplicatedIdInspection
import com.intellij.xml.util.XmlInvalidIdInspection
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueXmlSuppressionProvider : DefaultXmlSuppressionProvider() {

  companion object {
    val suppressedToolIds = listOf(
      XmlInvalidIdInspection::class, XmlDuplicatedIdInspection::class
    ).map { InspectionProfileEntry.getShortName(it.java.simpleName) }
  }

  override fun isProviderAvailable(file: PsiFile): Boolean = file.language.isKindOf(VueLanguage.INSTANCE)

  override fun isSuppressedFor(element: PsiElement, inspectionId: String): Boolean {
    return if (suppressedToolIds.contains(inspectionId)) true else super.isSuppressedFor(element, inspectionId)
  }
}