// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.grammar

import tanvd.grazi.utils.withOffset
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun assertIsEmpty(collection: Collection<*>) {
    assertTrue { collection.isEmpty() }
}

fun Typo.verify(text: String? = null) {
    if (location.pointer != null) word
    if (location.pointer != null && text != null) {
        //it may work unexpectedly if there is more than one equal element, but it is ok for tests
        val indexOfElement = text.indexOf(location.pointer?.element!!.text)
        assertEquals(word, text.subSequence(location.range.withOffset(indexOfElement)))
    }
}

fun Typo.assertTypoIs(category: Typo.Category, range: IntRange, fixes: List<String> = emptyList(), text: String? = null) {
    assertEquals(category, info.category)
    assertEquals(range, location.range)
    assertTrue { fixes.containsAll(fixes) }

    verify(text)
}


