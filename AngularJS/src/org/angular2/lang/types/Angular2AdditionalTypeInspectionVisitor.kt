// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.types

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.javascript.webSymbols.jsType
import com.intellij.lang.javascript.psi.JSEmptyExpression
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import com.intellij.lang.javascript.validation.JSTypeChecker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.config.isStrictTemplates
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.web.Angular2WebSymbolsQueryConfigurator

class Angular2AdditionalTypeInspectionVisitor(private val holder: ProblemsHolder) : Angular2HtmlElementVisitor() {

  override fun visitXmlAttribute(attribute: XmlAttribute) {
    val descriptor = attribute.descriptor as? Angular2AttributeDescriptor ?: return

    val kind = descriptor.info.type

    when (kind) {
      Angular2AttributeType.REGULAR -> checkOneTimeBindingType(attribute, descriptor)
      Angular2AttributeType.PROPERTY_BINDING -> checkPropertyBinding(attribute, descriptor)
      Angular2AttributeType.TEMPLATE_BINDINGS -> checkTemplateBindings(attribute, descriptor)
      else -> {}
    }
    super.visitXmlAttribute(attribute)
  }

  private fun checkOneTimeBindingType(attribute: XmlAttribute, descriptor: Angular2AttributeDescriptor) {
    val value = attribute.valueElement
    val isOneTimeBinding = descriptor.symbol.unwrapMatchedSymbols()
      .any { it.kind == Angular2WebSymbolsQueryConfigurator.KIND_NG_DIRECTIVE_ONE_TIME_BINDINGS }
    if (isOneTimeBinding && isStrictTemplates(attribute)) {
      checkTypes(attribute, descriptor, value?.value ?: "", BindingsTypeResolver.get(attribute.parent), true)
    }
  }

  private fun checkPropertyBinding(attribute: XmlAttribute, descriptor: Angular2AttributeDescriptor) {
    val expression = Angular2Binding.get(attribute)?.expression
    if ((expression == null || expression is JSEmptyExpression) && isStrictTemplates(attribute)) {
      checkTypes(attribute, descriptor, null, BindingsTypeResolver.get(attribute.parent), false)
    }
  }

  private fun checkTemplateBindings(attribute: XmlAttribute, descriptor: Angular2AttributeDescriptor) {
    val name = descriptor.info.name
    val bindings = Angular2TemplateBindings.get(attribute)
    val binding = bindings.bindings.find { it.key == name }
    if ((binding == null || binding.expression.let { it == null || it is JSEmptyExpression }) && isStrictTemplates(attribute)) {
      checkTypes(attribute, descriptor, "", BindingsTypeResolver.get(bindings), false)
    }
  }

  private fun checkTypes(attribute: XmlAttribute, descriptor: Angular2AttributeDescriptor,
                         value: String?, bindingsTypeResolver: BindingsTypeResolver, reportOnValue: Boolean) {
    val valueType = if (value != null )
      JSStringLiteralTypeImpl(value, true, JSTypeSource.EMPTY_TS_EXPLICITLY_DECLARED)
    else
      JSUndefinedType(JSTypeSource.EMPTY_TS_EXPLICITLY_DECLARED)
    val symbolType = bindingsTypeResolver.substituteType(
      descriptor.sourceDirectives.firstOrNull(),
      descriptor.symbol.jsType
    )
    JSTypeChecker.getErrorMessageIfTypeNotAssignableToType(attribute, symbolType, valueType, Angular2Language.INSTANCE.optionHolder,
                                                           "javascript.type.is.not.assignable.to.type")
      ?.let {
        holder.registerProblem(attribute.valueElement?.takeIf { reportOnValue } ?: attribute.nameElement ?: attribute, it)
      }
  }

}