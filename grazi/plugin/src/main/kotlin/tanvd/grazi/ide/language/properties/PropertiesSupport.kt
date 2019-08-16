package tanvd.grazi.ide.language.properties

import com.intellij.lang.properties.psi.impl.PropertyValueImpl
import com.intellij.psi.PsiElement
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class PropertiesSupport : LanguageSupport() {
    companion object {
        private val tagsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isRelevant(element: PsiElement) = element is PropertyValueImpl

    override fun check(element: PsiElement): Set<Typo> {
        require(element is PropertyValueImpl) { "Got non PropertyValueImpl in PropsSupport" }

        return GrammarChecker.default.check(element).filterNot { it.info.category in tagsIgnoredCategories }.toSet()
    }
}
