// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.xml.util.XmlTagUtil
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.NOT_REACHABLE
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.tags.Angular2ElementDescriptor
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.Angular2Bundle

class AngularUndefinedTagInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitXmlTag(holder: ProblemsHolder, tag: XmlTag) {
    val descriptor = tag.descriptor
    if (descriptor !is Angular2ElementDescriptor || descriptor.implied) {
      return
    }
    val scope = Angular2DeclarationsScope(tag)
    val matchedDirectives = Angular2ApplicableDirectivesProvider(tag, true).matched
    val proximity = scope.getDeclarationsProximity(matchedDirectives)
    if (proximity == IN_SCOPE) {
      return
    }
    val tagName = XmlTagUtil.getStartTagNameElement(tag) ?: return
    val quickFixes = SmartList<LocalQuickFix>()
    if (proximity != NOT_REACHABLE) {
      Angular2FixesFactory.addUnresolvedDeclarationFixes(tag, quickFixes)
    }
    holder.registerProblem(tagName,
                           Angular2Bundle.message("angular.inspection.undefined-tag.message.out-of-scope", tagName.text),
                           Angular2InspectionUtils.getBaseProblemHighlightType(scope, matchedDirectives),
                           *quickFixes.toTypedArray<LocalQuickFix>())
  }
}
