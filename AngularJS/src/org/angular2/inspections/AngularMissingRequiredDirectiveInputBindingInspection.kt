// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.XmlTagUtil
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.inspections.quickfixes.CreateAttributeQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.web.scopes.OneTimeBindingsProvider

class AngularMissingRequiredDirectiveInputBindingInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitXmlTag(holder: ProblemsHolder, tag: XmlTag) {
    val scope = Angular2DeclarationsScope(tag)
    if (scope.importsOwner == null) {
      return
    }
    val inputsToMatch = Angular2ApplicableDirectivesProvider(tag).matched
      .flatMap { d -> if (scope.contains(d)) d.inputs.filter { it.required }.map { Pair(it, d) } else emptyList() }
      .ifEmpty { return }

    val startTag = XmlTagUtil.getStartTagNameElement(tag)?.textRangeInParent ?: TextRange.EMPTY_RANGE

    val provided = tag.attributes.mapNotNullTo(HashSet()) { attr ->
      Angular2AttributeNameParser.parse(attr.name, tag)
        .takeIf {
          it.type == Angular2AttributeType.REGULAR ||
          (it is Angular2AttributeNameParser.PropertyBindingInfo &&
           (it.bindingType == PropertyBindingType.PROPERTY || it.bindingType == PropertyBindingType.ATTRIBUTE))
        }
        ?.name
    }

    inputsToMatch.filter { !provided.contains(it.first.name) }
      .forEach { (property, directive) ->
        val fixes = mutableListOf<LocalQuickFix>(CreateAttributeQuickFix("[" + property.name + "]"))
        if (OneTimeBindingsProvider.isOneTimeBindingProperty(property)) {
          fixes.add(CreateAttributeQuickFix(property.name))
        }
        holder.registerProblem(tag, startTag,
                               Angular2Bundle.message(
                                 if (directive.isComponent) "angular.inspection.missing-required-directive-input-binding.message.component"
                                 else "angular.inspection.missing-required-directive-input-binding.message",
                                 property.name, directive.getName()),
                               *fixes.toTypedArray())
      }

  }

  override fun visitAngularAttribute(holder: ProblemsHolder,
                                     attribute: XmlAttribute,
                                     descriptor: Angular2AttributeDescriptor) {
    if (descriptor.info.type == Angular2AttributeType.TEMPLATE_BINDINGS && !isTemplateTag(attribute.parent)) {
      val scope = Angular2DeclarationsScope(attribute)
      val templateBindings = Angular2TemplateBindings.get(attribute)
      val inputsToMatch = Angular2ApplicableDirectivesProvider(templateBindings).matched
        .flatMap { d -> if (scope.contains(d)) d.inputs.filter { it.required }.map { Pair(it, d) } else emptyList() }
        .ifEmpty { return }
      val provided = templateBindings.bindings.mapNotNullTo(HashSet()) {
        it.takeIf { !it.keyIsVar() && it.expression != null }
          ?.key
      }
      inputsToMatch.filter { !provided.contains(it.first.name) }
        .forEach {
          holder.registerProblem(attribute, attribute.nameElement.textRangeInParent,
                                 Angular2Bundle.message("angular.inspection.missing-required-directive-input-binding.message",
                                                        it.first.name, it.second.getName()))
        }
    }
  }
}
