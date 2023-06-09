// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.xml.util.XmlTagUtil
import org.jetbrains.astro.AstroBundle
import org.jetbrains.astro.inspections.quickfixes.AstroImportComponentQuickFix
import org.jetbrains.astro.webSymbols.AstroProximity
import org.jetbrains.astro.webSymbols.AstroQueryConfigurator

class AstroMissingComponentImportInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return object : XmlElementVisitor() {
      override fun visitXmlTag(tag: XmlTag) {
        val descriptor = tag.descriptor
        if (descriptor !is WebSymbolElementDescriptor)
          return

        val symbol = descriptor.symbol
        if (symbol !is PsiSourcedWebSymbol
            || tag.localName != symbol.name
            || symbol.properties[AstroQueryConfigurator.PROP_ASTRO_PROXIMITY] != AstroProximity.OUT_OF_SCOPE)
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
