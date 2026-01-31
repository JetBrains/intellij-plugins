package com.jetbrains.lang.makefile.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

abstract class MakefileSubstitutionInjectionHost(node: ASTNode) : ASTWrapperPsiElement(node), MakefileSubstitution {
  override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
    return MakefileSubstitutionTextEscaper(this)
  }

  override fun updateText(text: String): MakefileSubstitution {
    val command = MakefileElementFactory.createSubstitution(project, text)
    return this.replace(command) as MakefileSubstitution
  }

  override fun isValidHost(): Boolean {
    return true
  }
}
