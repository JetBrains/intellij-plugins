package tanvd.grazi.ide.language.json

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class JsonSupport : LanguageSupport() {
    companion object {
        private val tagsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    override fun isRelevant(element: PsiElement) = element is JsonStringLiteral

    override fun check(element: PsiElement): Set<Typo> {
        require(element is JsonStringLiteral) { "Got non JsonStringLiteral in JsonSupport" }
        return GrammarChecker.ignoringQuotes.check(element).filterNot { it.info.category in tagsIgnoredCategories }.toSet()
    }
}
