// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import org.angular2.codeInsight.blocks.Angular2BlocksCodeCompletionProvider
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes

class Angular2HtmlCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement(Angular2HtmlTokenTypes.BLOCK_NAME),
           Angular2BlocksCodeCompletionProvider())
  }
}