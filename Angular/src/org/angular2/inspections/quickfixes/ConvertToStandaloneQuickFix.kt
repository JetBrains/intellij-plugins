// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.openapi.project.Project
import com.intellij.psi.util.parentOfTypes
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.STANDALONE_PROP
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class ConvertToStandaloneQuickFix(private val className: String) : LocalQuickFix {

  @Nls
  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.standalone.convert-to-standalone.name", className)
  }

  @Nls
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.standalone.convert-to-standalone.family")
  }

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val decorator = when (val element = descriptor.psiElement) {
                      is JSReferenceExpression -> Angular2EntitiesProvider.getEntity(element.resolve())
                      else -> Angular2EntitiesProvider.getEntity(element.parentOfTypes(ES6Decorator::class, TypeScriptClass::class))
                    }?.decorator ?: return

    val objectLiteral = Angular2DecoratorUtil.getObjectLiteralInitializer(decorator) ?: return
    Angular2FixesPsiUtil.insertJSObjectLiteralProperty(objectLiteral, STANDALONE_PROP, "true")
  }
}
