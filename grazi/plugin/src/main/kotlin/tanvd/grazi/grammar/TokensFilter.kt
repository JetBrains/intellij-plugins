package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import tanvd.grazi.utils.isSpellingTypo
import tanvd.grazi.utils.toPointer

/**
 * Helper to set up masks to ignore inline elements in sequences of tokens.
 * Just use [populate] to add neighbours of inline elements to mask and then
 * use [filter] to filter out all typos, which may occur because of inline elements.
 */
class TokensFilter(private val ignoreSpellcheck: Boolean = false) {
    private val leftNeighbour = HashSet<SmartPsiElementPointer<PsiElement>>()
    private val rightNeighbour = HashSet<SmartPsiElementPointer<PsiElement>>()

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
        leftNeighbour.add(element.toPointer())
    }

    /** Register element as a right neighbour of inline element */
    fun addRight(element: PsiElement) {
        rightNeighbour.add(element.toPointer())
    }

    fun filter(typos: Collection<Typo>): Set<Typo> = typos.filterNot { shouldIgnore(it) }.toSet()

    private fun shouldIgnore(typo: Typo): Boolean {
        if (!ignoreSpellcheck && typo.isSpellingTypo) {
            return false
        }

        val leftToken = leftNeighbour.firstOrNull { it == typo.location.pointer }
        val rightToken = rightNeighbour.firstOrNull { it == typo.location.pointer }

        if (leftToken == null && rightToken == null) {
            return false
        }

        var shouldIgnore = false

        if (leftToken != null) {
            shouldIgnore = shouldIgnore || typo.location.isAtEnd()
        }

        if (rightToken != null) {
            shouldIgnore = shouldIgnore || typo.location.isAtStart()
        }
        return shouldIgnore
    }
}
