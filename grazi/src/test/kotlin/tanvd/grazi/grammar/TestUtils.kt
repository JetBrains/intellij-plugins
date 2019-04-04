package tanvd.grazi.grammar

import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun assertIsEmpty(collection: Collection<*>) {
    assertTrue { collection.isEmpty() }
}

fun Typo.assertTypoIs(category: Typo.Category, range: IntRange, fixes: List<String> = emptyList()) {
    assertEquals(category, info.category)
    assertEquals(range, location.range)
    assertTrue { fix?.containsAll(fixes) ?: false }
}
