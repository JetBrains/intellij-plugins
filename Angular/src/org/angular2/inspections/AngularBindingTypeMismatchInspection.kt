// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.javascript.webSymbols.jsType
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.JSEmptyExpression
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.validation.JSTypeChecker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.webSymbols.utils.qualifiedKind
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import org.angular2.codeInsight.Angular2HighlightingUtils
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.config.Angular2Compiler.isStrictTemplates
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.inspections.quickfixes.Angular2FixesFactory.getCreateInputTransformFixes
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2Interpolation
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.lang.types.BindingsTypeResolver
import org.angular2.web.NG_DIRECTIVE_ONE_TIME_BINDINGS

class AngularBindingTypeMismatchInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitAngularAttribute(
    holder: ProblemsHolder,
    attribute: XmlAttribute,
    descriptor: Angular2AttributeDescriptor,
  ) {
    val kind = descriptor.info.type
    when (kind) {
      Angular2AttributeType.REGULAR -> checkOneTimeBindingType(holder, attribute, descriptor)
      Angular2AttributeType.PROPERTY_BINDING -> checkPropertyBinding(holder, attribute, descriptor)
      Angular2AttributeType.TEMPLATE_BINDINGS -> checkTemplateBindings(holder, attribute, descriptor)
      else -> {}
    }
  }

  private fun checkOneTimeBindingType(holder: ProblemsHolder, attribute: XmlAttribute, descriptor: Angular2AttributeDescriptor) {
    val value = attribute.valueElement
    val isOneTimeBinding = descriptor.symbol.unwrapMatchedSymbols()
      .any { it.qualifiedKind == NG_DIRECTIVE_ONE_TIME_BINDINGS }
    if (isOneTimeBinding && isStrictTemplates(attribute)) {
      checkTypes(holder, attribute, descriptor, value?.value ?: "", BindingsTypeResolver.get(attribute.parent), true)
    }
  }

  private fun checkPropertyBinding(holder: ProblemsHolder, attribute: XmlAttribute, descriptor: Angular2AttributeDescriptor) {
    val expression = Angular2Binding.get(attribute)?.expression
    if ((expression == null || expression is JSEmptyExpression)
        && Angular2Interpolation.get(attribute).isEmpty()
        && isStrictTemplates(attribute)
    ) {
      checkTypes(holder, attribute, descriptor, null, BindingsTypeResolver.get(attribute.parent), false)
    }
    else if (isTemplateTag(attribute.parent)
             && (descriptor.info as Angular2AttributeNameParser.PropertyBindingInfo).bindingType == PropertyBindingType.PROPERTY
             && expression != null && expression !is JSEmptyExpression
             // Maybe a fake structural directive input
             && descriptor.sourceDirectives.filter { it.directiveKind.isStructural }
               .let { directives ->
                 directives.isNotEmpty() && directives.flatMap { it.inputs }
                   .none { it.name == descriptor.info.name }
               }
    ) {
      holder.registerProblem(attribute.nameElement ?: attribute,
                             Angular2Bundle.htmlMessage(
                               "angular.inspection.undefined-binding.message.embedded.property-not-provided",
                               descriptor.info.name.withColor(Angular2HighlightingUtils.TextAttributesKind.NG_INPUT, attribute)),
                             ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                             *getCreateInputTransformFixes(attribute, "string").toTypedArray())
    }
  }

  private fun checkTemplateBindings(holder: ProblemsHolder, attribute: XmlAttribute, descriptor: Angular2AttributeDescriptor) {
    val name = descriptor.info.name
    val bindings = Angular2TemplateBindings.get(attribute)
    val binding = bindings.bindings.find { it.key == name }
    if ((binding == null || binding.expression.let { it == null || it is JSEmptyExpression }) && isStrictTemplates(attribute)) {
      checkTypes(holder, attribute, descriptor, "", BindingsTypeResolver.get(bindings), false)
    }
  }

  private fun checkTypes(
    holder: ProblemsHolder, attribute: XmlAttribute, descriptor: Angular2AttributeDescriptor,
    value: String?, bindingsTypeResolver: BindingsTypeResolver, reportOnValue: Boolean,
  ) {
    withTypeEvaluationLocation(attribute) {
      val valueType = if (value != null)
        JSStringLiteralTypeImpl(value, true, JSTypeSource.EMPTY_TS_EXPLICITLY_DECLARED)
      else
        JSNamedTypeFactory.createUndefinedType(JSTypeSource.EMPTY_TS_EXPLICITLY_DECLARED)
      val symbolType = bindingsTypeResolver.substituteType(
        descriptor.sourceDirectives.firstOrNull(),
        descriptor.symbol.jsType
      )

      val highlightType = Angular2InspectionUtils.getTypeScriptInspectionHighlightType(attribute)

      JSTypeChecker.getErrorMessageIfTypeNotAssignableToType(attribute, symbolType, valueType, Angular2Language.optionHolder,
                                                             "javascript.type.is.not.assignable.to.type")
        ?.let {
          holder.registerProblem(attribute.valueElement?.takeIf { reportOnValue } ?: attribute.nameElement ?: attribute, it,
                                 highlightType,
                                 *getCreateInputTransformFixes(attribute, "string").toTypedArray())
        }
    }
  }
}