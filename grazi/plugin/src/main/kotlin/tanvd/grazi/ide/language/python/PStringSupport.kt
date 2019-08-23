package tanvd.grazi.ide.language.python

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyFormattedStringElement
import com.jetbrains.python.psi.PyPlainStringElement
import com.jetbrains.python.psi.PyStringLiteralExpression
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.ide.language.LanguageSupport

class PStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isRelevant(element: PsiElement) = element is PyStringLiteralExpression && !element.isDocString

    override fun check(element: PsiElement) = GrammarChecker.default.check((element as PyStringLiteralExpression).stringElements,
            tokenRules = GrammarChecker.TokenRules(ignoreByIndex = linkedSetOf({ token, index ->
                when (token) {
                    is PyFormattedStringElement -> token.literalPartRanges.all { index !in it }
                    is PyPlainStringElement -> false
                    else -> false
                }
            })))
}
