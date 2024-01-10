// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlTokenType
import org.angular2.codeInsight.blocks.Angular2HtmlBlocksCodeCompletionProvider
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes

class Angular2HtmlCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC,
           psiElement(Angular2HtmlTokenTypes.BLOCK_NAME),
           Angular2HtmlBlocksCodeCompletionProvider())
    extend(CompletionType.BASIC,
           psiElement(XmlTokenType.XML_DATA_CHARACTERS)
             .withParent(PlatformPatterns.or(psiElement(XmlDocument::class.java), psiElement(XmlText::class.java))),
           Angular2HtmlBlocksCodeCompletionProvider())
  }
}