// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.html.polySymbols.elements.PolySymbolElementDescriptor
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.XmlTagUtil
import org.jetbrains.astro.AstroBundle
import org.jetbrains.astro.inspections.quickfixes.AstroImportComponentQuickFix
import org.jetbrains.astro.polySymbols.AstroProximity
import org.jetbrains.astro.polySymbols.PROP_ASTRO_PROXIMITY

class AstroMissingComponentImportInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return object : XmlElementVisitor() {
      override fun visitXmlTag(tag: XmlTag) {
        val descriptor = tag.descriptor
        if (descriptor !is PolySymbolElementDescriptor)
          return

        val symbol = descriptor.symbol
        if (symbol !is PsiSourcedPolySymbol
            || symbol[PROP_ASTRO_PROXIMITY] != AstroProximity.OUT_OF_SCOPE)
          return

        val elementToImport = symbol.source ?: return
        val tagNameToken = XmlTagUtil.getStartTagNameElement(tag) ?: return

        val tagName = tagNameToken.text
        holder.registerProblem(tagNameToken,
                               AstroBundle.message("astro.inspection.message.missing.component.import", tagName),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                               AstroImportComponentQuickFix(tagNameToken, tagName, elementToImport))
      }
    }
  }
}
