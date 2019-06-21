package tanvd.grazi.ide.language.kotlin

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*

class KConstructsSupport : LanguageSupport() {
    override fun isSupported(language: Language): Boolean {
        return language is KotlinLanguage && GraziConfig.state.enabledSpellcheck
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is KtParameter || element is PsiNameIdentifierOwner
    }

    override fun check(element: PsiElement): Set<Typo> {
        return when (element) {
            is KtParameter -> {
                val function = (element.parent as? KtParameterList)?.parent as? KtNamedFunction
                if (function?.hasModifier(KtTokens.OVERRIDE_KEYWORD) == true) return emptySet()
                val paramName = element.name ?: return emptySet()
                element.text.ifContains(paramName) { index ->
                    GraziSpellchecker.check(paramName).map { typo ->
                        typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(index),
                                pointer = element.toPointer(), shouldUseRename = true))
                    }
                }
            }
            is PsiNameIdentifierOwner -> {
                if (element is KtScript ||
                        (element is KtModifierListOwner && element.hasModifier(KtTokens.OVERRIDE_KEYWORD))) return emptySet()

                val identName = element.name ?: return emptySet()
                element.text.ifContains(identName) { index ->
                    GraziSpellchecker.check(identName).map { typo ->
                        typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(index),
                                pointer = element.toPointer(), shouldUseRename = true))
                    }
                }
            }
            else -> {
                error("Got non named element in KConstructsFileSupport")
            }
        }.orEmpty().toSet()
    }
}
