package org.jetbrains.qodana.protocol

import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.readText
import kotlin.math.max

private val LOG = logger<OpenInIdeFileRegionLocator>()

internal class OpenInIdeFileRegionLocator(
  private val fileRelativePath: String,
  regionStartLine: Int, // 0-based
  offsetInLine: Int, // 0-based
  regionLength: Int,
  private val regionValidator: (String) -> Boolean
) {
  private val regionStartLine = max(regionStartLine, 0)
  private val offsetInLine = max(offsetInLine, 0)
  private val regionLength = max(regionLength, 0)

  suspend fun regionExistsInDirectory(directory: Path): Boolean {
    LOG.info("Searching in $directory for $fileRelativePath:${this@OpenInIdeFileRegionLocator.regionStartLine}:${this@OpenInIdeFileRegionLocator.offsetInLine}, length ${this@OpenInIdeFileRegionLocator.regionLength}")
    return withContext(QodanaDispatchers.Default) {
      val file = Path(directory.invariantSeparatorsPathString)
        .resolve(Path(fileRelativePath).invariantSeparatorsPathString)

      val fileContent = withContext(QodanaDispatchers.IO) {
        try {
          file.toRealPath().toAbsolutePath().readText()
        }
        catch (e : IOException) {
          LOG.info("Can't read file $file: ${e.message}")
          LOG.debug(e)
          return@withContext null
        }
      } ?: return@withContext false

      val lines = fileContent
        .lineSequence()
        .take(this@OpenInIdeFileRegionLocator.regionStartLine + 1)
        .toList()
      val line = lines.getOrNull(this@OpenInIdeFileRegionLocator.regionStartLine)
      if (line == null) {
        LOG.info("Can't find ${this@OpenInIdeFileRegionLocator.regionStartLine} line in file $file")
        return@withContext false
      }

      if (line.length < this@OpenInIdeFileRegionLocator.offsetInLine) {
        LOG.info("Line $line in file $file smaller than requested offset ${this@OpenInIdeFileRegionLocator.offsetInLine}")
        return@withContext false
      }

      val offsetInFileContent = lines
        .dropLast(1)
        .sumOf { it.length + 1 } + this@OpenInIdeFileRegionLocator.offsetInLine // +1 for \n
      val regionStart = offsetInFileContent
      val regionEnd = offsetInFileContent + this@OpenInIdeFileRegionLocator.regionLength
      val region = try {
        fileContent.substring(regionStart, regionEnd)
      }
      catch (_ : IndexOutOfBoundsException) {
        LOG.info("Can't get region [$regionStart, ${regionEnd}] in file $file (out of bounds)")
        return@withContext false
      }

      return@withContext regionValidator.invoke(region)
    }
  }
}