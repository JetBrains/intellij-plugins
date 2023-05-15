// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlTag
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class RemoveTagContentQuickFix : LocalQuickFix {
  @Nls
  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.template.remove-tag-content.name")
  }

  @Nls
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.template.remove-tag-content.family")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val tag = descriptor.psiElement as XmlTag
    val content = tag.value.children
    if (content.isNotEmpty()) {
      tag.deleteChildRange(content[0], content[content.size - 1])
    }
  }
}
