// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.text.EditDistance
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.inspections.quickfixes.CreateAttributeQuickFix
import org.angular2.inspections.quickfixes.RenameAttributeQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.web.scopes.I18NAttributesScope.Companion.isI18nCandidate
import java.util.*

class AngularInvalidI18nAttributeInspection : AngularHtmlLikeTemplateLocalInspectionTool() {

  override fun visitAngularAttribute(holder: ProblemsHolder,
                                     attribute: XmlAttribute,
                                     descriptor: Angular2AttributeDescriptor) {
    val info = descriptor.info
    val parent = attribute.parent
    if (info.type == Angular2AttributeType.I18N && parent != null) {
      val range = attribute.nameElement.textRangeInParent
      val i18nedAttrName = info.name
      val candidates = TreeSet<String>()
      for (attr in parent.attributes) {
        val attrInfo = Angular2AttributeNameParser.parse(attr.name, parent)
        if (isI18nCandidate(attrInfo)) {
          candidates.add(Angular2AttributeType.I18N.buildName(attr.name))
        }
      }
      for (attr in parent.attributes) {
        candidates.remove(attr.name)
      }
      if (i18nedAttrName.isEmpty()) {
        val quickFixes = candidates.take(3)
          .map { RenameAttributeQuickFix(it) }
          .toTypedArray<LocalQuickFix>()

        @Suppress("DialogTitleCapitalization")
        holder.registerProblem(attribute, range,
                               Angular2Bundle.message("angular.inspection.i18n.message.empty"),
                               *quickFixes)
      }
      else if (descriptor.hasErrorSymbols) {
        val quickFixes = candidates
          .sortedWith { str1, str2 -> -EditDistance.levenshtein(str1, str2, false) }
          .take(2)
          .map { name -> RenameAttributeQuickFix(name) }
          .plus(CreateAttributeQuickFix(i18nedAttrName))
          .toTypedArray<LocalQuickFix>()

        holder.registerProblem(attribute, TextRange(range.startOffset + 5, range.endOffset),
                               Angular2Bundle.message("angular.inspection.i18n.message.not-matching", i18nedAttrName),
                               *quickFixes)
      }
    }
  }
}
