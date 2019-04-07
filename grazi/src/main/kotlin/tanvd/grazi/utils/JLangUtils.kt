package tanvd.grazi.utils

import tanvd.grazi.grammar.Typo

fun Collection<Typo>.spellcheckOnly(): Collection<Typo> = filter { it.info.rule.isDictionaryBasedSpellingRule }
