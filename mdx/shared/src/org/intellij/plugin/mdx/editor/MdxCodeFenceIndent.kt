package org.intellij.plugin.mdx.editor

import com.intellij.openapi.editor.Document

/**
 * Whatever the fence's opening [line] carries in front of its backticks. Every line of the fence carries the same,
 * so this is what gets stripped for the sandbox and put back afterwards.
 *
 * Indentation — spaces or tabs — and the `>` markers of a blockquote around the fence carry over as they are. A
 * list bullet does not: repeating it would start a second item, so it stands in as blank filler of its own width,
 * which is exactly the item's content indent. `MarkdownCodeFenceUtils.getIndent` does it the same way.
 */
fun fenceIndent(document: Document, line: Int): String {
  val text = document.immutableCharSequence
  val lineStart = document.getLineStartOffset(line)
  val lineEnd = document.getLineEndOffset(line)
  var end = lineStart
  while (end < lineEnd && text[end] != '`' && text[end] != '~') end++
  return buildString {
    for (offset in lineStart until end) {
      val char = text[offset]
      append(if (char == ' ' || char == '\t' || char == '>') char else ' ')
    }
  }
}
