package name.kropp.intellij.makefile

import com.intellij.psi.*
import com.intellij.spellchecker.tokenizer.*

class MakefileSpellcheckingStrategy : SpellcheckingStrategy() {
  override fun getTokenizer(element: PsiElement): Tokenizer<*> {
    return super.getTokenizer(element)
  }
}