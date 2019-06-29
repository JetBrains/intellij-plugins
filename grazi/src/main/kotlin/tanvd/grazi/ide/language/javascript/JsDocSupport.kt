package tanvd.grazi.ide.language.javascript

import com.intellij.lang.Language
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.psi.PsiElement
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class JsDocSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isSupported(language: Language): Boolean {
        return language is JSLanguageDialect
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is JSDocComment
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is JSDocComment) { "Got not JSDocComment in a JsDocSupport" }

        val langRanges = element.children.map { it.textRangeInParent }

        return SanitizingGrammarChecker.default.check(setOf(element), indexBasedIgnore = { _, index ->
            langRanges.any { it.contains(index) }
        })
    }
}
