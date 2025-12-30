// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.project.Project
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.IMPORTS_PROP
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil.removeReferenceFromImportsList
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class MoveDeclarationOfStandaloneToImportsQuickFix(val className: String) : LocalQuickFix {

  @Nls
  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.standalone.move-to-imports.name", className)
  }

  @Nls
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.standalone.move-to-imports.family")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val reference = descriptor.psiElement
                      .asSafely<JSReferenceExpression>()
                      ?.takeIf { it.qualifier == null }
                    ?: return
    val referenceName = reference.referenceName ?: return

    val modulePtr = Angular2EntitiesProvider.getModule(reference.parentOfType<ES6Decorator>())?.createPointer()
                    ?: return

    removeReferenceFromImportsList(reference)

    val module = modulePtr.dereference() ?: return
    Angular2FixesPsiUtil.insertEntityDecoratorMember(module, IMPORTS_PROP, referenceName)
  }
}