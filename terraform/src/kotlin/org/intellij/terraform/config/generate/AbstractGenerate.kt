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
package org.intellij.terraform.config.generate

import com.intellij.codeInsight.actions.SimpleCodeInsightAction
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Plow
import org.intellij.terraform.config.codeinsight.InsertHandlersUtil.addHCLBlockRequiredProperties
import org.intellij.terraform.config.codeinsight.TerraformConfigCompletionContributor
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hcl.psi.HCLBlock

abstract class AbstractGenerate : SimpleCodeInsightAction() {
  override fun invoke(project: Project, editor: Editor, file: PsiFile) {
    if (!EditorModificationUtil.checkModificationAllowed(editor)) return
    setCaretToNearbyRoot(editor, file)
    val offset = editor.caretModel.currentCaret.offset
    val marker = editor.document.createRangeMarker(offset, offset)
    marker.isGreedyToLeft = true
    marker.isGreedyToRight = true
    TemplateManager.getInstance(project).startTemplate(editor, template, false, null, object : TemplateEditingAdapter() {
      override fun templateFinished(template: Template, brokenOff: Boolean) {
        if (!brokenOff) {
          val element = file.findElementAt(marker.startOffset)
          val block = PsiTreeUtil.getParentOfType(element, HCLBlock::class.java, false)
          if (block != null) {
            if (TextRange(marker.startOffset, marker.endOffset).contains(block.textOffset)) {
              // It's our new block
              // Invoke add properties quick fix
              addHCLBlockRequiredProperties(file, project, block)
            }
          }
        }
      }
    })
  }

  private fun setCaretToNearbyRoot(editor: Editor, file: PsiFile) {
    val offset = editor.caretModel.currentCaret.offset
    val node = file.node.findLeafElementAt(offset) ?: return
    var element = node.psi
    while (element != null) {
      val parent = element.parent
      if (parent is PsiFile) break
      element = parent
    }
    if (element != null && element !is PsiWhiteSpace) {
      val line = editor.document.getLineNumber(offset)
      val start = editor.document.getLineNumber(element.node.startOffset)
      val end = editor.document.getLineNumber(element.node.startOffset + element.node.textLength)
      if (line - start < end - line) {
        // Place before element
        editor.caretModel.currentCaret.moveToOffset(editor.document.getLineStartOffset(start))
      } else {
        // Place after element
        if (editor.document.lineCount == end + 1) {
          editor.document.insertString(element.node.startOffset + element.node.textLength, "\n")
        }
        editor.caretModel.currentCaret.moveToOffset(editor.document.getLineEndOffset(end + 1))
      }
    }
  }

  abstract val template: Template

  override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
    return TerraformPatterns.TerraformConfigFile.accepts(file)
  }

  companion object {
    val InvokeCompletionExpression: Expression = object : Expression() {
      override fun calculateQuickResult(context: ExpressionContext?): Result? {
        return null //calculateResult(context)
      }

      override fun calculateResult(context: ExpressionContext?): Result {
        val lookupItems = calculateLookupItems(context)
        if (lookupItems == null || lookupItems.isEmpty()) return TextResult("")

        return TextResult(lookupItems[0].lookupString)
      }

      override fun calculateLookupItems(context: ExpressionContext?): Array<out LookupElement>? {
        if (context == null) return null
        val editor = context.editor ?: return null
        val file = PsiDocumentManager.getInstance(context.project).getPsiFile(editor.document) ?: return null
        val element = file.findElementAt(context.startOffset) ?: return null
        val consumer = Plow.of { consumer ->
          TerraformConfigCompletionContributor.BlockTypeOrNameCompletionProvider.doCompletion(element, consumer)
        }.collectTo(ArrayList())
        consumer.sortBy { it.lookupString }
        return consumer.toTypedArray()
      }
    }
  }
}
