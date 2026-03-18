package com.intellij.dts.zephyr

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.util.concurrency.ThreadingAssertions
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.isDirectory
import kotlin.io.path.name

object DtsZephyrFileUtil {
  private const val BOARDS_PATH = "boards"
  private const val BINDINGS_PATH = "dts/bindings"
  private const val DEFAULT_PATH = "zephyrproject/zephyr"

  private val includePaths = listOf("include", "dts", "dts/common")

  fun isValid(root: Path?): Boolean {
    if (root == null || !Files.isDirectory(root)) return false

    for (expectedDirectory in listOf(BOARDS_PATH, BINDINGS_PATH) + includePaths) {
      val directory = root.resolve(expectedDirectory)
      if (!Files.isDirectory(directory)) return false

      val hasChildren = try {
        Files.list(directory).use { it.findFirst().isPresent }
      }
      catch (_: IOException) {
        false
      }
      if (!hasChildren) return false
    }

    return true
  }

  @RequiresBackgroundThread
  fun searchForRoot(project: Project): Path? {
    ThreadingAssertions.assertBackgroundThread()

    val candidates = mutableListOf<Path>()

    val visitor = object : VirtualFileVisitor<Any>(limit(2)) {
      override fun visitFile(file: VirtualFile): Boolean {
        ProgressManager.checkCanceled()

        val path = Path.of(file.path)

        if (isValid(path)) {
          candidates.add(path)
          return false
        }

        return true
      }
    }

    // search project roots
    for (module in project.modules) {
      val rootManger = ModuleRootManager.getInstance(module)

      for (root in rootManger.contentRoots) {
        VfsUtilCore.visitChildrenRecursively(root, visitor)
      }
    }

    // search default installation directory
    if (candidates.isEmpty()) {
      val defaultPath = Path.of(System.getProperty("user.home"), DEFAULT_PATH)
      if (isValid(defaultPath)) {
        candidates.add(defaultPath)
      }
    }

    return candidates.firstOrNull()
  }

  fun getAllBoardDirs(root: Path?): Sequence<Path> {
    if (root == null) return emptySequence()

    val boards = root.resolve(BOARDS_PATH)
    if (!Files.isDirectory(boards)) return emptySequence()

    return sequence {
      for (arch in Files.list(boards).filter { it.isDirectory() }) {
        if (arch.name == "common") continue

        for (board in Files.list(arch).filter { it.isDirectory() }) {
          if (Files.list(board).anyMatch { it.name == "board.cmake"}) {
            yield(board)
          }
        }
      }
    }
  }

  fun getIncludeDirs(root: Path?, board: DtsZephyrBoard?): List<VirtualFile> {
    if (root == null) return emptyList()

    val includes = sequence {
      yieldAll(includePaths)

      board?.arch?.let { yield("dts/$it") }
    }

    return includes.mapNotNull { subPath ->
      val resolved = root.resolve(subPath)
      if (Files.isDirectory(resolved)) VfsUtil.findFile(resolved, false) else null
    }.toList()
  }
}