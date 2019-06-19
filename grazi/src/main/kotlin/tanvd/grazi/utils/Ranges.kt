package tanvd.grazi.utils

import com.intellij.openapi.util.TextRange
import tanvd.grazi.grammar.Typo

fun Typo.toSelectionRange(): TextRange {
    val end = if (location.pointer?.element!!.textLength >= location.range.endInclusive + 1) {
        location.range.endInclusive + 1
    } else {
        location.range.endInclusive
    }
    return location.element!!.textRange.cutOut(TextRange(location.range.start, end))
}
