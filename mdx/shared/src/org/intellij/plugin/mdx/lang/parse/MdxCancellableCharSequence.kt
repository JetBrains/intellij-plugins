package org.intellij.plugin.mdx.lang.parse

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.text.StringUtil

/** Makes every MDX source scan cancellable, including work performed inside platform lexers. */
internal fun mdxCancellableText(text: CharSequence): CharSequence {
  ProgressManager.checkCanceled()
  return (text as? MdxCancellableCharSequence) ?: MdxCancellableCharSequence(text)
}

internal fun mdxLineStart(text: CharSequence, offset: Int): Int {
  val source = mdxCancellableText(text)
  var lineStart = offset.coerceAtMost(source.length)
  while (lineStart > 0 && source[lineStart - 1] != '\n') {
    lineStart--
  }
  return lineStart
}

internal fun mdxSmallIndent(text: CharSequence, lineStart: Int, offset: Int): Int {
  val source = mdxCancellableText(text)
  var indent = 0
  while (lineStart + indent < offset && source[lineStart + indent] == ' ') {
    indent++
    if (indent > 3) return -1
  }
  return indent
}

private class MdxCancellableCharSequence(
  text: CharSequence,
) : StringUtil.BombedCharSequence(text) {
  override fun checkCanceled() {
    ProgressManager.checkCanceled()
  }

  override fun toString(): String {
    val size = length
    return buildString(size) {
      for (index in 0..<size) {
        append(this@MdxCancellableCharSequence[index])
      }
    }
  }
}
