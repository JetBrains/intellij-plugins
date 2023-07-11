// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.analysis.RemoveAttributeIntentionFix
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.SmartList
import com.intellij.util.containers.MultiMap
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.NOT_REACHABLE
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.entities.Angular2Directive
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2Bundle.Companion.BUNDLE
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo
import org.angular2.lang.html.parser.Angular2AttributeType.*
import org.angular2.lang.html.psi.PropertyBindingType
import org.jetbrains.annotations.PropertyKey

class AngularUndefinedBindingInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitAngularAttribute(holder: ProblemsHolder,
                                     attribute: XmlAttribute,
                                     descriptor: Angular2AttributeDescriptor) {
    val info = descriptor.info
    val templateTag = isTemplateTag(attribute.parent)
    if (info.type == TEMPLATE_BINDINGS) {
      if (templateTag) {
        return
      }
      visitTemplateBindings(holder, attribute, Angular2TemplateBindings.get(attribute))
      return
    }
    else if (info.type == REFERENCE || info.type == LET || info.type == I18N || info.type == PROPERTY_BINDING && (info as PropertyBindingInfo).bindingType != PropertyBindingType.PROPERTY) {
      return
    }
    val scope = Angular2DeclarationsScope(attribute)
    val proximity: DeclarationProximity
    val matchedDirectives = Angular2ApplicableDirectivesProvider(attribute.parent).matched
    if (descriptor.hasErrorSymbols) {
      proximity = NOT_REACHABLE
    }
    else if (descriptor.hasNonDirectiveSymbols) {
      if (templateTag) {
        proximity = NOT_REACHABLE
      }
      else {
        return
      }
    }
    else {
      proximity = scope.getDeclarationsProximity(
        descriptor.sourceDirectives.filter { matchedDirectives.contains(it) })
    }
    if (proximity == IN_SCOPE) {
      return
    }
    val quickFixes = SmartList<LocalQuickFix>()
    if (proximity != NOT_REACHABLE) {
      Angular2FixesFactory.addUnresolvedDeclarationFixes(attribute, quickFixes)
    }
    quickFixes.add(RemoveAttributeIntentionFix(attribute.name))
    var severity = Angular2InspectionUtils.getBaseProblemHighlightType(scope, matchedDirectives)
    @PropertyKey(resourceBundle = BUNDLE) val messageKey: String =
      when (info.type) {
        EVENT -> {
          if (templateTag)
            "angular.inspection.undefined-binding.message.embedded.event-not-emitted"
          else
            "angular.inspection.undefined-binding.message.event-not-emitted"
        }
        PROPERTY_BINDING -> {
          if (templateTag)
            "angular.inspection.undefined-binding.message.embedded.property-not-provided"
          else
            "angular.inspection.undefined-binding.message.property-not-provided"
        }
        BANANA_BOX_BINDING ->
          "angular.inspection.undefined-binding.message.banana-box-binding-not-provided"
        REGULAR -> {
          severity = ProblemHighlightType.WARNING
          if (proximity === NOT_REACHABLE)
            "angular.inspection.undefined-binding.message.unknown-attribute"
          else
            "angular.inspection.undefined-binding.message.attribute-directive-out-of-scope"
        }
        else -> return
      }
    // TODO register error on the symbols themselves
    holder.registerProblem(attribute.nameElement,
                           Angular2Bundle.message(messageKey, info.name, attribute.parent.name),
                           severity,
                           *quickFixes.toTypedArray<LocalQuickFix>())
  }

  private fun visitTemplateBindings(holder: ProblemsHolder,
                                    attribute: XmlAttribute,
                                    bindings: Angular2TemplateBindings) {
    val matched = Angular2ApplicableDirectivesProvider(bindings).matched
    val scope = Angular2DeclarationsScope(attribute)
    var proximity = scope.getDeclarationsProximity(matched)
    if (proximity != IN_SCOPE) {
      val fixes = SmartList<LocalQuickFix>()
      Angular2FixesFactory.addUnresolvedDeclarationFixes(bindings, fixes)
      holder.registerProblem(attribute.nameElement,
                             Angular2Bundle.message("angular.inspection.undefined-binding.message.embedded.no-directive-matched",
                                                    bindings.templateName),
                             ProblemHighlightType.WEAK_WARNING,
                             *fixes.toTypedArray<LocalQuickFix>())
      return
    }

    val input2DirectiveMap = MultiMap<String, Angular2Directive>()
    matched.forEach { dir -> dir.inputs.forEach { input -> input2DirectiveMap.putValue(input.name, dir) } }

    for (binding in bindings.bindings) {
      if (!binding.keyIsVar() && binding.expression != null) {
        proximity = scope.getDeclarationsProximity(input2DirectiveMap.get(binding.key))
        if (proximity != IN_SCOPE) {
          val element = binding.keyElement ?: attribute.nameElement
          val fixes = SmartList<LocalQuickFix>()
          Angular2FixesFactory.addUnresolvedDeclarationFixes(binding, fixes)
          holder.registerProblem(element,
                                 Angular2Bundle.message("angular.inspection.undefined-binding.message.embedded.property-not-provided",
                                                        binding.key),
                                 Angular2InspectionUtils.getBaseProblemHighlightType(scope, matched),
                                 *fixes.toTypedArray<LocalQuickFix>())
        }
      }
    }
  }
}
