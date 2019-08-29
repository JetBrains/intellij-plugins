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
        //Ranges of elements that have been parsed by dialect (not general JS).
        //Mostly it is non-text elements, like parameters and document tags.
        //All of them should be considered as `inline elements` and must be ignored.
        val langRanges = element.children.map { it.textRangeInParent }

        return GrammarChecker.default.check(setOf(element), GrammarChecker.TokenRules(ignoreByIndex = linkedSetOf({ _, index ->
            langRanges.any { range -> index in range }
        })))
    }
}
