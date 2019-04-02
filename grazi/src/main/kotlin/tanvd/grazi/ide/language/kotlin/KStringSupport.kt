package tanvd.grazi.ide.language.kotlin

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.*
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class KStringSupport : LanguageSupport {
    private val disabledRules = setOf("UPPERCASE_SENTENCE_START")

    override fun isSupported(file: PsiFile): Boolean {
        return file is KtFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        for (str in file.filterFor<KtStringTemplateEntry>()) {
            addAll(SanitizingGrammarChecker.default.check(str.filterFor<KtLiteralStringTemplateEntry>()).filter{ it.info.rule.id !in disabledRules })

            ProgressManager.checkCanceled()
        }
    }
}
