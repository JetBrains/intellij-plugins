package org.jetbrains.qodana.poly

import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.GeneratedSourcesFilter
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.SingleRootFileViewProvider

private val GENERATED_NAMES = setOf(
  "package-lock.json",
  "npm-shrinkwrap.json",
  "yarn.lock",
  "pnpm-lock.yaml",
  "bun.lock",
  "bun.lockb",
  "go.sum",
  "go.work.sum",
)

private val GENERATED_SUFFIXES = listOf(".pb.go", ".pb.gw.go", ".map")

private val MINIFIABLE_EXTENSIONS = setOf("js", "jsx", "mjs", "cjs", "ts", "tsx", "css", "scss", "less")

private const val SAMPLE_LIMIT = 4096  // JSMinifiedFileGistService.INITIAL_FILE_READ_LIMIT
private const val MAX_OFFSET = 2048  // MinifiedFilesUtil.MAX_OFFSET
private const val MIN_SIZE = 150  // MinifiedFilesUtil.MIN_SIZE
private const val TAIL_LIMIT = 400  // MinifiedFilesUtil.COUNT_OF_CONSIDERING_CHARACTERS_FROM_END_OF_FILE
private const val MAX_UNNEEDED_WHITESPACE_RATIO = 0.01  // MinifiedFilesUtil.MAX_UNNEEDED_OFFSET_PERCENTAGE

/**
 * The character stand-in for the token sets that `JSMinifiedFileUtil` passes: `JSTokenTypes.OPERATIONS`,
 * the brace, paren, dot and semicolon types, and `JSTokenTypes.STRING_LITERALS`.
 */
private const val PUNCTUATION = "{}[]()<>;:,.=+-*/%!&|^~?\"'`"

/** The same pattern as `GoGeneratedSourcesFilter.GENERATED_PATTERN`. */
private val GO_GENERATED_MARKER = Regex("""^// Code generated .* DO NOT EDIT\.$""")

private val CONTENT_VERDICT = Key.create<Pair<Long, Boolean>>("qodana.poly.generated.by.content")

internal fun isGeneratedFileName(name: String): Boolean =
  name in GENERATED_NAMES ||
  name.contains(".min.") ||
  GENERATED_SUFFIXES.any { name.endsWith(it) }

/**
 * Go puts the marker before the first line that is not a comment. `GoFilePropertiesParser` therefore
 * tests every line comment in that region, and so does this function.
 */
internal fun hasGoGeneratedMarker(text: CharSequence): Boolean {
  for (line in text.lineSequence()) {
    val trimmed = line.trimEnd()
    if (trimmed.isBlank()) continue
    if (!trimmed.startsWith("//") && !trimmed.startsWith("/*") && !trimmed.startsWith("*")) return false
    if (GO_GENERATED_MARKER.matches(trimmed)) return true
  }
  return false
}

/**
 * The `MinifiedFilesUtil` rule for the start of a file, ported to a text scanner because Qodana Poly
 * has no `ParserDefinition`.
 *
 * A comment never counts, so a license banner cannot skew the result. A string length stays out of the
 * ratio, so a base64 blob cannot skew it either. A whitespace run longer than one char rejects the text,
 * so indentation alone disqualifies it. Whitespace next to [PUNCTUATION] counts as unneeded, which
 * separates minified code from unindented code that still has normal spacing.
 */
internal fun isMinifiedText(text: CharSequence): Boolean = scan(text, scoreSize = true)

/**
 * The same scan for the end of a file, which `MinifiedFilesUtil` also reads.
 *
 * It applies the whitespace-run rule alone. The size and the ratio need a whole file to mean anything,
 * and the text starts at an arbitrary offset, so [scan] would misread them.
 */
internal fun isMinifiedTail(text: CharSequence): Boolean = scan(text, scoreSize = false)

