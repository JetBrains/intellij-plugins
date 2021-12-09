package com.jetbrains.lang.makefile.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

abstract class MakefileFunctionElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), MakefileFunction {
  override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
    return MakefileFunctionTextEscaper(this)
  }

  override fun updateText(text: String): MakefileFunction {
    val fn = MakefileElementFactory.createFunction(project, text)
    return this.replace(fn) as MakefileFunction
  }

  override fun isValidHost(): Boolean {
    return true
  }
}
