package tanvd.grazi.ide.language.java

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*

class JConstructsSupport : LanguageSupport() {
    override fun isSupported(language: Language): Boolean {
        return language is JavaLanguage && GraziConfig.state.enabledSpellcheck
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is PsiMethod || element is PsiNamedElement
    }

    override fun check(element: PsiElement): Set<Typo> {
        return when (element) {
            is PsiMethod -> {
                val methodName = element.name
                element.text.ifContains(methodName) { index ->
                    GraziSpellchecker.check(methodName).map { typo ->
                        typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(index),
                                pointer = element.toPointer(), shouldUseRename = true))
                    }
                }

            }
            is PsiNamedElement -> {
                val identName = element.name ?: return emptySet()
                element.text.ifContains(identName) { index ->
                    GraziSpellchecker.check(identName).map { typo ->
                        typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(index),
                                pointer = element.toPointer(), shouldUseRename = true))
                    }
                }

            }
            else -> {
                error("Got non construct element in a JConstructsSupport")
            }
        }.orEmpty().toSet()
    }
}
