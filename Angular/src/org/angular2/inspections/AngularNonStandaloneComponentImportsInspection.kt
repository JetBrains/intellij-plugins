// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.psi.PsiElementVisitor
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2ClassBasedEntity
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.inspections.quickfixes.ConvertToStandaloneNonStandaloneQuickFix
import org.angular2.inspections.quickfixes.RemoveJSProperty
import org.angular2.lang.Angular2Bundle

class AngularNonStandaloneComponentImportsInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : JSElementVisitor() {
      override fun visitES6Decorator(decorator: ES6Decorator) {
        if (Angular2DecoratorUtil.isAngularEntityDecorator(decorator, Angular2DecoratorUtil.COMPONENT_DEC)) {
          val initializer = Angular2DecoratorUtil.getObjectLiteralInitializer(decorator) ?: return
          val component = Angular2EntitiesProvider.getComponent(decorator).takeIf { it is Angular2ClassBasedEntity } ?: return
          val importsProperty = initializer.findProperty(Angular2DecoratorUtil.IMPORTS_PROP) ?: return
          if (!component.isStandalone) {
            holder.registerProblem(importsProperty.nameIdentifier ?: importsProperty,
                                   Angular2Bundle.message("angular.inspection.non-standalone-component-imports.message"),
                                   ConvertToStandaloneNonStandaloneQuickFix((component as Angular2ClassBasedEntity).className, true),
                                   RemoveJSProperty(Angular2DecoratorUtil.IMPORTS_PROP))
          }
        }
      }
    }
  }
}