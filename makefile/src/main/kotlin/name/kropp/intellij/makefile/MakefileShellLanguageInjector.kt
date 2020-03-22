package name.kropp.intellij.makefile

import com.intellij.lang.*
import com.intellij.lang.injection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.*
import kotlin.math.*

private const val SHELL_LANGUAGE_ID = "Shell Script"
private val SHELL_LANGUAGE = Language.findLanguageByID(SHELL_LANGUAGE_ID)

class MakefileShellLanguageInjector : MultiHostInjector {
  override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
    return mutableListOf(MakefileRecipe::class.java, MakefileFunction::class.java)
  }

  private fun isTab(c: Char): Boolean = c == '\t'

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    if (SHELL_LANGUAGE == null) return

    val rangesToInject = when (context) {
      is MakefileRecipe -> {
        context.children.filterIsInstance<MakefileCommand>().map {
          val tabs = it.text.takeWhile(::isTab).count()
          val silent = if (it.text.dropWhile(::isTab).firstOrNull() == '@') 1 else 0
          TextRange.create(it.textRangeInParent.startOffset + tabs + silent, min(it.textRangeInParent.endOffset + 1, context.textLength))
        }
      }
      is MakefileFunction -> {
        if (context.functionName.textMatches("shell")) {
          context.children.filterIsInstance<MakefileFunctionParam>().map { it.textRangeInParent }
        } else {
          emptyList()
        }
      }
      else -> emptyList()
    }

    if (rangesToInject.any()) {
      registrar.startInjecting(SHELL_LANGUAGE)
      rangesToInject.forEach {
        registrar.addPlace(null, null, context as PsiLanguageInjectionHost, it)
      }
      registrar.doneInjecting()
    }
  }
}