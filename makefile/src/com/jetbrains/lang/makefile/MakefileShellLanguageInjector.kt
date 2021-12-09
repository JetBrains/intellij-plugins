package com.jetbrains.lang.makefile

import com.intellij.lang.*
import com.intellij.lang.injection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.jetbrains.lang.makefile.psi.*
import kotlin.math.*

private const val SHELL_LANGUAGE_ID = "Shell Script"
private val SHELL_LANGUAGE = Language.findLanguageByID(SHELL_LANGUAGE_ID)

class MakefileShellLanguageInjector : MultiHostInjector {
  override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
    return mutableListOf(MakefileRecipe::class.java, MakefileFunction::class.java, MakefileSubstitution::class.java)
  }

  private fun isTab(c: Char): Boolean = c == '\t'

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    if (SHELL_LANGUAGE == null) return

    val rangesToInject = when (context) {
      is MakefileRecipe -> {
        context.children.filterIsInstance<MakefileCommand>().map {
          val tabs = it.text.takeWhile(::isTab).count()
          val firstSymbol = it.text.dropWhile(::isTab).firstOrNull()
          val silent = if (firstSymbol == '@' || firstSymbol == '-') 1 else 0
          TextRange.create(it.textRangeInParent.startOffset + tabs + silent, min(it.textRangeInParent.endOffset, context.textLength))
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