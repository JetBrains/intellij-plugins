// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.testIntegration.TestFinderHelper
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.PIPE_DEC
import org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement
import org.angular2.Angular2DecoratorUtil.isAngularEntityDecorator
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.Angular2EntityUtils.renderEntityList
import org.angular2.entities.Angular2FrameworkHandler
import org.angular2.entities.source.Angular2SourceModule
import org.angular2.lang.Angular2Bundle

class AngularMissingOrInvalidDeclarationInModuleInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : JSElementVisitor() {

      override fun visitES6Decorator(decorator: ES6Decorator) {
        if (isAngularEntityDecorator(decorator, COMPONENT_DEC, DIRECTIVE_DEC, PIPE_DEC)
            && !TestFinderHelper.isTest(decorator)) {
          val declaration = Angular2EntitiesProvider.getEntity(decorator) as? Angular2Declaration
          if (declaration != null) {
            if (declaration.isStandalone) {
              return
            }

            val modules = declaration.allDeclaringModules
            if (Angular2FrameworkHandler.EP_NAME.extensionList
                .any { h -> h.suppressModuleInspectionErrors(modules, declaration) }) {
              return
            }
            val classIdentifier = getClassForDecoratorElement(decorator)?.nameIdentifier ?: decorator
            if (modules.isEmpty()) {
              holder.registerProblem(classIdentifier,
                                     Angular2Bundle.message("angular.inspection.invalid-declaration-in-module.message.not-declared",
                                                            Angular2EntityUtils.getEntityClassName(decorator)),
                                     if (allSourceDeclarationsResolved(decorator.project))
                                       ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                     else
                                       ProblemHighlightType.WEAK_WARNING)
            }
            else if (modules.size > 1) {
              holder.registerProblem(classIdentifier,
                                     Angular2Bundle.message("angular.inspection.invalid-declaration-in-module.message.declared-in-many",
                                                            Angular2EntityUtils.getEntityClassName(decorator),
                                                            renderEntityList(modules)))
            }
          }
        }
      }
    }
  }


  private fun allSourceDeclarationsResolved(project: Project): Boolean {
    val modules = Angular2EntitiesProvider.getAllModules(project)
    return modules.all { m -> m !is Angular2SourceModule || m.areDeclarationsFullyResolved() }
  }
}
