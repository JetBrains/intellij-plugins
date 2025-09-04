package com.intellij.dts.completion.insert

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.dts.util.DtsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType

private fun isLineBlank(start: PsiElement): Boolean {
  for (element in DtsUtil.iterateLeafs(start, filter = false, strict = false)) {
    if (element.elementType != TokenType.WHITE_SPACE) return false
    if (element.text.contains('\n')) return true
  }

  return true
}

private class InsertBackendImpl(val context: InsertionContext) : DtsInsertBackend {
  private val nextChar: Char?
  private val lineEmpty: Boolean

  private val startOffset: Int = context.selectionEndOffset
  private var offset: Int = startOffset

  init {
    val start = context.file.findElementAt(startOffset)

    if (start == null) {
      nextChar = null
      lineEmpty = true
    }
    else {
      lineEmpty= isLineBlank(start)

      val nextElement = DtsUtil.iterateLeafs(start, strict = false).firstOrNull()
      nextChar = nextElement?.text?.firstOrNull()
    }
  }

  override fun shouldWrite(char: Char): Int {
    val sameCharacter = nextChar == char

    return when {
      sameCharacter && !lineEmpty -> 3
      sameCharacter -> 2
      !lineEmpty -> 1
      else -> 0
    }
  }

  private fun skipWhitespace(text: String): String {
    val start = offset
    for (c in text) {
      if (c != ' ' || context.document.text.getOrNull(offset) != ' ') break

      offset++
    }

    return text.substring(offset - start)
  }

  override fun write(text: String) {
    val trimmed = skipWhitespace(text)

    context.document.insertString(offset, trimmed)
    offset += trimmed.length
  }

  override fun moveCaret(offset: Int) {
    context.editor.caretModel.moveToOffset(startOffset + offset)
  }

  override fun openAutocomplete() {
    AutoPopupController.getInstance(context.project).scheduleAutoPopup(context.editor)
  }
}

fun dtsInsertIntoDocument(context: InsertionContext, writer: DtsInsertSession.() -> Unit) {
  val session = DtsInsertSession(InsertBackendImpl(context))
  dtsRunInsertSession(session, writer)
}


