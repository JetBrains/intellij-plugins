// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.BasicInsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty

class ResourceBlockNameInsertHandler(val type: BlockType) : BasicInsertHandler<LookupElement>() {
  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val editor = context.editor
    val file = context.file
    val project = context.project

    if (project.isDisposed) return

    val element = file.findElementAt(context.startOffset) ?: return
    val parent: PsiElement

    if (element is HCLIdentifier) {
      parent = element.parent ?: return
    } else if (element.node?.elementType == HCLElementTypes.ID) {
      val p = element.parent
      if (p is HCLElement) {
        parent = p as? HCLObject ?: p.parent ?: return
      } else {
        parent = p
      }
    } else {
      return
    }

    if (parent is HCLProperty) {
      return
    }

    var offset: Int? = null
    val current: Int
    val expected = type.args
    var addBraces = false
    if (parent is HCLBlock && TfInsertHandlerService.isNextNameOnTheSameLine(element, context.document)) {
      // Count existing arguments and add missing
      val elements = parent.nameElements
      current = elements.size - 1
      // Locate caret to latest argument
      val last = elements.last()
      // TODO: Move caret to last argument properly
      editor.caretModel.moveToOffset(last.textRange.endOffset)
    } else {
      // Add arguments and braces
      current = 0
      addBraces = true
    }
    // TODO check context.completionChar before adding arguments or braces

    if (current < expected) {
      offset = editor.caretModel.offset + 2
      TfInsertHandlerService.addArguments(expected - current, editor)
      TfInsertHandlerService.scheduleBasicCompletion(context)
    }
    if (addBraces) {
      TfInsertHandlerService.addBraces(editor)
    }
    PsiDocumentManager.getInstance(project).commitDocument(editor.document)

    if (offset != null) {
      editor.caretModel.moveToOffset(offset)
    }
    if (type.properties.isNotEmpty()) {
      TfInsertHandlerService.getInstance(project).addBlockRequiredProperties(file, editor, project)
    }
  }
}
