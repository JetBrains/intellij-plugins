package com.jetbrains.lang.makefile

import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.inspections.PlainTextSplitter
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.TokenConsumer
import com.intellij.spellchecker.tokenizer.Tokenizer
import com.jetbrains.lang.makefile.psi.MakefileComment
import com.jetbrains.lang.makefile.psi.MakefileDocComment

class MakefileSpellcheckingStrategy : SpellcheckingStrategy(), DumbAware {
  override fun getTokenizer(element: PsiElement): Tokenizer<out PsiElement> = when (element) {
    is MakefileComment -> MakefileCommentTokenizer
    is MakefileDocComment -> MakefileDocCommentTokenizer
    else -> super.getTokenizer(element)
  }
}

object MakefileCommentTokenizer : Tokenizer<MakefileComment>() {
  override fun tokenize(element: MakefileComment, consumer: TokenConsumer) {
    consumer.consumeToken(element, PlainTextSplitter.getInstance())
  }
}

object MakefileDocCommentTokenizer : Tokenizer<MakefileDocComment>() {
  override fun tokenize(element: MakefileDocComment, consumer: TokenConsumer) {
    consumer.consumeToken(element, PlainTextSplitter.getInstance())
  }
}