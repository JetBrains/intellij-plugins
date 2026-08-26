package org.intellij.plugin.mdx.lang.parse

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.text.StringUtil

/** Makes every MDX source scan cancellable, including work performed inside platform lexers. */
internal fun mdxCancellableText(text: CharSequence): CharSequence {
  ProgressManager.checkCanceled()
  return (text as? MdxCancellableCharSequence) ?: MdxCancellableCharSequence(text)
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
