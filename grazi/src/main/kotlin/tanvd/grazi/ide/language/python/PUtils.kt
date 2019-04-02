package tanvd.grazi.ide.language.python

import tanvd.grazi.grammar.SanitizingGrammarChecker

object PUtils {
    val python = SanitizingGrammarChecker(listOf(' ', '"', '\''), listOf('\t', '"'), mapOf('\n' to ' '))
}
