// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.template.Template
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.entities.Angular2ClassBasedEntity
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE
import org.angular2.lang.expr.psi.Angular2Binding

class CreateDirectiveInputIntentionAction(xmlAttribute: XmlAttribute, referenceName: String)
  : BaseCreateDirectiveInputOutputAction(xmlAttribute, referenceName) {

  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.template.create-input.name", myReferencedName)
  }

  override fun buildTemplate(template: Template,
                             referenceExpression: JSReferenceExpression?,
                             isStaticContext: Boolean,
                             anchorParent: PsiElement) {
    addDecoratedField(template, INPUT_DEC)
    template.addTextSegment("!: ")
    addTypeSegment(template)
    addSemicolonSegment(template, anchorParent)

    Angular2FixesPsiUtil.insertJSImport(anchorParent, ANGULAR_CORE_PACKAGE, INPUT_DEC)
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