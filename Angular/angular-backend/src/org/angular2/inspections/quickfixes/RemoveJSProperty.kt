// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.openapi.project.Project
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class RemoveJSProperty(private val myPropertyName: String) : LocalQuickFix {

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.decorator.remove-property.name", myPropertyName)
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.decorator.remove-property.family")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val property = descriptor.psiElement.parent as? JSProperty
    if (property != null) {
      val parent = property.parent
      property.delete()
      FormatFixer.create(parent, FormatFixer.Mode.Reformat).fixFormat()
    }
  }
}
