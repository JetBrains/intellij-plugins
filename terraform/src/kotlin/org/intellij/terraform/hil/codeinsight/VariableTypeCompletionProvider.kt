/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
package org.intellij.terraform.hil.codeinsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLMethodCallExpression
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.common.MethodCallExpression
import org.intellij.terraform.hcl.psi.common.SelectExpression

object VariableTypeCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val position = parameters.position // AST element
    val parent = position.parent as? HCLIdentifier ?: return

    val p = PsiTreeUtil.getTopmostParentOfType(parent, HCLProperty::class.java) ?: return
    if (!PsiTreeUtil.isAncestor(p.value, parent, false)) return
    var call: HCLMethodCallExpression?
    call = PsiTreeUtil.getParentOfType(parent, HCLMethodCallExpression::class.java, true)
    if (call?.callee === parent) {
      call = PsiTreeUtil.getParentOfType(parent.parent, HCLMethodCallExpression::class.java, true)
    }
    when {
      p.name == "type" -> {
        result.addAllElements(listOf("string", "number", "bool", "any").map { LookupElementBuilder.create(it) })
        result.addAllElements(listOf("list", "set", "map", "object", "tuple").map { LookupElementBuilder.create(it).withInsertHandler(FunctionInsertHandler) })
        if (call?.callee?.text == "object") {
          // Keyword "optional" is valid only as a modifier for object type attributes.
          result.addElement(LookupElementBuilder.create("optional").withInsertHandler(FunctionInsertHandler))
        }
      }
      p.name == "default" -> {

      }
    }
  }


  object FunctionInsertHandler : BasicInsertHandler<LookupElement>() {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
      val editor = context.editor
      val file = context.file

      val project = editor.project
      if (project == null || project.isDisposed) return

      val e = file.findElementAt(context.startOffset) ?: return

      val element = (if (e.node?.elementType == HCLElementTypes.ID) {
        e.parent
      } else {
        e
      }) as? HCLIdentifier ?: return

      val name = item.lookupString

      // Probably first element in interpolation OR under ILSelectExpression
      val parent = element.parent
      if (parent is SelectExpression<*>) {
        return
      }

      if (context.completionChar in " (") {
        context.setAddCompletionChar(false)
      }

      if (parent is MethodCallExpression<*>) {
        // Looks like function name was modified
      } else {
        val add = when (name) {
          "object" -> "({})" to 2
          "tuple" -> "([])" to 2
          else -> "()" to 1
        }
        EditorModificationUtil.insertStringAtCaret(editor, add.first, false, false)
        editor.caretModel.moveToOffset(editor.caretModel.offset + add.second)
        scheduleBasicCompletion(context)
      }

      PsiDocumentManager.getInstance(project).commitDocument(editor.document)
    }


    private fun scheduleBasicCompletion(context: InsertionContext) {
      context.laterRunnable = object : Runnable {
        override fun run() {
          if (context.project.isDisposed || context.editor.isDisposed) return
          CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(context.project, context.editor)
        }
      }
    }
  }

}