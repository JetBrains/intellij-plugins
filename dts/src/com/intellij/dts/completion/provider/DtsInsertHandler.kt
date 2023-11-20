package com.intellij.dts.completion.provider

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.symbols.DtsPropertySymbol
import com.intellij.dts.util.DtsUtil
import com.intellij.dts.lang.DtsPropertyType
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.util.asSafely

private fun checkLineBlank(start: PsiElement): Boolean {
    for (element in DtsUtil.iterateLeafs(start, filter = false, strict = false)) {
        if (element.elementType != TokenType.WHITE_SPACE) return false
        if (element.text.contains('\n')) return true
    }

    return true
}

private class InsertSession(val context: InsertionContext, force: Boolean) {
    val canInsert: Boolean
    val nextElement: PsiElement?

    var offset: Int = context.selectionEndOffset

    init {
        val start = context.file.findElementAt(offset)

        if (start == null) {
            canInsert = true
            nextElement = null
        } else {
            canInsert = force || checkLineBlank(start)
            nextElement = DtsUtil.iterateLeafs(start, strict = false).firstOrNull()
        }
    }

    private fun insertText(text: String, moveCaret: Boolean = false) {
        context.document.insertString(offset, text)
        offset += text.length

        if (moveCaret) {
            context.editor.caretModel.moveToOffset(offset)
        }
    }

    private fun insertPair(leftStr: String, leftToken: IElementType, rightStr: String, rightToken: IElementType, body: String): Boolean {
        if (!canInsert || nextElement.elementType == leftToken) return false
        insertText(leftStr + body, moveCaret = true)

        if (nextElement.elementType == rightToken) return false
        insertText(rightStr)

        return true
    }

    fun insertSpace(): Boolean {
        if (!canInsert) return false

        val text = context.document.text

        if (text.length > offset && text.slice(offset..offset) == " ") {
            context.editor.caretModel.moveToOffset(++offset)
        } else {
            insertText(" ", moveCaret = true)
        }

        return true
    }

    fun insertAssign(): Boolean {
        if (!canInsert || nextElement.elementType == DtsTypes.ASSIGN) return false
        insertText(" = ", moveCaret = true)

        return true
    }

    fun insertString(): Boolean = insertPair(
        leftStr = "\"",
        leftToken = DtsTypes.DQUOTE,
        rightStr = "\"",
        rightToken = DtsTypes.DQUOTE,
        body = "",
    )

    fun insertCellArray(body: String = ""): Boolean = insertPair(
        leftStr = "<",
        leftToken = DtsTypes.LANGL,
        rightStr = ">",
        rightToken = DtsTypes.RANGL,
        body = body,
    )

    fun insertByteArray(): Boolean = insertPair(
        leftStr = "[",
        leftToken = DtsTypes.LBRACKET,
        rightStr = "]",
        rightToken = DtsTypes.RBRACKET,
        body = "",
    )

    fun insertNodeBraces(): Boolean = insertPair(
        leftStr = "{",
        leftToken = DtsTypes.LBRACE,
        rightStr = "}",
        rightToken = DtsTypes.RBRACE,
        body = "",
    )

    fun insertSemicolon(moveCaret: Boolean = false): Boolean {
        if (!canInsert || nextElement.elementType == DtsTypes.SEMICOLON) return false
        insertText(";", moveCaret)

        return true
    }

    fun openAutocomplete(condition: Boolean = true): Boolean {
        if (condition) {
            AutoPopupController.getInstance(context.project).scheduleAutoPopup(context.editor, CompletionType.BASIC, null)
        }

        return true
    }
}

object DtsInsertHandler {
    private inline fun <reified T : Symbol> getSymbol(item: LookupElement): T? {
        return item.`object`.asSafely<Pointer<*>>()?.dereference().asSafely<T>()
    }

    private fun insertBool(session: InsertSession) {
        session.insertSemicolon(moveCaret = true)
    }

    private fun insertCompound(session: InsertSession) {
        session.insertAssign() && session.insertSemicolon()
    }

    private fun insertString(symbol: DtsPropertySymbol, session: InsertSession) {
        val isCompatible = symbol.name == "compatible"
        session.insertAssign() && session.insertString() && session.openAutocomplete(isCompatible) && session.insertSemicolon()
    }

    private fun insertCellArray(session: InsertSession) {
        session.insertAssign() && session.insertCellArray() && session.insertSemicolon()
    }

    private fun insertByteArray(session: InsertSession) {
        session.insertAssign() && session.insertByteArray() && session.insertSemicolon()
    }

    private fun insertPHandle(session: InsertSession) {
        session.insertAssign() && session.insertCellArray("&") && session.openAutocomplete() && session.insertSemicolon()
    }

    val PROPERTY = InsertHandler<LookupElement> { context, item ->
        val symbol = getSymbol<DtsPropertySymbol>(item) ?: return@InsertHandler
        val session = InsertSession(context, force = false)

        when (symbol.type) {
            DtsPropertyType.Boolean -> insertBool(session)
            DtsPropertyType.String, DtsPropertyType.StringList -> insertString(symbol, session)
            DtsPropertyType.Int, DtsPropertyType.Ints, DtsPropertyType.PHandleList -> insertCellArray(session)
            DtsPropertyType.PHandle, DtsPropertyType.PHandles -> insertPHandle(session)
            DtsPropertyType.Bytes -> insertByteArray(session)
            DtsPropertyType.Path, DtsPropertyType.Compound -> insertCompound(session)
        }
    }

    val SUB_NODE = InsertHandler<LookupElement> { context, _ ->
        val session = InsertSession(context, force = false)
        session.insertSpace() && session.insertNodeBraces() && session.insertSemicolon()
    }

    val ROOT_NODE = InsertHandler<LookupElement> { context, _ ->
        val session = InsertSession(context, force = true)
        session.insertSpace() && session.insertNodeBraces() && session.insertSemicolon()
    }
}