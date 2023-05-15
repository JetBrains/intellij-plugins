// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElementVisitor
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.TEMPLATE_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_URL_PROP
import org.angular2.Angular2DecoratorUtil.getObjectLiteralInitializer
import org.angular2.Angular2DecoratorUtil.isAngularEntityDecorator
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.inspections.quickfixes.AddJSPropertyQuickFix
import org.angular2.inspections.quickfixes.RemoveJSProperty
import org.angular2.lang.Angular2Bundle

class AngularIncorrectTemplateDefinitionInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : JSElementVisitor() {

      override fun visitES6Decorator(decorator: ES6Decorator) {
        if (isAngularEntityDecorator(decorator, COMPONENT_DEC)) {
          val initializer = getObjectLiteralInitializer(decorator) ?: return
          val templateUrl = initializer.findProperty(TEMPLATE_URL_PROP)
          val template = initializer.findProperty(TEMPLATE_PROP)
          if (template == null && templateUrl == null) {
            val component = Angular2EntitiesProvider.getComponent(decorator)
            val name = component?.typeScriptClass?.name
            if (name != null) {
              holder.registerProblem(initializer,
                                     Angular2Bundle.message("angular.inspection.invalid-template-definition.message.missing", name),
                                     AddJSPropertyQuickFix(initializer, TEMPLATE_PROP, "\n\n", 1, true),
                                     AddJSPropertyQuickFix(initializer, TEMPLATE_URL_PROP, "./", 2, false))
            }
          }
          else if (template != null && templateUrl != null) {
            listOfNotNull(template.nameIdentifier, templateUrl.nameIdentifier)
              .forEach { id ->
                holder.registerProblem(
                  id, Angular2Bundle.message("angular.inspection.invalid-template-definition.message.duplicated"),
                  RemoveJSProperty(StringUtil.unquoteString(id.text)))
              }
          }
        }
      }
    }
  }
}
