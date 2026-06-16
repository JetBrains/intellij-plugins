package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.toNioPathOrNull
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.qodana.coverage.CoverageEngineType
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.streams.asSequence

interface QodanaCoverageFileProvider {
  val engineType: CoverageEngineType

  fun getCoverageFiles(project: Project): List<Path>
}

@ApiStatus.Internal
abstract class BaseQodanaCoverageFileProvider : QodanaCoverageFileProvider {

  /** Canonical extension the target engine's [com.intellij.coverage.CoverageRunner] accepts (e.g. "info" for LCOV). */
  protected abstract val canonicalExtension: String

  override fun getCoverageFiles(project: Project): List<Path> {
    val valid = getCoverageFilesLocations(project)
      .filter { isValidCoverageReport(it) }
    return copyToTempDir(project, valid)
  }

  /** Content sniff applied to every candidate: `true` only if [file] holds the format this engine parses. */
  protected abstract fun isValidCoverageReport(file: Path): Boolean

  protected abstract fun getCoverageFilesLocations(project: Project): List<Path>

  /**
   * Resolves candidate reports under every project anchor root as [names] x [dirs].
   *
   * Each [dirs] entry is a relative path whose segments are a literal directory name, a single-level `*` wildcard,
   * or a depth-bounded `**` (see [GLOBSTAR_MAX_DEPTH]). [names] are exact basenames (`lcov.info`) or `*.ext` globs.
   */
  protected fun discover(project: Project, names: List<String>, dirs: List<String>): List<Path> {
    val result = LinkedHashSet<Path>()
    for (root in anchorRoots(project)) {
      for (dir in dirs) {
        for (base in expandDir(root, dir)) {
          for (name in names) result += matchName(base, name)
        }
      }
    }
    return result.toList()
  }

  private fun copyToTempDir(project: Project, files: List<Path>): List<Path> {
    if (files.isEmpty()) return emptyList()
    val projectDir = project.guessProjectDir()?.toNioPathOrNull()
    val targetRoot = Files.createTempDirectory("qodana-coverage-$engineType")
    val taken = HashSet<Path>()
    val result = mutableListOf<Path>()
    for (file in files) {
      val relative = if (projectDir != null && file.startsWith(projectDir)) projectDir.relativize(file)
                     else Path.of(file.name)
      var destination = targetRoot.resolve(relative)
      if (!destination.extension.equals(canonicalExtension, ignoreCase = true)) {
        destination = destination.resolveSibling("${destination.name}.$canonicalExtension")
      }
      if (!taken.add(destination)) {
        logger.warn("Coverage report collision at $destination, dropping $file")
        continue
      }
      destination.parent?.createDirectories()
      file.copyTo(destination, overwrite = true)
      result.add(destination)
    }
    return result
  }
}

private fun anchorRoots(project: Project): List<Path> =
  ReadAction.computeBlocking<List<Path>, Throwable> {
    val rootManager = ProjectRootManager.getInstance(project)
    (rootManager.contentRoots.asSequence() + rootManager.contentSourceRoots.asSequence())
      .mapNotNull { it.toNioPathOrNull() }
      .distinct()
      .toList()
  }

private const val GLOBSTAR_MAX_DEPTH = 2

private fun expandDir(root: Path, dir: String): List<Path> {
  if (dir.isEmpty() || dir == ".") return listOf(root)
  var current = listOf(root)
  for (segment in dir.split('/')) {
    if (segment.isEmpty() || segment == ".") continue
    current = when (segment) {
      "*" -> current.flatMap { base ->
        if (base.isDirectory()) base.listDirectoryEntries().filter { it.isDirectory() } else emptyList()
      }
      "**" -> current.flatMap { base -> expandRecursively(base) }
      else -> current.map { it.resolve(segment) }
    }
    if (current.isEmpty()) break
  }
  return current.filter { it.isDirectory() }
}

private fun expandRecursively(base: Path): List<Path> {
  if (!base.isDirectory()) return emptyList()
  return Files.walk(base, GLOBSTAR_MAX_DEPTH).use { stream ->
    stream.asSequence().filter { it.isDirectory() }.toList()
  }
}

private fun matchName(base: Path, name: String): List<Path> {
  if (!base.isDirectory()) return emptyList()
  return if (name.startsWith("*.")) {
    val extension = name.substring(2)
    base.listDirectoryEntries().filter { it.isRegularFile() && it.extension.equals(extension, ignoreCase = true) }
  }
  else {
    val candidate = base.resolve(name)
    if (candidate.isRegularFile()) listOf(candidate) else emptyList()
  }
}
