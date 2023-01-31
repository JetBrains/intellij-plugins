// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.text.CharSequenceSubSequence
import com.intellij.xml.impl.XmlBraceMatcher

class AstroBraceMatcher : XmlBraceMatcher() {

  override fun isFileTypeWithSingleHtmlTags(fileType: FileType?): Boolean {
    return true
  }

  override fun isPairBraces(tokenType1: IElementType, tokenType2: IElementType): Boolean =
    (tokenType1 === JSTokenTypes.XML_LBRACE && tokenType2 === JSTokenTypes.XML_RBRACE)
    || super.isPairBraces(tokenType1, tokenType2)

  override fun isLBraceToken(iterator: HighlighterIterator, fileText: CharSequence, fileType: FileType): Boolean =
    iterator.tokenType === JSTokenTypes.XML_LBRACE
    || (iterator.tokenType.let { it === XmlTokenType.XML_DATA_CHARACTERS || it === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN }
        && CharSequenceSubSequence(fileText, iterator.start, iterator.end).contentEquals("{"))
    || super.isLBraceToken(iterator, fileText, fileType)

  override fun isRBraceToken(iterator: HighlighterIterator, fileText: CharSequence, fileType: FileType): Boolean =
    iterator.tokenType === JSTokenTypes.XML_RBRACE
    || (iterator.tokenType.let { it === XmlTokenType.XML_DATA_CHARACTERS || it === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN }
        && CharSequenceSubSequence(fileText, iterator.start, iterator.end).contentEquals("}"))
    || super.isRBraceToken(iterator, fileText, fileType)

}