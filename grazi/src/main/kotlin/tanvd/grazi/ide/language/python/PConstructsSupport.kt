package tanvd.grazi.ide.language.python

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.jetbrains.python.PythonLanguage
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*

class PConstructsSupport : LanguageSupport() {

    override fun isSupported(language: Language): Boolean {
        return language is PythonLanguage && GraziConfig.state.enabledSpellcheck
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is PsiNamedElement
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is PsiNamedElement) { "Got non PsiNamedElement in a PConstructsSupport" }

        val identName = element.name ?: return emptySet()
        return element.text.ifContains(identName) { index ->
            GraziSpellchecker.check(identName).map { typo ->
                typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(index),
                        pointer = element.toPointer(), shouldUseRename = true))
            }
        }.orEmpty().toSet()
    }
}

