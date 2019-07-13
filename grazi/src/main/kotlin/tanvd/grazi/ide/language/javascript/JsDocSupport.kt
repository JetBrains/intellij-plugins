package tanvd.grazi.ide.language.javascript

import com.intellij.lang.javascript.psi.jsdoc.JSDocComment
import com.intellij.psi.PsiElement
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class JsDocSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isRelevant(element: PsiElement) = element is JSDocComment

    override fun check(element: PsiElement): Set<Typo> {
        require(element is JSDocComment) { "Got not JSDocComment in a JsDocSupport" }

        val langRanges = element.children.map { it.textRangeInParent }

        return GrammarChecker.default.check(setOf(element), indexBasedIgnore = { _, index ->
            langRanges.any { it.contains(index) }
        })
    }
}
