// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.html.HtmlCompatibleFile
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlAttribute
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.Angular2DecoratorUtil.getExpressionStringValue
import org.angular2.Angular2DecoratorUtil.getObjectLiteralInitializer
import org.angular2.Angular2DecoratorUtil.isAngularEntityDecorator
import org.angular2.inspections.quickfixes.AddJSPropertyQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.ATTR_SELECT
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.ELEMENT_NG_CONTENT

class AngularInvalidSelectorInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    if (holder.file is HtmlCompatibleFile) {
      return object : XmlElementVisitor() {
        override fun visitXmlAttribute(attribute: XmlAttribute) {
          if (attribute.name == ATTR_SELECT
              && attribute.parent.name == ELEMENT_NG_CONTENT) {
            val value = attribute.valueElement ?: return
            try {
              Angular2DirectiveSimpleSelector.parse(value.value)
            }
            catch (e: Angular2DirectiveSimpleSelector.ParseException) {
              holder.registerProblem(value,
                                     e.errorRange.shiftRight(value.text.indexOf(value.value)),
                                     e.message!!)
            }
          }
        }
      }
    }
    else if (DialectDetector.isTypeScript(holder.file) && Angular2LangUtil.isAngular2Context(holder.file)) {
      return object : JSElementVisitor() {

        override fun visitES6Decorator(decorator: ES6Decorator) {
          if (isAngularEntityDecorator(decorator, COMPONENT_DEC, DIRECTIVE_DEC)) {
            val initializer = getObjectLiteralInitializer(decorator) ?: return
            val selector = initializer.findProperty(SELECTOR_PROP)
            if (selector == null) {
              if (DIRECTIVE_DEC == decorator.decoratorName) {
                holder.registerProblem(initializer,
                                       Angular2Bundle.message("angular.inspection.invalid-directive-selector.message.missing"),
                                       AddJSPropertyQuickFix(initializer, SELECTOR_PROP, "", 0, false))
              }
            }
            else {
              val text = getExpressionStringValue(selector.value) ?: return
              try {
                Angular2DirectiveSimpleSelector.parse(text)
              }
              catch (e: Angular2DirectiveSimpleSelector.ParseException) {
                holder.registerProblem(selector.value!!,
                                       e.errorRange.shiftRight(1),
                                       e.message!!)
              }
            }
          }
        }
      }
    }
    return PsiElementVisitor.EMPTY_VISITOR
  }
}
