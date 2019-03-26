package tanvd.grazi.model

import org.languagetool.rules.RuleMatch

val RuleMatch.typoCategory: Typo.Category
    get() = Typo.Category[rule.category.id.toString()]

fun RuleMatch.toIntRange() = IntRange(fromPos, toPos)
