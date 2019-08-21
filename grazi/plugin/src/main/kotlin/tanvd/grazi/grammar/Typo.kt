package tanvd.grazi.grammar

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.ProblemGroup
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.languagetool.rules.*
import org.slf4j.LoggerFactory
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangToolFixes
import tanvd.grazi.utils.*

@Suppress("unused")
data class Typo(val location: Location, val info: Info, val fixes: List<String> = emptyList()) {
    companion object {
        private val logger = LoggerFactory.getLogger(Typo::class.java)
    }

    data class Location(val range: IntRange, val pointer: PsiPointer<PsiElement>? = null, val shouldUseRename: Boolean = false) {
        val element: PsiElement?
            get() = pointer?.element

        fun withOffset(offset: Int) = copy(range = IntRange(range.start + offset, range.endInclusive + offset))

        fun isAtStart(skipWhitespace: Boolean = true): Boolean {
            var start = 0
            val element = pointer!!
            while (start < element.element!!.text.length && start !in range && (skipWhitespace && element.element!!.text[start].isWhitespace())) {
                start++
            }
            return start in range
        }

        fun isAtEnd(skipWhitespace: Boolean = true): Boolean {
            val element = pointer!!
            var end = element.element!!.text.length - 1
            while (end >= 0 && end !in range && (skipWhitespace && element.element!!.text[end].isWhitespace())) {
                end--
            }
            return end in range
        }


        /**
         * Checks if [innerElement] covers the start of typo location
         * Assumed that [pointer] at a typo is ancestor to passed [innerElement]
         */
        fun isAtStartOfInnerElement(innerElement: PsiElement): Boolean {
            require(PsiTreeUtil.isAncestor(pointer!!.element, innerElement, false))

            //delta between the innerElement and [pointer] that is ancestor for this innerElement.
            val delta = (innerElement.textRange.startOffset - pointer.element!!.textRange.startOffset)
            val rangeForElement = IntRange(range.start - delta, range.last - delta)

            var start = 0
            while (start < innerElement.text.length && start !in rangeForElement && innerElement.text[start].isWhitespace()) {
                start++
            }
            return start in rangeForElement
        }

        /**
         * Checks if [innerElement] covers the end of typo location
         * Assumed that [pointer] at a typo is ancestor to [innerElement]
         */
        fun isAtEndOfInnerElement(innerElement: PsiElement): Boolean {
            require(PsiTreeUtil.isAncestor(pointer!!.element, innerElement, false))

            //delta between the innerElement and [pointer] that is ancestor for this innerElement.
            val delta = (innerElement.textRange.startOffset - pointer.element!!.textRange.startOffset)
            val rangeForElement = IntRange(range.start - delta, range.last - delta)

            var end = innerElement.text.length - 1
            while (end >= 0 && end !in rangeForElement && innerElement.text[end].isWhitespace()) {
                end--
            }
            return end in rangeForElement
        }
    }

    data class Info(val lang: Lang, val rule: Rule, val match: RuleMatch, val category: Category) {
        val incorrectExample: IncorrectExample?
            get() {
                val withCorrections = rule.incorrectExamples.filter { it.corrections.isNotEmpty() }
                return (withCorrections.takeIf { it.isNotEmpty() } ?: rule.incorrectExamples).minBy { it.example.length }
            }
    }

    val word: String by lazy {
        try {
            location.pointer?.element!!.text.subSequence(location.range).toString()
        } catch (t : Throwable) {
            logger.warn("Got an exception during getting typo word:\n${location.pointer?.element!!.text}\n${info.match.sentence.text}")
            throw t
        }
    }

    /** Constructor for LangTool, applies fixes to RuleMatch (Main constructor doesn't apply fixes) */
    constructor(match: RuleMatch, lang: Lang, offset: Int = 0) : this(
            Location(match.toIntRange().withOffset(offset)),
            Info(lang, match.rule, match, match.typoCategory),
            match.suggestedReplacements.map { LangToolFixes.fixSuggestion(match.rule, it) }
    )

    enum class Category(val value: String, val description: String) : ProblemGroup {
        /** Rules about detecting uppercase words where lowercase is required and vice versa.  */
        CASING("CASING", "Wrong case"),

        /** Rules about spelling terms as one word or as separate words.  */
        COMPOUNDING("COMPOUNDING", "Compounding"),

        GRAMMAR("GRAMMAR", "Grammar"),

        /** Spelling issues.  */
        TYPOS("TYPOS", "Typo"),

        PUNCTUATION("PUNCTUATION", "Punctuation"),

        /** Problems like incorrectly used dash or quote characters.  */
        TYPOGRAPHY("TYPOGRAPHY", "Typography"),

        /** Words that are easily confused, like 'there' and 'their' in English.  */
        CONFUSED_WORDS("CONFUSED_WORDS", "Confused word"),

        REPETITIONS("REPETITIONS", "Repetition"),

        REDUNDANCY("REDUNDANCY", "Redundancy"),

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

        OTHER("OTHER", "Other mistake");

        val highlight: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING

        override fun getProblemName() = description

        companion object {
            operator fun get(value: String) = values().find { it.value == value } ?: OTHER
        }
    }
}
