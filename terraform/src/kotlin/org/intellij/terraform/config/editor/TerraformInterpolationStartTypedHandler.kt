// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
