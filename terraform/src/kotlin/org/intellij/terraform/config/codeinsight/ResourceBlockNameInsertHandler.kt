/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.BasicInsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.TypeModel

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
      // ??? Do nothing
      return
    }
    if (item.lookupString in TypeModel.RootBlocksMap.keys && (element.parent !is PsiFile && parent.parent !is PsiFile)) {
      return
    }
    var offset: Int? = null
    val current: Int
    val expected = type.args
    var addBraces = false
    if (parent is HCLBlock && InsertHandlersUtil.isNextNameOnTheSameLine(element, context.document)) {
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
      InsertHandlersUtil.addArguments(expected - current, editor)
      InsertHandlersUtil.scheduleBasicCompletion(context)
    }
    if (addBraces) {
      InsertHandlersUtil.addBraces(editor)
    }

    PsiDocumentManager.getInstance(project).commitDocument(editor.document)
    if (offset != null) {
      editor.caretModel.moveToOffset(offset)
    }
    if (type.properties.isNotEmpty()) {
      InsertHandlersUtil.addHCLBlockRequiredProperties(file, editor, project)
    }
  }
}
