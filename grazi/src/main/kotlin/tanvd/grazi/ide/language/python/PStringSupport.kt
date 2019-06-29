package tanvd.grazi.ide.language.python

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.*
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport


class PStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isSupported(language: Language): Boolean {
        return language is PythonLanguage
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is PyStringLiteralExpression && !element.isDocString
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is PyStringLiteralExpression) { "Got non literal PyStringLiteralExpression in a PDocSupport" }
        return PUtils.python.check(element.stringElements, indexBasedIgnore = { token, index ->
            when (token) {
                is PyFormattedStringElement -> token.literalPartRanges.all { index !in it }
                is PyPlainStringElement -> false
                else -> false
            }
        })
    }
}
