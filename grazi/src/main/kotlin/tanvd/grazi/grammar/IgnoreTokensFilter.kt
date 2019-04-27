package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import tanvd.grazi.utils.*

/**
 * Helper to set up masks to ignore inline elements in sequences of tokens.
 * Just use [populate] to add neighbours of inline elements to mask and then
 * use filter to filter out all typos, which may occurred because of inline elements.
 */
class IgnoreTokensFilter(private val ignoreSpellcheck: Boolean = false) {
    private val leftNeighbour = HashSet<PsiElement>()
    private val rightNeighbour = HashSet<PsiElement>()

    inline fun <T : PsiElement, reified E : PsiElement> populate(elements: Collection<T>, addSiblingIf: (E) -> Boolean) {
        for (element in elements) {
            val prevSibling = element.prevSibling
            if (prevSibling is E && addSiblingIf(prevSibling)) {
                addRight(element)
            }

            val nextSibling = element.nextSibling
            if (nextSibling is E && addSiblingIf(nextSibling)) {
                addLeft(element)
            }
        }
    }

    fun <T : PsiElement> populate(elements: Collection<T>, addAsLeftIf: (T) -> Boolean, addAsRightIf: (T) -> Boolean) {
        for (element in elements) {
            if (addAsLeftIf(element)) addLeft(element)
            if (addAsRightIf(element)) addRight(element)
        }
    }

    /** Register element as a left neighbour of inline element */
    fun addLeft(element: PsiElement) {
        leftNeighbour.add(element)
    }

    /** Register element as a right neighbour of inline element */
    fun addRight(element: PsiElement) {
        rightNeighbour.add(element)
    }

    fun filter(typos: Collection<Typo>): Set<Typo> = typos.filterNot { shouldIgnore(it) }.toSet()

    private fun shouldIgnore(typo: Typo): Boolean {
        if (!ignoreSpellcheck && typo.isSpellingTypo) {
            return false
        }

        val leftToken = leftNeighbour.firstOrNull { it == typo.location.element }
        val rightToken = rightNeighbour.firstOrNull { it == typo.location.element }

        if (leftToken == null && rightToken == null) {
            return false
        }

        var shouldIgnore = false

        if (leftToken != null) {
            shouldIgnore = shouldIgnore || typo.isAtEnd()
        }

        if (rightToken != null) {
            shouldIgnore = shouldIgnore || typo.isAtStart()
        }
        return shouldIgnore
    }

    private fun Typo.isAtStart(): Boolean {
        var start = 0
        val element = location.element!!
        while (start < element.text.length && start !in location.range && blankCharRegex.matches(element.text[start])) {
            start++
        }
        return start in location.range
    }

    private fun Typo.isAtEnd(): Boolean {
        val element = location.element!!
        var start = element.text.length - 1
        while (start >= 0 && start !in location.range && blankCharRegex.matches(element.text[start])) {
            start--
        }
        return start in location.range
    }
}
