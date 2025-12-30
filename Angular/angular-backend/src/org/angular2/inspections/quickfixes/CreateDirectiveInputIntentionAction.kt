// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.entities.Angular2ClassBasedEntity
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.signals.Angular2SignalUtils

enum class InputKind {
  DECORATOR, SIGNAL, MODEL, SIGNAL_REQUIRED, MODEL_REQUIRED
}

class CreateDirectiveInputIntentionAction(xmlAttribute: XmlAttribute, referenceName: String, val kind: InputKind)
  : BaseCreateDirectiveInputOutputAction(xmlAttribute, referenceName) {

  override fun getName(): String {
    return when (kind) {
      InputKind.DECORATOR -> Angular2Bundle.message("angular.quickfix.template.create-input.name", myReferencedName)
      InputKind.SIGNAL -> Angular2Bundle.message("angular.quickfix.template.create-signal-input.name", myReferencedName)
      InputKind.MODEL -> Angular2Bundle.message("angular.quickfix.template.create-model.name", myReferencedName)
      InputKind.SIGNAL_REQUIRED -> Angular2Bundle.message("angular.quickfix.template.create-required-input.name", myReferencedName)
      InputKind.MODEL_REQUIRED -> Angular2Bundle.message("angular.quickfix.template.create-required-model.name", myReferencedName)
    }
  }

  private fun addSignalOrModelTemplate(
    template: Template,
    anchorParent: PsiElement,
    functionName: String,
    required: Boolean = false,
  ) {
    val type = inferType(myElementPointer.element)
    val defaultValue = type?.defaultValue
    template.addTextSegment(myReferencedName)
    template.addTextSegment(" = $functionName")

    if (required) {
      template.addTextSegment(".required")
    }

    if ((type == null || type.substitute().asSafely<JSPrimitiveType>()?.isPrimitive == true) && !required) {
      template.addTextSegment("(")
    }
    else {
      val typeText = type?.substitute()?.asSafely<JSPrimitiveType>()?.let {
        if (it.isPrimitive) it.getPrimitiveTypeText() else it.getClassTypeText()
      } ?: type?.getTypeText(JSType.TypeTextFormat.CODE) ?: "any"
      val expression = ConstantNode(typeText)
      template.addTextSegment("<")
      template.addVariable("__type", expression, expression, false)
      template.addTextSegment(">(")
    }

    if (!required) {
      val expression: Expression = if (defaultValue != null) ConstantNode(defaultValue) else ConstantNode("undefined")
      template.addVariable("\$INITIAL_VALUE$", expression, expression, true)
    }
    template.addTextSegment(")")

    addSemicolonSegment(template, anchorParent)
  }

  override fun buildTemplate(
    template: Template,
    referenceExpression: JSReferenceExpression?,
    isStaticContext: Boolean,
    anchorParent: PsiElement,
  ) {
    when (kind) {
      InputKind.MODEL -> {
        addSignalOrModelTemplate(template, anchorParent, "model")
        Angular2FixesPsiUtil.insertJSImport(anchorParent, ANGULAR_CORE_PACKAGE, Angular2SignalUtils.MODEL_FUN)
      }
      InputKind.MODEL_REQUIRED -> {
        addSignalOrModelTemplate(template, anchorParent, "model", true)
        Angular2FixesPsiUtil.insertJSImport(anchorParent, ANGULAR_CORE_PACKAGE, Angular2SignalUtils.MODEL_FUN)
      }
      InputKind.SIGNAL -> {
        addSignalOrModelTemplate(template, anchorParent, "input")
        Angular2FixesPsiUtil.insertJSImport(anchorParent, ANGULAR_CORE_PACKAGE, Angular2SignalUtils.INPUT_FUN)
      }
      InputKind.SIGNAL_REQUIRED -> {
        addSignalOrModelTemplate(template, anchorParent, "input", true)
        Angular2FixesPsiUtil.insertJSImport(anchorParent, ANGULAR_CORE_PACKAGE, Angular2SignalUtils.INPUT_FUN)
      }
      else -> {
        addDecoratedField(template, INPUT_DEC)
        template.addTextSegment("!: ")
        addTypeSegment(template)
        addSemicolonSegment(template, anchorParent)

        Angular2FixesPsiUtil.insertJSImport(anchorParent, ANGULAR_CORE_PACKAGE, INPUT_DEC)
      }
    }
  }

  override fun inferType(context: PsiElement?): JSType? =
    when (context) {
      is XmlAttribute -> Angular2Binding.get(context)?.expression?.let { JSResolveUtil.getExpressionJSType(it) }
      else -> null
    }

  override fun getTargetClasses(context: XmlAttribute): List<TypeScriptClass> {
    // Add directive matching the property-related selector
    val info = context.descriptor.asSafely<Angular2AttributeDescriptor>()?.info?.name
    return if (info == null)
      super.getTargetClasses(context)
    else
      super.getTargetClasses(context)
        .asSequence()
        .plus(
          context.parent?.descriptor?.getAttributeDescriptor(
            info,
            context.parent)
            ?.asSafely<Angular2AttributeDescriptor>()
            ?.sourceDirectives
            ?.filterIsInstance<Angular2ClassBasedEntity>()
            ?.mapNotNull { it.typeScriptClass }
          ?: emptyList()
        )
        .distinct()
        .toList()
  }
}
