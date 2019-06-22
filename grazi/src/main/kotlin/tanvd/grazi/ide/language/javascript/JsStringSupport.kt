package tanvd.grazi.ide.language.javascript

import com.intellij.lang.Language
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.psi.PsiElement
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class JsStringSupport : LanguageSupport(GraziBundle.langConfigSet("global.literal_string.disabled")) {
    override fun isSupported(language: Language): Boolean {
        return language is JSLanguageDialect
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is JSLiteralExpression
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is JSLiteralExpression) { "Got not JSLiteralExpression in a JsStringSupport" }

        return when(element) {
            is JSStringTemplateExpression -> {
                val textRanges = element.stringRanges
                SanitizingGrammarChecker.default.check(setOf(element), indexBasedIgnore = { _, index ->
                    textRanges.all { !it.contains(index) }
                })
            }
            else -> {
                SanitizingGrammarChecker.default.check(setOf<PsiElement>(element))
            }
        }
    }
}
