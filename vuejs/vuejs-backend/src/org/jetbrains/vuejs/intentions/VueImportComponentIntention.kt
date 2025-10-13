// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.intentions

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.lang.javascript.intentions.JavaScriptIntention
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.asSafely
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.extractComponentSymbol
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.inspections.quickfixes.VueImportComponentQuickFix
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.PROP_VUE_COMPOSITION_COMPONENT
import org.jetbrains.vuejs.web.PROP_VUE_PROXIMITY
import org.jetbrains.vuejs.web.symbols.VueComponentSymbol

class VueImportComponentIntention : JavaScriptIntention(), HighPriorityAction {

  override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
    element.node.elementType == XmlTokenType.XML_NAME
    && element.parent.asSafely<XmlTag>()
      ?.descriptor.asSafely<HtmlElementSymbolDescriptor>()
      ?.symbol
      ?.extractComponentSymbol()
      ?.takeIf { it.getElementToImport() != null }
      ?.let {
        it[PROP_VUE_COMPOSITION_COMPONENT] == true
        || it[PROP_VUE_PROXIMITY].let { proximity ->
          proximity == null || (proximity != VueModelVisitor.Proximity.LOCAL && proximity != VueModelVisitor.Proximity.OUT_OF_SCOPE)
        }
      } == true

  override fun getFamilyName(): String =
    VueBundle.message("vue.template.intention.import.component.family.name")

  override fun getText(): String =
    VueBundle.message("vue.template.intention.import.component.family.name")

  override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
    val tag = element.parent as? XmlTag ?: return
    val elementToImport = tag.descriptor.asSafely<HtmlElementSymbolDescriptor>()
                            ?.symbol
                            ?.extractComponentSymbol()
                            ?.getElementToImport()

                          ?: return
    VueImportComponentQuickFix(element, toAsset(tag.name), elementToImport).applyFix()
  }

  override fun generatePreview(project: Project, editor: Editor, psiFile: PsiFile): IntentionPreviewInfo {
    return IntentionPreviewInfo.EMPTY
  }

  private fun PolySymbol.getElementToImport() =
    this.asSafely<PsiSourcedPolySymbol>()
      ?.let {
        if (it is VueComponentSymbol)
          it.rawSource
        else
          it.source
      }
      ?.takeIf { it !is JSLiteralExpression }
}