package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.editor.impl.LineSet
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.LineColumn
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileCachedValue
import com.intellij.openapi.vfs.getCachedValue

/**
 * Text of a file as it is stored on disk, with an index of its line starts.
 *
 * A file gets one problem per highlighted element, so the conversion to SARIF asks for the same text many times.
 * [StringUtil.lineColToOffset] and [StringUtil.offsetToLineColumn] scan the text from its start on every call;
 * the [LineSet] of the text answers in `O(log lines)` with the same results.
 */
class FileText(val text: String) {
  private val lines: LineSet by lazy(LazyThreadSafetyMode.PUBLICATION) { LineSet.createLineSet(text) }

  /** Same as [StringUtil.lineColToOffset]: `-1` when the text has fewer lines. */
  fun lineColToOffset(line: Int, col: Int): Int {
    if (line < 0 || text.isEmpty()) return StringUtil.lineColToOffset(text, line, col)
    val lines = lines
    return if (line < lines.lineCount) lines.getLineStart(line) + col else -1
  }

  /** Same as [StringUtil.offsetToLineColumn]: `null` when [offset] is past the end of the text. */
  fun offsetToLineColumn(offset: Int): LineColumn? {
    // StringUtil gives a column of -1 for an offset between '\r' and '\n'; keep that answer.
    if (offset <= 0 || offset > text.length || offset < text.length && text[offset] == '\n' && text[offset - 1] == '\r') {
      return StringUtil.offsetToLineColumn(text, offset)
    }
    val lines = lines
    val line = lines.findLineIndex(offset)
    return LineColumn.of(line, offset - lines.getLineStart(line))
  }
}

private val FILE_TEXT_KEY: Key<VirtualFileCachedValue<FileText>> = Key.create("qodana.sarif.fileText")

/**
 * The text of [file], kept softly in the file until its content changes.
 *
 * It is the text on disk, line separators included: the SARIF offsets and snippets count `\r\n` as two characters.
 * Without a loaded document [getCachedValue] has already read the file with [VfsUtil.loadText], and that text is used.
 * The text of a loaded document has its separators converted to `\n`, so then the file is read from disk.
 */
fun qodanaFileText(file: VirtualFile?): FileText? {
  if (file == null || !file.isValid || file.isDirectory) return null
  return file.getCachedValue(FILE_TEXT_KEY, useSoftCache = true) { f, loaded ->
    val fromDisk = loaded is String && FileDocumentManager.getInstance().getCachedDocument(f) == null
    FileText(if (fromDisk) loaded.toString() else VfsUtil.loadText(f))
  }
}
