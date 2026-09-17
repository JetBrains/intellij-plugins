package org.jetbrains.qodana.poly

import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.GeneratedSourcesFilter
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.SingleRootFileViewProvider
import org.jetbrains.qodana.poly.util.CodeLineScanner

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
private const val MIN_SIZE = 150  // MinifiedFilesUtil.MIN_SIZE
private const val MIN_LONG_LINE = 120
private const val MAX_OFFSET = 2048  // MinifiedFilesUtil.MAX_OFFSET
/**
 * Minified code must space a keyword from its operand, and a short token pushes that cost up.
 * `new A;` repeated reaches 0.167, while a formatted one-line text starts at 0.25.
 */
private const val MAX_SPACE_RATIO = 0.20

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
 * Reports whether [text] is minified.
 *
 * A minified text meets all four requirements. A comment and a string count for nothing,
 * and a line that keeps no code votes on nothing.
 *
 *  1. The text keeps at least [MIN_SIZE] chars of code.
 *  2. At least half of the lines that keep code reach [MIN_LONG_LINE] chars of code.
 *  3. No line separates its code with a whitespace run longer than one char, inside the first
 *     [MAX_OFFSET] chars of code.
 *  4. Whitespace takes at most [MAX_SPACE_RATIO] of the text.
 *
 * Requirement 1 drops a tiny file.
 * Requirement 2 counts half of the lines, and not the mean length, so one long line among short lines decides nothing.
 * A license banner and a base64 blob therefore decide nothing either.
 * Requirement 3 is the `MinifiedFilesUtil` rule, and its [MAX_OFFSET] cap is the same one.
 * A bundle can indent a later chunk, so the cap keeps that chunk out of the judgment.
 * Requirement 4 rejects a formatted text that keeps one long line, where the first three see nothing wrong.
 */
internal fun isMinifiedText(text: CharSequence): Boolean {
  val scanner = CodeLineScanner()
  var code = 0
  var spaces = 0
  var lines = 0
  var longLines = 0

  for (raw in text.lineSequence()) {
    val line = scanner.scan(raw)
    if (line.code == 0) continue
    if (line.widestGap > 1 && code < MAX_OFFSET) return false
    code += line.code
    spaces += line.spaces
    lines++
    if (line.code >= MIN_LONG_LINE) longLines++
  }

  if (lines == 0 || code < MIN_SIZE) return false
  if (spaces.toDouble() / (code + spaces) > MAX_SPACE_RATIO) return false
  return longLines * 2 >= lines
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
    if (SingleRootFileViewProvider.isTooLargeForContentLoading(file)) return true
    return isMinifiedText(readText(file, limit = null))
  }

  /**
   * The same read as `JSMinifiedFileGistService.readFileContentPrefix`. It strips the BOM and it
   * detects the charset from the content, which a raw byte read cannot do. It also normalizes the line
   * separator, though [isMinifiedText] gives the same verdict either way.
   */
  private fun readText(file: VirtualFile, limit: Int?): CharSequence =
    try {
      if (limit == null) LoadTextUtil.loadText(file) else LoadTextUtil.loadText(file, limit)
    }
    catch (_: Exception) {
      ""
    }
}
