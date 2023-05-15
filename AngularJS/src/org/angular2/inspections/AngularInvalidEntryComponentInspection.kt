// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElementVisitor
import org.angular2.Angular2DecoratorUtil.BOOTSTRAP_PROP
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.ENTRY_COMPONENTS_PROP
import org.angular2.Angular2DecoratorUtil.MODULE_DEC
import org.angular2.Angular2DecoratorUtil.isAngularEntityDecorator
import org.angular2.entities.Angular2Component
import org.angular2.inspections.Angular2SourceEntityListValidator.ValidationResults
import org.angular2.lang.Angular2Bundle

class AngularInvalidEntryComponentInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : JSElementVisitor() {
      override fun visitES6Decorator(decorator: ES6Decorator) {
        if (isAngularEntityDecorator(decorator, MODULE_DEC, COMPONENT_DEC)) {
          val results = ValidationResults<ProblemType>()
          EntryComponentsValidator(decorator, results).validate()
          if (MODULE_DEC == decorator.decoratorName) {
            BootstrapValidator(decorator, results).validate()
          }
          results.registerProblems(ProblemType.INVALID_ENTRY_COMPONENT, holder)
        }
      }
    }
  }

  private enum class ProblemType {
    INVALID_ENTRY_COMPONENT
  }

  private class EntryComponentsValidator(decorator: ES6Decorator, results: ValidationResults<ProblemType>)
    : Angular2SourceEntityListValidator<Angular2Component, ProblemType>(
    decorator, results, Angular2Component::class.java, ENTRY_COMPONENTS_PROP) {

    override fun processNonAcceptableEntityClass(aClass: JSClass) {
      registerProblem(ProblemType.INVALID_ENTRY_COMPONENT,
                      Angular2Bundle.message("angular.inspection.invalid-entry-component.message.not-component",
                                             aClass.name!!))
    }
  }

  private class BootstrapValidator(decorator: ES6Decorator, results: ValidationResults<ProblemType>)
    : Angular2SourceEntityListValidator<Angular2Component, ProblemType>(
    decorator, results, Angular2Component::class.java, BOOTSTRAP_PROP) {

    override fun processNonAcceptableEntityClass(aClass: JSClass) {
      registerProblem(ProblemType.INVALID_ENTRY_COMPONENT,
                      Angular2Bundle.message("angular.inspection.invalid-entry-component.message.not-component",
                                             aClass.name!!))
    }

    override fun processAnyElement(node: JSElement) {
      registerProblem(ProblemType.INVALID_ENTRY_COMPONENT,
                      Angular2Bundle.message("angular.inspection.invalid-entry-component.message.not-array-of-class-types"))
    }
  }
}
