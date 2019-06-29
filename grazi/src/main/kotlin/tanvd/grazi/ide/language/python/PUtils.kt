package tanvd.grazi.ide.language.python

import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.utils.*
import tanvd.kex.ifTrue
import tanvd.kex.orTrue

object PUtils {
    val python = GrammarChecker(
            ignore = listOf({ str, cur ->
                str.lastOrNull()?.let { blankCharRegex.matches(it) }.orTrue() && blankCharRegex.matches(cur)
            }, { prev, cur -> (cur == '\'' || cur == '\"') && (prev.isEmpty() || prev.last() == cur) }),
            replace = listOf({ _, cur ->
                newLineCharRegex.matches(cur).ifTrue { ' ' }
            }),
            ignoreToken = listOf({ str ->
                str.all { !it.isLetter() }
            }))
}
