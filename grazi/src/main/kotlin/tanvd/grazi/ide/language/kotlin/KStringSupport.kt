package tanvd.grazi.ide.language.kotlin

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.*
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.*
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterFor

class KStringSupport : LanguageSupport(GraziBundle.langConfigSet("global.literal_string.disabled")) {
    companion object {
        fun isExpressionEntry(entry: KtStringTemplateEntry) = entry is KtStringTemplateEntryWithExpression || entry is KtSimpleNameStringTemplateEntry
    }

    override fun isSupported(language: Language): Boolean {
        return language is KotlinLanguage
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is KtStringTemplateExpression
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is KtStringTemplateExpression) { "Got not KtStringTemplateExpression in a KStringSupport" }

        val ignoreFilter = IgnoreTokensFilter()
        val entries = element.filterFor<KtLiteralStringTemplateEntry>()

        ignoreFilter.populate<KtLiteralStringTemplateEntry, KtStringTemplateEntry>(entries, addSiblingIf = { isExpressionEntry(it) })

        return ignoreFilter.filter(SanitizingGrammarChecker.default.check(entries))
    }
}
