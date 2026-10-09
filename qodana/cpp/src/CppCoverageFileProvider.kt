package org.jetbrains.qodana.cpp

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageFileProvider
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.useLines

internal object CppCoverageFileProvider : QodanaCoverageFileProvider {
  override val engineType: CoverageEngineType = CoverageEngineType.CMakeCoverageEngine

  override fun getCoverageFiles(project: Project): Map<Path, Path> = buildMap {
    putAll(GCovCoverageFileProvider.getCoverageFiles(project))
    putAll(LLVMCoverageFileProvider.getCoverageFiles(project))
  }
}

private object GCovCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.CMakeCoverageEngine
  override val canonicalExtension: String = "gcov"

  override fun isValidCoverageReport(file: Path): Boolean {
    return try {
      file.useLines { lines -> lines.take(20).any { GCOV_SOURCE_LINE.matches(it.trim()) } }
    }
    catch (e: IOException) {
      logger.warn("Failed to read GCOV report $file", e)
      false
    }
  }

  override fun getCoverageFilesLocations(project: Project): List<Path> =
    discover(project, names = listOf("*.gcov"), dirs = GCOV_REPORT_DIRECTORIES)
}

private object LLVMCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.CMakeCoverageEngine
  override val canonicalExtension: String = "json"

  override fun isValidCoverageReport(file: Path): Boolean {
    return try {
      Files.newByteChannel(file).use { channel ->
        val reportSize = channel.size()
        containsLlvmCoverageType(channel, 0) ||
        reportSize > LLVM_REPORT_CHECK_SIZE && containsLlvmCoverageType(channel, reportSize - LLVM_REPORT_CHECK_SIZE)
      }
    }
    catch (e: IOException) {
      logger.warn("Failed to read LLVM coverage report $file", e)
      false
    }
  }

  override fun getCoverageFilesLocations(project: Project): List<Path> =
    discover(project, names = listOf("*.json"), dirs = LLVM_REPORT_DIRECTORIES)
}

private fun containsLlvmCoverageType(channel: SeekableByteChannel, position: Long): Boolean {
  channel.position(position)
  val buffer = ByteBuffer.allocate(LLVM_REPORT_CHECK_SIZE)
  while (buffer.hasRemaining()) {
    if (channel.read(buffer) <= 0) break
  }
  buffer.flip()
  return UTF_8.decode(buffer).contains(LLVM_COVERAGE_TYPE)
}

private val logger = logger<CppCoverageFileProvider>()
private val GCOV_SOURCE_LINE = Regex("-:\\s*0:Source:.+")
private const val LLVM_COVERAGE_TYPE = "llvm.coverage.json.export"
private const val LLVM_REPORT_CHECK_SIZE = 4096
private val GCOV_REPORT_DIRECTORIES = listOf(
  ".", "coverage", "build", "build/**", "*/coverage", "*/Testing/CoverageInfo", "reports/gcov",
)
private val LLVM_REPORT_DIRECTORIES = listOf(
  ".", "coverage", "build", "build/**", "coverage-report", ".build/**/codecov",
)
