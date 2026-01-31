package com.jetbrains.lang.makefile

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.jetbrains.lang.makefile.psi.MakefileCommand
import com.jetbrains.lang.makefile.psi.MakefileFunction
import com.jetbrains.lang.makefile.psi.MakefileFunctionParam
import com.jetbrains.lang.makefile.psi.MakefileRecipe
import com.jetbrains.lang.makefile.psi.MakefileSubstitution

private const val SHELL_LANGUAGE_ID = "Shell Script"
private val SHELL_LANGUAGE = Language.findLanguageByID(SHELL_LANGUAGE_ID)

class MakefileShellLanguageInjector : MultiHostInjector {
  override fun elementsToInjectIn(): List<Class<out PsiElement>> {
    return mutableListOf(MakefileRecipe::class.java, MakefileFunction::class.java, MakefileSubstitution::class.java)
  }

  private fun isTab(c: Char): Boolean = c == '\t'

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    if (SHELL_LANGUAGE == null) return

    val rangesToInject = when (context) {
      is MakefileRecipe -> {
        context.children.filterIsInstance<MakefileCommand>().map {
          it.getShellCommandRange()
        }
      }
      is MakefileFunction -> {
        if (context.functionName.textMatches("shell")) {
          context.children.filterIsInstance<MakefileFunctionParam>().map { it.textRangeInParent }
        } else {
          emptyList()
        }
      }
      is MakefileSubstitution -> {
        if (context.textLength > 2) {
          listOf(TextRange(1, context.textLength - 1))
        } else emptyList()
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