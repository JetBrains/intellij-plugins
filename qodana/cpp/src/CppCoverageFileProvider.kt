package org.jetbrains.qodana.cpp

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageFileProvider
import java.io.IOException
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
    discover(project, names = listOf("*.gcov"), dirs = COVERAGE_DIRECTORIES)
}

private object LLVMCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.CMakeCoverageEngine
  override val canonicalExtension: String = "json"

  override fun isValidCoverageReport(file: Path): Boolean {
    return try {
      Files.newBufferedReader(file).use { reader ->
        val buffer = CharArray(LLVM_HEADER_SIZE)
        val charsRead = reader.read(buffer)
        charsRead > 0 && String(buffer, 0, charsRead).contains(LLVM_COVERAGE_TYPE)
      }
    }
    catch (e: IOException) {
      logger.warn("Failed to read LLVM coverage report $file", e)
      false
    }
  }

  override fun getCoverageFilesLocations(project: Project): List<Path> =
    discover(project, names = listOf("*.json"), dirs = COVERAGE_DIRECTORIES)
}

private val logger = logger<CppCoverageFileProvider>()
private val GCOV_SOURCE_LINE = Regex("-:\\s*0:Source:.+")
private const val LLVM_COVERAGE_TYPE = "llvm.coverage.json.export"
private const val LLVM_HEADER_SIZE = 4096
private val COVERAGE_DIRECTORIES = listOf(".", "coverage", ".coverage", "build", "build/**", "*/**")
