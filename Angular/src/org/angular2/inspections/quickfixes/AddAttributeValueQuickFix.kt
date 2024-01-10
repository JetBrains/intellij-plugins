// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class AddAttributeValueQuickFix : LocalQuickFix {
  @Nls
  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.template.add-attribute-value.name")
  }

  @Nls
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.template.add-attribute-value.family")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val attribute = descriptor.psiElement as XmlAttribute
    attribute.setValue("")
    val value = attribute.valueElement
    if (value != null) {
      PsiNavigationSupport.getInstance()
        .createNavigatable(
          project, attribute.containingFile.virtualFile,
          value.textRange.startOffset + 1
        )
        .navigate(true)
    }
  }
}
