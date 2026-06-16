package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.coverage.CoverageFileProvider
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.annotations.ApiStatus
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.useLines

private val LOG = Logger.getInstance(CoverageFileProvider::class.java)

/** LCOV content check: looks for the `SF:` / `DA:` records [com.intellij.coverage.lcov.LcovSerializationUtils] keys on. */
@ApiStatus.Internal
fun isLcovReport(path: Path): Boolean {
  return try {
    path.useLines { lines ->
      var sourceFile = false
      var lineHit = false
      var scanned = 0
      for (line in lines) {
        if (scanned++ > 200) break
        if (line.startsWith("SF:")) sourceFile = true
        else if (line.startsWith("DA:")) lineHit = true
        if (sourceFile && lineHit) return@useLines true
      }
      false
    }
  }
  catch (e: IOException) {
    LOG.warn("Failed to read coverage report $path", e)
    false
  }
}

/** `<packages>` wrapper element. */
private val COBERTURA_PACKAGES_REGEX = Regex("""<packages\b""")

/** A single `<line>` element carrying both `number` and `hits` attributes, in any order (Cobertura / coverage.py). */
private val COBERTURA_LINE_REGEX = Regex("""<line\b(?=[^>]*\bnumber\s*=)(?=[^>]*\bhits\s*=)""")

/**
 * Cobertura-schema XML check (also matches coverage.py, which emits the same dialect): a `<coverage>` root wrapping
 * `<packages>`, with at least one `<line number=... hits=...>` record
 */
@ApiStatus.Internal
fun isCoberturaLikeXmlReport(path: Path): Boolean {
  val head = readReportHead(path) ?: return false
  if (!xmlRootElement(head).equals("coverage", ignoreCase = true)) return false
  return COBERTURA_PACKAGES_REGEX.containsMatchIn(head) && COBERTURA_LINE_REGEX.containsMatchIn(head)
}

private val XML_ROOT_REGEX = Regex("<([A-Za-z][\\w.:-]*)")

/** First real XML element name (skips the `<?xml?>` declaration, comments and `<!DOCTYPE>`), or `null` if none. */
@ApiStatus.Internal
fun xmlRootElement(head: String): String? = XML_ROOT_REGEX.find(head)?.groupValues?.get(1)

/** Reads up to [maxChars] characters from the start of [path] for content sniffing; `null` if unreadable. */
@ApiStatus.Internal
fun readReportHead(path: Path, maxChars: Int = 64 * 1024): String? {
  return try {
    Files.newBufferedReader(path).use { reader ->
      val buffer = CharArray(maxChars)
      var total = 0
      while (total < maxChars) {
        val read = reader.read(buffer, total, maxChars - total)
        if (read < 0) break
        total += read
      }
      String(buffer, 0, total)
    }
  }
  catch (e: IOException) {
    LOG.warn("Failed to read coverage report $path", e)
    null
  }
}