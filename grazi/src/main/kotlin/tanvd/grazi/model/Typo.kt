package tanvd.grazi.model

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.ProblemGroup
import org.languagetool.rules.RuleMatch
import tanvd.grazi.grammar.toIntRange
import tanvd.grazi.grammar.typoCategory

data class Typo(val range: IntRange, val description: String, val category: Category, val fix: List<String>? = null) {
    constructor(match: RuleMatch) : this(match.toIntRange(), match.rule.description, match.typoCategory, match.suggestedReplacements)

    val fullDescription: String
        get() {
            if (description.isBlank())
                return category.description
            if (description.contains(":"))
                return description
            return "${category.description}: $description"
        }

    enum class Category(val value: String, val description: String,
                        val highlight: ProblemHighlightType = ProblemHighlightType.WEAK_WARNING) : ProblemGroup {
        /** Rules about detecting uppercase words where lowercase is required and vice versa.  */
        CASING("CASING", "Wrong case", ProblemHighlightType.LIKE_UNKNOWN_SYMBOL),

        /** Rules about spelling terms as one word or as as separate words.  */
        COMPOUNDING("COMPOUNDING", "Compounding"),

        GRAMMAR("GRAMMAR", "Grammar"),

        /** Spelling issues.  */
        TYPOS("TYPOS", "Typo", ProblemHighlightType.LIKE_UNKNOWN_SYMBOL),

        PUNCTUATION("PUNCTUATION", "Punctuation"),

        /** Problems like incorrectly used dash or quote characters.  */
        TYPOGRAPHY("TYPOGRAPHY", "Typography"),

        /** Words that are easily confused, like 'there' and 'their' in English.  */
        CONFUSED_WORDS("CONFUSED_WORDS", "Confused word"),

        REPETITIONS("REPETITIONS", "Repetition"),

        REDUNDANCY("REDUNDANCY", "Redundancy", ProblemHighlightType.LIKE_UNUSED_SYMBOL),

        /** General style issues not covered by other categories, like overly verbose wording.  */
        STYLE("STYLE", "Style"),

        GENDER_NEUTRALITY("GENDER_NEUTRALITY", "Gender neutrality"),

        /** Logic, content, and consistency problems.  */
        SEMANTICS("SEMANTICS", "Semantics"),

        /** Colloquial style.  */
        COLLOQUIALISMS("COLLOQUIALISMS", "Colloquialism"),

        /** Regionalisms: words used only in another language variant or used with different meanings.  */
        REGIONALISMS("REGIONALISMS", "Regionalism"),

        /** False friends: words easily confused by language learners because a similar word exists in their native language.  */
        FALSE_FRIENDS("FALSE_FRIENDS", "Other language word"),

        /** Rules that only make sense when editing Wikipedia (typically turned off by default in LanguageTool).  */
        WIKIPEDIA("WIKIPEDIA", "Wikipedia style"),

        /** Miscellaneous rules that don't fit elsewhere.  */
        MISC("MISC", "Miscellaneous"),

        OTHER("OTHER", "Typo");

        override fun getProblemName() = description

        companion object {
            operator fun get(value: String): Category {
                return values().find { it.value == value } ?: OTHER
            }
        }
    }
}
