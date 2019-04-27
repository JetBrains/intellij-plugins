package tanvd.grazi.utils

import tanvd.grazi.grammar.Typo

fun Collection<Typo>.spellcheckOnly(): Collection<Typo> = filter { it.isSpellingTypo }
val Typo.isSpellingTypo: Boolean
    get() = info.rule.isDictionaryBasedSpellingRule
