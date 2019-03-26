package tanvd.grazi.grammar

import org.languagetool.rules.RuleMatch
import tanvd.grazi.model.Typo

val RuleMatch.typoCategory: Typo.Category
    get() = Typo.Category[rule.category.id.toString()]

fun RuleMatch.toIntRange(offset: Int = 0) = IntRange(fromPos + offset, toPos + offset)

fun IntRange.withOffset(offset: Int) = IntRange(start + offset, endInclusive + offset)

