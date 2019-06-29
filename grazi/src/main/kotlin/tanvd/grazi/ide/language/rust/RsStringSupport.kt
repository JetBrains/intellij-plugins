package tanvd.grazi.ide.language.rust

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import org.rust.lang.RsLanguage
import org.rust.lang.core.psi.RsLitExpr
import org.rust.lang.core.psi.ext.stubKind
import org.rust.lang.core.stubs.RsStubLiteralKind
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.*
import tanvd.kex.ifTrue
import tanvd.kex.orTrue

class RsStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    companion object {
        val rust = GrammarChecker(
                ignore = listOf({ str, cur ->
                    str.lastOrNull()?.let { blankOrNewLineCharRegex.matches(it) }.orTrue() && blankOrNewLineCharRegex.matches(cur)
                }, { _, cur -> cur == '\"' }),
                replace = listOf({ _, cur ->
                    newLineCharRegex.matches(cur).ifTrue { ' ' }
                }),
                ignoreToken = listOf({ str ->
                    str.all { !it.isLetter() }
                }))

    }

    override fun isSupported(language: Language): Boolean {
        return language is RsLanguage
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is RsLitExpr && element.stubKind is RsStubLiteralKind.String
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is RsLitExpr) { "Got not RsLitExpr in a RsStringSupport" }

        return rust.check(element)
    }
}
