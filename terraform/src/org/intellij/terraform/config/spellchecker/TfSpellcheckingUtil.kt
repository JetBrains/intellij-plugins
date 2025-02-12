// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.spellchecker

import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.component1
import com.intellij.openapi.util.component2
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.inspections.PlainTextSplitter
import com.intellij.spellchecker.tokenizer.TokenConsumer
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.intellij.terraform.hcl.psi.HCLHeredocContent
import org.intellij.terraform.hcl.psi.HCLStringLiteral

object TfSpellcheckingUtil {
  val RootBlocksWithChangeableName: Set<String> = setOf("output", "variable", "module", "resource", "data")

  val StringLiteralTokenizer: Tokenizer<HCLStringLiteral> = object : Tokenizer<HCLStringLiteral>() {
    override fun tokenize(element: HCLStringLiteral, consumer: TokenConsumer) {
      handleTokenizing(element, element.textFragments, consumer)
    }
  }

  val HeredocContentTokenizer: Tokenizer<HCLHeredocContent> = object : Tokenizer<HCLHeredocContent>() {
    override fun tokenize(element: HCLHeredocContent, consumer: TokenConsumer) {
      handleTokenizing(element, element.textFragments, consumer)
    }
  }

  private fun handleTokenizing(element: PsiElement, textFragments: List<Pair<TextRange, String>>, consumer: TokenConsumer) {
    val textSplitter = PlainTextSplitter.getInstance()
    for ((fragmentRange, escaped) in textFragments) {
      // Fragment without escaping, also not a broken escape sequence or a unicode code point
      if (escaped.length == fragmentRange.length && !escaped.startsWith("\\")) {
        consumer.consumeToken(element, escaped, false, fragmentRange.startOffset, TextRange.allOf(escaped), textSplitter)
      }
    }
  }
}