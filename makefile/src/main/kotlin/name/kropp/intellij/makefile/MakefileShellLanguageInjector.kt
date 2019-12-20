package name.kropp.intellij.makefile

import com.intellij.lang.*
import com.intellij.lang.injection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.*
import java.util.*
import kotlin.math.*

private const val SHELL_LANGUAGE_ID = "Shell Script"
private val SHELL_LANGUAGE = Language.findLanguageByID(SHELL_LANGUAGE_ID)

class MakefileShellLanguageInjector : MultiHostInjector {
  override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
    return Collections.singletonList(MakefileRecipe::class.java)
  }

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    if (SHELL_LANGUAGE != null && context is MakefileRecipe) {
      val rangesToInject =context.children.filterIsInstance<MakefileCommand>().map {
        TextRange.create(it.textRangeInParent.startOffset + 1, min(it.textRangeInParent.endOffset + 1, context.textLength))
      }
      if (rangesToInject.any()) {
        registrar.startInjecting(SHELL_LANGUAGE)
        rangesToInject.forEach {
          registrar.addPlace(null, null, context, it)
        }
        registrar.doneInjecting()
      }
    }
  }
}