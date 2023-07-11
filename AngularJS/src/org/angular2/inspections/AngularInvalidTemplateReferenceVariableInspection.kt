// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.analysis.RemoveAttributeIntentionFix
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.SmartList
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.entities.Angular2EntityUtils
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.parser.Angular2AttributeType

class AngularInvalidTemplateReferenceVariableInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitAngularAttribute(holder: ProblemsHolder,
                                     attribute: XmlAttribute,
                                     descriptor: Angular2AttributeDescriptor) {
    val info = descriptor.info
    if (info.type == Angular2AttributeType.REFERENCE) {
      val exportName = attribute.value
      if (!exportName.isNullOrEmpty()) {
        val matchedDirectives = Angular2ApplicableDirectivesProvider(attribute.parent).matched
        val allMatching = matchedDirectives.filter { dir -> dir.exportAs.contains(exportName) }
        val scope = Angular2DeclarationsScope(attribute)
        val matching = allMatching.filter { d -> scope.contains(d) }
        val range = TextRange(0, info.name.length)
          .shiftRight(attribute.name.length - info.name.length)
        if (matching.isEmpty()) {
          val quickFixes = SmartList<LocalQuickFix>()
          val proximity = scope.getDeclarationsProximity(allMatching)
          if (proximity != Angular2DeclarationsScope.DeclarationProximity.NOT_REACHABLE) {
            Angular2FixesFactory.addUnresolvedDeclarationFixes(attribute, quickFixes)
          }
          quickFixes.add(RemoveAttributeIntentionFix(attribute.name))
          holder.registerProblem(attribute.nameElement,
                                 Angular2Bundle.message("angular.inspection.invalid-template-ref-var.message.unbound", exportName),
                                 Angular2InspectionUtils.getBaseProblemHighlightType(scope, matchedDirectives),
                                 range,
                                 *quickFixes.toTypedArray<LocalQuickFix>())
        }
        else if (matching.size > 1) {
          holder.registerProblem(attribute.nameElement,
                                 range,
                                 Angular2Bundle.message("angular.inspection.invalid-template-ref-var.message.ambiguous-name", exportName,
                                                        Angular2EntityUtils.renderEntityList(matching)))
        }
      }
    }
  }
}
