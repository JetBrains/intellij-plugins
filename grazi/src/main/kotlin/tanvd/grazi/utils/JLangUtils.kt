package tanvd.grazi.utils

import tanvd.grazi.grammar.Typo

fun Collection<Typo>.spellcheckOnly(): Set<Typo> = filter { it.isSpellingTypo }.toSet()
val Typo.isSpellingTypo: Boolean
    get() = info.rule.isDictionaryBasedSpellingRule
