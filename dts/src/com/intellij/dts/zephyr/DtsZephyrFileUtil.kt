package com.intellij.dts.zephyr

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.findDirectory
import com.intellij.util.concurrency.ThreadingAssertions
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread

object DtsZephyrFileUtil {
  private const val BOARDS_PATH = "boards"
  private const val BINDINGS_PATH = "dts/bindings"
  private const val DEFAULT_PATH = "zephyrproject/zephyr"

  private val includePaths = listOf("include", "dts", "dts/common")

  fun isValid(root: VirtualFile?): Boolean {
    if (root == null || !root.isDirectory) return false

    // check if all expected directories exist
    for (expectedDirectory in listOf(BOARDS_PATH, BINDINGS_PATH) + includePaths) {
      val directory = root.findDirectory(expectedDirectory)
      if (directory == null || !directory.exists() || directory.children.isEmpty()) return false
    }

    return true
  }

  @RequiresBackgroundThread
  fun searchForRoot(project: Project): VirtualFile? {
    ThreadingAssertions.assertBackgroundThread()

    val candidates = mutableListOf<VirtualFile>()

    val visitor = object : VirtualFileVisitor<Any>(limit(2)) {
      override fun visitFile(file: VirtualFile): Boolean {
        ProgressManager.checkCanceled()

        if (isValid(file)) {
          candidates.add(file)
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
      val file = VfsUtil.findRelativeFile(VfsUtil.getUserHomeDir(), DEFAULT_PATH)

      if (file != null && isValid(file)) {
        candidates.add(file)
      }
    }

    return candidates.firstOrNull()
  }

  fun getAllBoardDirs(root: VirtualFile?): Sequence<String> {
    val boards = VfsUtil.findRelativeFile(root, BOARDS_PATH)
    if (boards == null || !boards.isDirectory) return emptySequence()

    return sequence {
      for (arch in boards.children.filter { it.isDirectory }) {
        if (arch.name == "common") continue

        for (board in arch.children.filter { it.isDirectory }) {
          if (board.findChild("board.cmake") == null) continue

          yield(board.path)
        }
      }
    }
  }

  fun getIncludeDirs(root: VirtualFile?, board: DtsZephyrBoard?): List<VirtualFile> {
    if (root == null) return emptyList()

    val includes = sequence {
      yieldAll(includePaths)

      board?.arch?.let { yield("dts/$it") }
    }

    return includes.mapNotNull(root::findDirectory).toList()
  }
}