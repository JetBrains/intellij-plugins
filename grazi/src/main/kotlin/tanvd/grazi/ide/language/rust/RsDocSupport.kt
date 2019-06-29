package tanvd.grazi.ide.language.rust

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import org.rust.ide.injected.findDoctestInjectableRanges
import org.rust.lang.RsLanguage
import org.rust.lang.core.psi.RsDocCommentImpl
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.*
import tanvd.kex.ifTrue
import tanvd.kex.orTrue

class RsDocSupport : LanguageSupport() {
    companion object {
        //TODO-tanvd@undin Poor solution, but for better need support of Rust team
        val rust = GrammarChecker(
                ignore = listOf({ str, cur ->
                    (str.lastOrNull()?.let { blankOrNewLineCharRegex.matches(it) }.orTrue()
                            && blankOrNewLineCharRegex.matches(cur))
                            || (cur == '/')

                }, { _, cur -> cur == '\"' }),
                replace = listOf({ _, cur ->
                    newLineCharRegex.matches(cur).ifTrue { ' ' }
                }, { prev, cur ->
                    (cur == '#' && !prev.endsWith('#')).ifTrue { '\n' }
                }),
                ignoreToken = listOf({ str ->
                    str.all { !it.isLetter() }
                }))
    }

    override fun isSupported(language: Language): Boolean {
        return language is RsLanguage
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is RsDocCommentImpl
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is RsDocCommentImpl) { "Got not RsDocCommentImpl in a RsDocSupport" }
        val ranges = findDoctestInjectableRanges(element).flatten().toList()
        return rust.check(setOf(element),
                indexBasedIgnore = { _, index -> ranges.any { it.contains(index) } })
    }
}
