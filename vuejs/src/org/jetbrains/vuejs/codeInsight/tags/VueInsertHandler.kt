// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.tags

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.editorActions.XmlTagNameSynchronizer
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.ImportExportType
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.editor.VueComponentSourceEdit
import org.jetbrains.vuejs.model.VueModelManager

class VueInsertHandler : XmlTagInsertHandler() {

  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    if (shouldHandleXmlInsert(context)) {
      super.handleInsert(context, item)
    }
    val element = WebSymbolCodeCompletionItem.getPsiElement(item)
                  ?: return
    val importedFile = element.containingFile
    if (importedFile == context.file) return
    val nodeModule = NodeModuleSearchUtil.findDependencyRoot(element.containingFile.virtualFile)
    if (isSkippedModule(nodeModule)) return

    context.commitDocument()
    val elementToImport = if (importedFile is XmlFile
                              || ES6PsiUtil.findDefaultExports(importedFile).any { PsiTreeUtil.isContextAncestor(it, element, false) })
      importedFile
    else element
    XmlTagNameSynchronizer.runWithoutCancellingSyncTagsEditing(context.document) {
      val location = PsiTreeUtil.findElementOfClassAtOffset(context.file, context.startOffset, PsiElement::class.java, false)
      val componentSourceEdit = location?.let { VueComponentSourceEdit.create(VueModelManager.findEnclosingContainer(it)) }
                                ?: return@runWithoutCancellingSyncTagsEditing
      componentSourceEdit.insertComponentImport(item.lookupString.removePrefix("<"), elementToImport)
      componentSourceEdit.reformatChanges()
      PostprocessReformattingAspect.getInstance(context.project).doPostponedFormatting()
    }
  }

  companion object {
    val INSTANCE: VueInsertHandler = VueInsertHandler()

    fun reformatElement(element: PsiElement?) {
      if (element != null && element.isValid) {
        val range = element.textRange
        FormatFixer.doReformat(element as? PsiFile ?: element.containingFile, range.startOffset, range.endOffset)
      }
    }

    private fun shouldHandleXmlInsert(context: InsertionContext): Boolean {
      val file = context.file
      if (!file.language.isKindOf(XMLLanguage.INSTANCE)) {
        return false
      }
      val element = PsiTreeUtil.findElementOfClassAtOffset(file, context.startOffset, XmlTag::class.java, false)
      return element == null || element.language.isKindOf(XMLLanguage.INSTANCE)
    }

    private fun isSkippedModule(nodeModule: VirtualFile?) =
      nodeModule != null
      && nodeModule.parent?.name == JSLibraryUtil.NODE_MODULES
      && ("vue" == nodeModule.name || "vue-router" == nodeModule.name)

  }
}
