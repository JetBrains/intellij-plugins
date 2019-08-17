package tanvd.grazi.ide.language.javascript

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.psi.PsiElement
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class JsStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isRelevant(element: PsiElement) = element is JSLiteralExpression

    override fun check(element: PsiElement): Set<Typo> {
        require(element is JSLiteralExpression) { "Got not JSLiteralExpression in a JsStringSupport" }

        return when (element) {
            is JSStringTemplateExpression -> {
                val textRanges = element.stringRanges
                GrammarChecker.default.check(element, tokenRules = GrammarChecker.TokenRules(ignoreByIndex = linkedSetOf({ _, index ->
                    textRanges.all { !it.contains(index) }
                })))
            }
            else -> {
                GrammarChecker.default.check(element)
            }
        }
    }
}
