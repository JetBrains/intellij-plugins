package tanvd.grazi.ide.language.rust

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import org.rust.lang.RsLanguage
import org.rust.lang.core.psi.ext.RsNameIdentifierOwner
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.*

class RsConstructsSupport : LanguageSupport() {
    override fun isSupported(language: Language): Boolean {
        return language is RsLanguage && GraziConfig.state.enabledSpellcheck
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is RsNameIdentifierOwner
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is RsNameIdentifierOwner) { "Got not RsNameIdentifierOwner in a RsConstructsSupport" }
        val identName = element.name ?: return emptySet()
        return element.text.ifContains(identName) { index ->
            GraziSpellchecker.check(identName).map { typo ->
                typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(index),
                        pointer = element.toPointer(), shouldUseRename = true))
            }
        }.orEmpty().toSet()
    }
}
