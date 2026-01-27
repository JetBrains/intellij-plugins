// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.XmlTagUtil
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.elementToImport
import org.jetbrains.vuejs.codeInsight.extractComponentSymbol
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.inspections.quickfixes.VueImportComponentQuickFix
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.PROP_VUE_COMPOSITION_COMPONENT
import org.jetbrains.vuejs.web.PROP_VUE_PROXIMITY

class VueMissingComponentImportInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return object : XmlElementVisitor() {
      override fun visitXmlTag(tag: XmlTag) {
        val descriptor = tag.descriptor
        if (descriptor !is HtmlElementSymbolDescriptor)
          return

        val symbol = descriptor.symbol
        if (symbol[PROP_VUE_PROXIMITY] != VueModelVisitor.Proximity.OUT_OF_SCOPE
            || symbol[PROP_VUE_COMPOSITION_COMPONENT] == true)
          return
        val elementToImport = symbol.extractComponentSymbol()?.elementToImport ?: return

        val tagNameToken = XmlTagUtil.getStartTagNameElement(tag) ?: return

        val tagName = tagNameToken.text
        holder.registerProblem(tagNameToken,
                               VueBundle.message("vue.inspection.message.missing.component.import", tagName),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                               VueImportComponentQuickFix(tagNameToken, toAsset(tagName, true), elementToImport))
      }
    }
  }

  override fun isAvailableForFile(file: PsiFile): Boolean = file.isVueFile
}
