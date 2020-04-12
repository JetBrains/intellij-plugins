package name.kropp.intellij.makefile.psi

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.*

abstract class MakefileSubstitutionInjectionHost(node: ASTNode) : ASTWrapperPsiElement(node), MakefileSubstitution {
  override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
    return MakefileSubstitutionTextEscaper(this)
  }

  override fun updateText(text: String): PsiLanguageInjectionHost {
    val command = MakefileElementFactory.createSubstitution(project, text)
    return this.replace(command) as MakefileSubstitution
  }

  override fun isValidHost(): Boolean {
    return true
  }
}
