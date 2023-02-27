// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.spellchecker

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.spellchecker.inspections.PlainTextSplitter
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.TokenConsumer
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.intellij.terraform.hcl.psi.HCLHeredocContent
import org.intellij.terraform.hcl.psi.HCLHeredocMarker
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.config.patterns.TerraformPatterns

open class HCLSpellcheckerStrategy : SpellcheckingStrategy() {
  companion object {
    val StringLiteralTokenizer = object : Tokenizer<HCLStringLiteral>() {
      override fun tokenize(element: HCLStringLiteral, consumer: TokenConsumer) {
        val textSplitter = PlainTextSplitter.getInstance()
//        if (!element.textContains('\\')) {
//          return consumer.consumeToken(element, textSplitter)
//        }
        for (fragment in element.textFragments) {
          val fragmentRange = fragment.getFirst()
          val escaped = fragment.getSecond()
          // Fragment without escaping, also not a broken escape sequence or a unicode code point
          if (escaped.length == fragmentRange.length && !escaped.startsWith("\\")) {
            consumer.consumeToken(element, escaped, false, fragmentRange.startOffset, TextRange.allOf(escaped), textSplitter)
          }
        }
      }
    }
    val HeredocContentTokenizer = object : Tokenizer<HCLHeredocContent>() {
      override fun tokenize(element: HCLHeredocContent, consumer: TokenConsumer) {
        val textSplitter = PlainTextSplitter.getInstance()
//        if (!element.textContains('\\')) {
//          return consumer.consumeToken(element, textSplitter)
//        }
        for (fragment in element.textFragments) {
          val fragmentRange = fragment.getFirst()
          val escaped = fragment.getSecond()
          // Fragment without escaping, also not a broken escape sequence or a unicode code point
          if (escaped.length == fragmentRange.length && !escaped.startsWith("\\")) {
            consumer.consumeToken(element, escaped, false, fragmentRange.startOffset, TextRange.allOf(escaped), textSplitter)
          }
        }
      }
    }
  }

  override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
    if (element == null) return EMPTY_TOKENIZER
    if (element is PsiLanguageInjectionHost && InjectedLanguageManager.getInstance(element.project).getInjectedPsiFiles(element) != null) {
      return EMPTY_TOKENIZER
    }
    if (element is HCLStringLiteral) {
      return StringLiteralTokenizer
    }
    if (element is HCLHeredocContent) {
      return HeredocContentTokenizer
    }
    if (element is HCLHeredocMarker) {
      return TEXT_TOKENIZER
    }
    if (element is HCLIdentifier) {
//      if (HCLPsiUtil.isPropertyKey(element)) {
//        return EMPTY_TOKENIZER
//      }
      return TEXT_TOKENIZER
    }
    if (element is PsiNameIdentifierOwner) {
      return EMPTY_TOKENIZER
    }
    return super.getTokenizer(element)
  }

  override fun isMyContext(element: PsiElement): Boolean {
    return !TerraformPatterns.TerraformFile.accepts(element.containingFile)
  }
}
