package com.jetbrains.lang.makefile.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

abstract class MakefileRecipeElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), MakefileRecipe {
  override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
    return MakefileRecipeTextEscaper(this)
  }

  override fun updateText(text: String): MakefileRecipe {
    val command = MakefileElementFactory.createRecipe(project, text)
    return this.replace(command) as MakefileRecipe
  }

  override fun isValidHost(): Boolean {
    return true
  }
}
