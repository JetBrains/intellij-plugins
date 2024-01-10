// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class ConvertToPropertyBindingQuickFix(private val myPropertyName: String) : LocalQuickFix {

  @Nls
  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.template.bind-to-property.name", myPropertyName)
  }

  @Nls
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.template.bind-to-property.family")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val attribute = descriptor.psiElement.parent as? XmlAttribute
    attribute?.setName("[$myPropertyName]")
  }
}
