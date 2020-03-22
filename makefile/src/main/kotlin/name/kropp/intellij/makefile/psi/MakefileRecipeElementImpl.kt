package name.kropp.intellij.makefile.psi

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.psi.*

abstract class MakefileRecipeElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), MakefileRecipe {
  override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
    return MakefileRecipeTextEscaper(this)
  }

  override fun updateText(text: String): PsiLanguageInjectionHost {
    val command = MakefileElementFactory.createRecipe(project, text)
    return this.replace(command) as MakefileRecipe
  }

  override fun isValidHost(): Boolean {
    return true
  }
}
