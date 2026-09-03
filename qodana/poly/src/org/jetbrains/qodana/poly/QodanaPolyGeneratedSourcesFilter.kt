package org.jetbrains.qodana.poly

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.GeneratedSourcesFilter
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException

private val GENERATED_NAMES = setOf(
  "package-lock.json",
  "yarn.lock",
  "pnpm-lock.yaml",
  "bun.lock",
  "bun.lockb",
  "go.sum",
  "go.work.sum",
)

private val MINIFIABLE_EXTENSIONS = setOf("js", "jsx", "mjs", "cjs", "ts", "tsx", "css", "scss", "less")

private val GENERATED_MARKER = Regex("""^// Code generated .* DO NOT EDIT\.$""")

private const val SAMPLE_LIMIT = 4096
private const val MIN_SAMPLE_LENGTH = 150
private const val MIN_MEAN_LINE_LENGTH = 120

private val CONTENT_VERDICT = Key.create<Pair<Long, Boolean>>("qodana.poly.generated.by.content")

/** A lock file, or a name that says the file is minified. */
internal fun isGeneratedFileName(name: String): Boolean = name in GENERATED_NAMES || name.contains(".min.")

/** The marker that `go generate` and protoc write on the first line. */
internal fun hasGeneratedMarker(text: String): Boolean = GENERATED_MARKER.matches(text.lineSequence().first().trimEnd())

/** A stand-in for `MinifiedFilesUtil`, which needs a `ParserDefinition` that Qodana Poly does not have. */
internal fun isMinifiedText(text: String): Boolean {
  val trimmed = text.trimEnd()
  return trimmed.length >= MIN_SAMPLE_LENGTH &&
         trimmed.length / trimmed.lineSequence().count() >= MIN_MEAN_LINE_LENGTH
}

/**
 * Marks a generated file in Qodana Poly, which bundles no language plugin and therefore gets none of
 * the filters that `JSGeneratedSourcesFilter`, `CssMinifiedFilesFilter`, `GoGeneratedSourcesFilter`,
 * `GoSumGeneratedSourcesFilter`, `JavaGeneratedSourcesFilter` and `KotlinGeneratedSourcesFilter`
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

    val sample = readSample(file)
    val verdict = sample != null && (wantsMarker && hasGeneratedMarker(sample) || wantsMinified && isMinifiedText(sample))
    CONTENT_VERDICT.set(file, timeStamp to verdict)
    return verdict
  }

  private fun readSample(file: VirtualFile): String? =
    try {
      file.inputStream.use { String(it.readNBytes(SAMPLE_LIMIT), file.charset) }
    }
    catch (_: IOException) {
      null
    }
}
