// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.cli.config

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.minimatch.Minimatch
import com.intellij.util.text.minimatch.MinimatchOptions

/**
 * An `assets` entry in the normalized form of the Angular CLI.
 * The build copies each file under [input] that [accepts] to the [output] path of the application.
 *
 * @property output the path segments in the build output, relative to the output root
 */
data class AngularAssetPattern(
  val input: VirtualFile,
  val output: List<String>,
  val glob: String,
  val ignore: List<String>,
) {
  private val globMatcher: Minimatch? by lazy(LazyThreadSafetyMode.PUBLICATION) { createMatcher(glob) }

  private val ignoreMatchers: List<Minimatch> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    (DEFAULT_IGNORE + ignore).mapNotNull(::createMatcher)
  }

  /**
   * @param relativePath the path segments of a file, relative to [input]
   */
  fun accepts(relativePath: List<String>): Boolean {
    val path = relativePath.joinToString("/")
    return globMatcher?.match(path) == true && ignoreMatchers.none { it.match(path) }
  }

  /**
   * @param relativePath the path segments of a directory, relative to [input]
   * @return `true` if the glob can accept a file in the directory
   */
  fun acceptsDirectory(relativePath: List<String>): Boolean =
    globMatcher?.match(relativePath.joinToString("/"), partial = true) == true

  private companion object {
    // The Angular CLI never copies these files.
    val DEFAULT_IGNORE = listOf(".gitkeep", "**/.DS_Store", "**/Thumbs.db")

    fun createMatcher(glob: String): Minimatch? =
      try {
        Minimatch(glob.removePrefix("/"), MinimatchOptions(dot = true))
      }
      catch (_: Exception) {
        null
      }
  }
}

/**
 * Converts the `assets` entries the same way as `normalizeAssetPatterns` of the Angular CLI.
 *
 * A path string must be inside [sourceRoot]. The output of the path string is its location relative to [sourceRoot].
 * The input of an object entry is relative to [baseDir]. The default output of an object entry is the output root.
 *
 * @param sourceRoot the source root path, relative to [baseDir]
 */
internal fun normalizeAssetPatterns(
  patterns: List<AngularJsonAssetPattern>,
  baseDir: VirtualFile,
  sourceRoot: String,
): List<AngularAssetPattern> {
  val sourceRootSegments = toPathSegments(sourceRoot) ?: return emptyList()
  return patterns.mapNotNull { pattern ->
    when (pattern) {
      is AngularJsonAssetPath -> normalizeAssetPath(pattern.path, baseDir, sourceRootSegments)
      is AngularJsonAssetGlob -> normalizeAssetGlob(pattern, baseDir)
    }
  }
}

private fun normalizeAssetPath(path: String, baseDir: VirtualFile, sourceRootSegments: List<String>): AngularAssetPattern? {
  val segments = toPathSegments(path)
                   ?.takeIf { it.size >= sourceRootSegments.size && it.subList(0, sourceRootSegments.size) == sourceRootSegments }
                 ?: return null
  val file = baseDir.findFileByRelativePath(segments.joinToString("/"))
             ?: return null
  return if (file.isDirectory) {
    AngularAssetPattern(file, segments.drop(sourceRootSegments.size), "**/*", emptyList())
  }
  else {
    val parent = file.parent?.takeIf { segments.size > sourceRootSegments.size }
                 ?: return null
    AngularAssetPattern(parent, segments.subList(sourceRootSegments.size, segments.size - 1), file.name, emptyList())
  }
}

private fun normalizeAssetGlob(pattern: AngularJsonAssetGlob, baseDir: VirtualFile): AngularAssetPattern? {
  val glob = pattern.glob ?: return null
  val inputPath = pattern.input ?: return null
  val input = if (FileUtil.isAbsolute(inputPath))
    baseDir.fileSystem.findFileByPath(FileUtil.toSystemIndependentName(inputPath))
  else
    toPathSegments(inputPath)?.let { baseDir.findFileByRelativePath(it.joinToString("/")) }
  val output = toPathSegments(pattern.output ?: "")
               ?: return null
  return AngularAssetPattern(input?.takeIf { it.isDirectory } ?: return null, output, glob, pattern.ignore)
}

/**
 * @return the normalized path segments, or `null` if the path goes above its start location
 */
private fun toPathSegments(path: String): List<String>? {
  val result = mutableListOf<String>()
  for (segment in path.split('/', '\\')) {
    when (segment) {
      "", "." -> {}
      ".." -> if (result.isEmpty()) return null else result.removeLast()
      else -> result.add(segment)
    }
  }
  return result
}
