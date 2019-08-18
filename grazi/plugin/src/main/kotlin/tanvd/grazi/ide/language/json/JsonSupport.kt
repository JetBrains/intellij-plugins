package tanvd.grazi.ide.language.json

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterNotToSet

class JsonSupport : LanguageSupport() {
    companion object {
        private val ignoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isRelevant(element: PsiElement) = element is JsonStringLiteral

    override fun check(element: PsiElement): Set<Typo> {
        require(element is JsonStringLiteral) { "Got non JsonStringLiteral in JsonSupport" }

        return GrammarChecker.default.check(element).filterNotToSet { it.info.category in ignoredCategories }
    }
}
