package name.kropp.intellij.makefile.psi

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.*

abstract class MakefileFunctionElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), MakefileFunction {
  override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
    return MakefileFunctionTextEscaper(this)
  }

  override fun updateText(text: String): PsiLanguageInjectionHost {
    val fn = MakefileElementFactory.createFunction(project, text)
    return this.replace(fn) as MakefileFunction
  }

  override fun isValidHost(): Boolean {
    return true
  }
}
