package tanvd.grazi.model

import com.intellij.codeInspection.ProblemHighlightType

enum class TyposCategories(val value: String, val highlight: ProblemHighlightType = ProblemHighlightType.WEAK_WARNING) {
    /** Rules about detecting uppercase words where lowercase is required and vice versa.  */
    CASING("CASING"),

    /** Rules about spelling terms as one word or as as separate words.  */
    COMPOUNDING("COMPOUNDING"),

    GRAMMAR("GRAMMAR"),

    /** Spelling issues.  */
    TYPOS("TYPOS", ProblemHighlightType.LIKE_UNKNOWN_SYMBOL),

    PUNCTUATION("PUNCTUATION"),

    /** Problems like incorrectly used dash or quote characters.  */
    TYPOGRAPHY("TYPOGRAPHY"),

    /** Words that are easily confused, like 'there' and 'their' in English.  */
    CONFUSED_WORDS("CONFUSED_WORDS"),

    REPETITIONS("REPETITIONS"),

    REDUNDANCY("REDUNDANCY", ProblemHighlightType.LIKE_UNUSED_SYMBOL),

    /** General style issues not covered by other categories, like overly verbose wording.  */
    STYLE("STYLE"),

    GENDER_NEUTRALITY("GENDER_NEUTRALITY"),

    /** Logic, content, and consistency problems.  */
    SEMANTICS("SEMANTICS"),

    /** Colloquial style.  */
    COLLOQUIALISMS("COLLOQUIALISMS"),

    /** Regionalisms: words used only in another language variant or used with different meanings.  */
    REGIONALISMS("REGIONALISMS"),

    /** False friends: words easily confused by language learners because a similar word exists in their native language.  */
    FALSE_FRIENDS("FALSE_FRIENDS"),

    /** Rules that only make sense when editing Wikipedia (typically turned off by default in LanguageTool).  */
    WIKIPEDIA("WIKIPEDIA"),

    /** Miscellaneous rules that don't fit elsewhere.  */
    MISC("MISC"),

    OTHER("OTHER");

    companion object {
        operator fun get(value: String): TyposCategories {
            return values().find { it.value == value } ?: OTHER
        }
    }
}
