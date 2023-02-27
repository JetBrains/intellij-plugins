/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.getNextSiblingNonWhiteSpace
import org.intellij.terraform.config.model.*
import org.intellij.terraform.hil.codeinsight.ReferenceCompletionHelper

object ResourcePropertyInsertHandler : BasicInsertHandler<LookupElement>() {
  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val editor = context.editor
    val project = context.project
    // Add property name - done by default

    val element = context.file.findElementAt(context.startOffset)

    // Ensure not modifying existing property
    if (element is HCLIdentifier || element is HCLStringLiteral) {
      if (isNextIsEqual(element)) return
    } else if (HCLTokenTypes.IDENTIFYING_LITERALS.contains(element?.node?.elementType)) {
      if (isNextIsEqual(element?.parent)) return
    }

    // Add equals sign
    if (context.completionChar in " =") {
      context.setAddCompletionChar(false)
    }
    EditorModificationUtil.insertStringAtCaret(editor, " = ")

    // Add value placeholder: "" for string; 0 for int, "${}" for string with IL, etc
    val obj = item.`object`
    if (obj is PropertyType) {
      val module = PsiTreeUtil.getParentOfType(element, HCLElement::class.java)?.getTerraformModule()
      val pair = module?.let { getProposedValueFromModelAndHint(obj, module) } ?: getPlaceholderValue(obj.type)
      if (pair != null) {
        EditorModificationUtil.insertStringAtCaret(editor, pair.first)
        EditorModificationUtil.moveCaretRelatively(editor, pair.second)
      }
    }

    PsiDocumentManager.getInstance(project).commitDocument(editor.document)
  }

  fun getPlaceholderValue(type: Type): Pair<String, Int>? {
    return when (type) {
      Types.String -> Pair("\"\"", -1)
      is MapType, is ObjectType -> Pair("{}", -1)
      is ListType, is SetType, is TupleType -> Pair("[]", -1)
      else -> null
    }
  }

  fun getProposedValueFromModelAndHint(property: PropertyType, module: Module): Pair<String, Int>? {
    val hint = property.hint
    if (hint is ReferenceHint) {
      val suggestions: List<Pair<String, Int>> = hint.hint
          .mapNotNull { ReferenceCompletionHelper.findByFQNRef(it, module) }
          .flatten()
          .mapNotNull {
            return@mapNotNull when (it) {
            // TODO: Enable or remove next two lines
            // is HCLBlock -> HCLQualifiedNameProvider.getQualifiedModelName(it)
            // is HCLProperty -> HCLQualifiedNameProvider.getQualifiedModelName(it)
              is String -> Pair((if (module.isHCL2Supported()) it else "\"\${$it}\""), 0)
              else -> null
            }
          }
      if (suggestions.size == 1) {
        return suggestions.first()
      }
    }
    return null
  }

  private fun isNextIsEqual(element: PsiElement?): Boolean {
    return element?.getNextSiblingNonWhiteSpace()?.node?.elementType == HCLElementTypes.EQUALS
  }
}
