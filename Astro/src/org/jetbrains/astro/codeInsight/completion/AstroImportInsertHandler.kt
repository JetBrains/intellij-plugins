// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight.completion

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.editorActions.XmlTagNameSynchronizer
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import org.jetbrains.astro.inspections.quickfixes.AstroImportComponentQuickFix

object AstroImportInsertHandler : XmlTagInsertHandler() {


  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    super.handleInsert(context, item)
    val element = WebSymbolCodeCompletionItem.getPsiElement(item)
                  ?: return
    val importedFile = element.containingFile
    if (importedFile == context.file) return

    context.commitDocument()

    XmlTagNameSynchronizer.runWithoutCancellingSyncTagsEditing(context.document) {
      val location = PsiTreeUtil.findElementOfClassAtOffset(context.file, context.startOffset, PsiElement::class.java, false)
                     ?: return@runWithoutCancellingSyncTagsEditing
      AstroImportComponentQuickFix(location, item.lookupString.removePrefix("<"), element).applyFix()
      PostprocessReformattingAspect.getInstance(context.project).doPostponedFormatting()
    }
  }
}