package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.PsiPlainTextImpl
import tanvd.grazi.utils.withOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun assertIsEmpty(collection: Collection<*>) {
    assertTrue { collection.isEmpty() }
}

fun Typo.verify(text: String? = null) {
    if (location.element != null) word
    if (location.element != null && text != null) {
        //it may work unexpectedly if there is more than one equal element, but it is ok for tests
        val indexOfElement = text.indexOf(location.element!!.text)
        assertEquals(word, text.subSequence(location.range.withOffset(indexOfElement)))
    }
}

fun Typo.assertTypoIs(category: Typo.Category, range: IntRange, fixes: List<String> = emptyList(), text: String? = null) {
    assertEquals(category, info.category)
    assertEquals(range, location.range)
    assertTrue { fixes.containsAll(fixes) }

    verify(text)
}

class TestPlainText(text: String) : PsiPlainTextImpl(text)

fun plain(vararg texts: String) = plain(texts.toList())

fun plain(texts: List<String>): Collection<PsiElement> {
    return texts.map { TestPlainText(it) as PsiElement }
}

