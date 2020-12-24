package name.kropp.intellij.makefile

import com.intellij.psi.*
import com.intellij.spellchecker.inspections.*
import com.intellij.spellchecker.tokenizer.*
import name.kropp.intellij.makefile.psi.*

class MakefileSpellcheckingStrategy : SpellcheckingStrategy() {
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