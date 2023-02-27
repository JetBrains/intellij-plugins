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
package org.intellij.terraform.config.editor

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLFile

class TerraformInterpolationStartTypedHandler : TypedHandlerDelegate() {
  override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (c != '{') return Result.CONTINUE

    if (file !is HCLFile || !file.isInterpolationsAllowed()) return Result.CONTINUE

    if (!CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET) return Result.CONTINUE

    PsiDocumentManager.getInstance(project).commitDocument(editor.document)

    val offset = editor.caretModel.offset
    val element = file.findElementAt(offset)

    if (HCLTokenTypes.STRING_LITERALS.contains(element?.node?.elementType)
        && "\$" == editor.document.getText(TextRange.from(offset - 2, 1))
        && (offset >= editor.document.textLength || "}" != editor.document.getText(TextRange.from(offset, 1)))) {
      editor.document.insertString(offset, "}")
      return Result.STOP
    }

    return Result.CONTINUE
  }
}
