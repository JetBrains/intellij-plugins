package org.intellij.prisma.ide.editor

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.DocumentUtil
import com.intellij.util.text.CharArrayUtil
import org.intellij.prisma.lang.psi.PrismaFieldDeclarationBlock
import org.intellij.prisma.lang.psi.PrismaFile
import org.intellij.prisma.lang.psi.PrismaModelDeclaration

class PrismaTypedHandler : TypedHandlerDelegate() {

  override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (file !is PrismaFile) return Result.CONTINUE

    if (charTyped == '@' || charTyped == '"') {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
      return Result.STOP
    }

    if (Character.isJavaIdentifierPart(charTyped)) {
      val offset = editor.caretModel.offset
      val element = file.findElementAt(offset) ?: return Result.CONTINUE

      val isImmediateModelDeclarationChild = PsiTreeUtil.skipParentsOfType(
        element,
        PrismaFieldDeclarationBlock::class.java
      ) is PrismaModelDeclaration

      if (!isImmediateModelDeclarationChild) {
        return Result.CONTINUE
      }

      // skip a completion popup in the beginning of the model block line
      // because only block attributes are available,
      // but their popup is triggered by '@' explicitly
      if (DocumentUtil.isLineEmpty(editor.document, editor.document.getLineNumber(offset))) {
        return Result.STOP
      }

      // the same but for the first identifier
      val chars = editor.document.immutableCharSequence
      val beforeIdentifier = skipIdentifierBackward(chars, offset)
      val newLineOffset = CharArrayUtil.shiftBackward(chars, beforeIdentifier, " \t")
      if (StringUtil.isChar(chars, newLineOffset, '\n')) {
        return Result.STOP
      }
    }

    return Result.CONTINUE
  }

  private fun skipIdentifierBackward(chars: CharSequence, start: Int): Int {
    if (start == 0) return 0

    var i = start - 1
    while (i > 0 && chars[i].isLetterOrDigit()) {
      i--
    }
    return i
  }
}