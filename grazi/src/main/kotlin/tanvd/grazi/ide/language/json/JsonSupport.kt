package tanvd.grazi.ide.language.json

import com.intellij.json.JsonLanguage
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.*
import tanvd.kex.ifTrue
import tanvd.kex.orTrue

class JsonSupport : LanguageSupport() {
    companion object {
        val tagsIgnoredCategories = listOf(Typo.Category.CASING)

        val json = GrammarChecker(
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
        return language is JsonLanguage
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is JsonStringLiteral
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is JsonStringLiteral) { "Got non JsonStringLiteral in JsonSupport" }
        return json.check(element).filterNot { it.info.category in tagsIgnoredCategories }.toSet()
    }
}
