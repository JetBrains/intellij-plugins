package tanvd.grazi.ide.language.kotlin

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.*
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.*
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class KStringSupport : LanguageSupport(GraziBundle.langConfigSet("global.literal_string.disabled")) {

    companion object {
        fun isExpressionEntry(entry: KtStringTemplateEntry) = entry is KtStringTemplateEntryWithExpression || entry is KtSimpleNameStringTemplateEntry
    }

    override fun isSupported(file: PsiFile): Boolean {
        return file is KtFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (str in file.filterFor<KtStringTemplateExpression>()) {
            val ignoreFilter = IgnoreTokensFilter()
            val entries = str.filterFor<KtLiteralStringTemplateEntry>()

            ignoreFilter.populate<KtLiteralStringTemplateEntry, KtStringTemplateEntry>(entries, addSiblingIf = { isExpressionEntry(it) })

            addAll(ignoreFilter.filter(SanitizingGrammarChecker.default.check(entries)))

            ProgressManager.checkCanceled()
        }
    }
}
