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
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.model.BlockType

class ResourceBlockSubNameInsertHandler(val type: BlockType) : BasicInsertHandler<LookupElement>() {
  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val editor = context.editor
    val file = context.file
    val project = context.project

    if (project.isDisposed) return

    var element = file.findElementAt(context.startOffset) ?: return
    val parent: PsiElement

    if (element is HCLIdentifier) {
      parent = element.parent ?: return
    } else if (element is HCLStringLiteral) {
      parent = element.parent ?: return
    } else if (HCLTokenTypes.IDENTIFYING_LITERALS.contains(element.node?.elementType)) {
      element = element.parent
      val p = element
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
    var offset: Int? = null
    val already: Int
    val expected = type.args
    if (parent is HCLBlock && InsertHandlersUtil.isNextNameOnTheSameLine(element, context.document)) {
      // Count existing arguments and add missing
      val elements = parent.nameElements
      already = elements.size - 1
      val i = elements.indexOf(element)
      assert(i != -1)

      // Locate caret to next argument
      // Do not invoke completion for last name
      if (i < elements.lastIndex && i + 1 != elements.lastIndex) {
        val next = elements[i + 1]
        if (next.textContains('"') && next.textLength < 3) {
          editor.caretModel.moveToOffset(next.textRange.endOffset - 1)
          InsertHandlersUtil.scheduleBasicCompletion(context)
        }
      }
    } else {
      return
    }

    if (already < expected) {
      offset = editor.caretModel.offset + 2
      InsertHandlersUtil.addArguments(expected - already, editor)
      if (expected - already != 1) { // Do not invoke completion for last name
        InsertHandlersUtil.scheduleBasicCompletion(context)
      }
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