private fun scan(text: CharSequence, scoreSize: Boolean): Boolean {
  var i = 0
  var nonComment = 0
  var nonCommentNonString = 0
  var unneeded = 0
  var previous = ' '
  var previousWasComment = false

  while (i < text.length && nonComment < MAX_OFFSET) {
    val c = text[i]
    when {
      c == '/' && text.startsWith("//", i) -> {
        i = endOfLineComment(text, i)
        previousWasComment = true
      }
      c == '/' && text.startsWith("/*", i) -> {
        i = endOfBlockComment(text, i)
        previousWasComment = true
      }
      c == '"' || c == '\'' || c == '`' -> {
        val end = endOfString(text, i)
        nonComment += end - i
        previous = c
        previousWasComment = false
        i = end
      }
      c.isWhitespace() -> {
        var end = i
        while (end < text.length && text[end].isWhitespace()) end++
        if (end - i > 1 && !previousWasComment && end < text.length) return false
        nonComment += end - i
        nonCommentNonString += end - i
        val next = if (end < text.length) text[end] else ' '
        if (previous in PUNCTUATION || next in PUNCTUATION) unneeded++
        previousWasComment = false
        i = end
      }
      else -> {
        nonComment++
        nonCommentNonString++
        previous = c
        previousWasComment = false
        i++
      }
    }
  }

  if (!scoreSize) return true
  if (nonCommentNonString == 0) return false
  return nonComment >= MIN_SIZE && unneeded.toDouble() / nonCommentNonString < MAX_UNNEEDED_WHITESPACE_RATIO
}

private fun endOfLineComment(text: CharSequence, start: Int): Int {
  val end = text.indexOf('\n', start)
  return if (end < 0) text.length else end + 1
}

private fun endOfBlockComment(text: CharSequence, start: Int): Int {
  val end = text.indexOf("*/", start + 2)
  return if (end < 0) text.length else end + 2
}

/**
 * Only a template literal crosses a line break. A `'` or a `"` that never closes on its line is not a
 * string at all, and it sits in a regular expression, in JSX text, or in a typo. The scanner must give
 * the line break back, or the swallowed text would hide the indentation that rejects the file.
 */
private fun endOfString(text: CharSequence, start: Int): Int {
  val quote = text[start]
  var i = start + 1
  while (i < text.length) {
    when {
      text[i] == '\\' -> i += 2
      text[i] == quote -> return i + 1
      text[i] == '\n' && quote != '`' -> return i
      else -> i++
    }
  }
  return text.length
}

/**
 * Marks a generated file in Qodana Poly, which bundles no language plugin and therefore gets none of
 * the filters that `JSGeneratedSourcesFilter`, `CssMinifiedFilesFilter`, `GoGeneratedSourcesFilter`,
 * `GoSumGeneratedSourcesFilter`, `JavaGeneratedSourcesFilter` and `SourceMapGeneratedSourcesFilter`
 * contribute elsewhere.
 */
class QodanaPolyGeneratedSourcesFilter : GeneratedSourcesFilter() {
  override fun isGeneratedSource(file: VirtualFile, project: Project): Boolean {
    if (file.isDirectory || !file.isValid || project.isDefault) return false
    if (isGeneratedFileName(file.name)) return true
    if (ProjectFileIndex.getInstance(project).isInGeneratedSources(file)) return true
    return hasGeneratedContent(file)
  }

  private fun hasGeneratedContent(file: VirtualFile): Boolean {
    val extension = file.extension?.lowercase()
    val wantsMarker = extension == "go"
    val wantsMinified = extension in MINIFIABLE_EXTENSIONS && !file.name.contains(".dev.")
    if (!wantsMarker && !wantsMinified) return false

    val cached = CONTENT_VERDICT.get(file)
    val timeStamp = file.timeStamp
    if (cached != null && cached.first == timeStamp) return cached.second

    val verdict = if (wantsMarker) hasGoGeneratedMarker(readText(file, SAMPLE_LIMIT)) else isMinifiedFile(file)
    CONTENT_VERDICT.set(file, timeStamp to verdict)
    return verdict
  }

  private fun isMinifiedFile(file: VirtualFile): Boolean {
    if (!isMinifiedText(readText(file, SAMPLE_LIMIT))) return false
    // The file end separates a minified library from a minified library plus formatted code. Only a
    // file that already looks minified pays this read, and Qodana then leaves that file unanalyzed.
    if (SingleRootFileViewProvider.isTooLargeForContentLoading(file)) return true
    val text = readText(file, limit = null)
    val start = text.length - TAIL_LIMIT
    return start <= 0 || isMinifiedTail(text.subSequence(start, text.length))
  }

  /**
   * The same read as `JSMinifiedFileGistService.readFileContentPrefix`. It strips the BOM, it detects
   * the charset, and it normalizes the line separator. [isMinifiedText] needs that last part, because a
   * `\r\n` would otherwise read as a whitespace run of two chars and reject every file.
   */
  private fun readText(file: VirtualFile, limit: Int?): CharSequence =
    try {
      if (limit == null) LoadTextUtil.loadText(file) else LoadTextUtil.loadText(file, limit)
    }
    catch (_: Exception) {
      ""
    }
}
