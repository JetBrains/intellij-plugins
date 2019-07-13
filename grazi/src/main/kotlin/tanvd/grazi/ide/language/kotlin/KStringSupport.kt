package tanvd.grazi.ide.language.kotlin

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.*
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterFor

class KStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    companion object {
        private fun isExpressionEntry(entry: KtStringTemplateEntry) = entry is KtStringTemplateEntryWithExpression || entry is KtSimpleNameStringTemplateEntry
    }

    override fun isRelevant(element: PsiElement) = element is KtStringTemplateExpression

    override fun check(element: PsiElement): Set<Typo> {
        require(element is KtStringTemplateExpression) { "Got not KtStringTemplateExpression in a KStringSupport" }

        val ignoreFilter = TokensFilter()
        val entries = element.filterFor<KtLiteralStringTemplateEntry>()

        ignoreFilter.populate<KtLiteralStringTemplateEntry, KtStringTemplateEntry>(entries, addSiblingIf = { isExpressionEntry(it) })

        return ignoreFilter.filter(GrammarChecker.default.check(entries))
    }
}
