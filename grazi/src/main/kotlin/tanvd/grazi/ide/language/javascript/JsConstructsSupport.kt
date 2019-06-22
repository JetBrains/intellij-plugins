package tanvd.grazi.ide.language.javascript

import com.intellij.lang.Language
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*

class JsConstructsSupport : LanguageSupport() {
    override fun isSupported(language: Language): Boolean {
        return language is JSLanguageDialect && GraziConfig.state.enabledSpellcheck
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is PsiNamedElement
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is PsiNamedElement) { "Got non PsiNamedElement in a JsConstructsSupport" }

        val identName = element.name ?: return emptySet()
        return element.text.ifContains(identName) { index ->
            GraziSpellchecker.check(identName).map { typo ->
                typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(index),
                        pointer = element.toPointer(), shouldUseRename = true))
            }
        }.orEmpty().toSet()
    }
}
