package com.intellij.dts.zephyr

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.*
import java.nio.file.Path

data class ZephyrBoard(val arch: String, val name: String) {
   companion object {
      fun unmarshal(marshalled: String): ZephyrBoard? {
         val split = marshalled.split('/')
         if (split.size != 2) return null

          val (arch, name) = split
          if (arch.isBlank() || name.isBlank()) return null

          return ZephyrBoard(arch, name)
      }
   }

    fun marshal(): String = "$arch/$name"
}

object DtsZephyrRoot {
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

    fun searchForRoot(project: Project): VirtualFile? {
        val candidates = mutableListOf<VirtualFile>()

        val visitor = object : VirtualFileVisitor<Any>(limit(2)) {
            override fun visitFile(file: VirtualFile): Boolean {
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
            val localFileSystem = LocalFileSystem.getInstance()
            val path = Path.of(System.getProperty("user.home"), DEFAULT_PATH)
            val file = localFileSystem.findFileByNioFile(path)

            if (file != null && isValid(file)) {
                candidates.add(file)
            }
        }

        return candidates.firstOrNull()
    }

    private fun getBoardsDir(root: VirtualFile?): VirtualFile? {
        return root?.findDirectory(BOARDS_PATH)
    }

    fun getAllBoards(root: VirtualFile?): Sequence<ZephyrBoard> {
        val boards = getBoardsDir(root)
        if (boards == null) return emptySequence()

        return sequence {
            for (arch in boards.children.filter { it.isDirectory }) {
                if (arch.name == "common") continue

                for (board in arch.children.filter { it.isDirectory }) {
                    if (board.findChild("board.cmake") == null) continue

                    yield(ZephyrBoard(arch.name, board.name))
                }
            }
        }
    }

    fun getBindingsDir(root: VirtualFile?): VirtualFile? {
        return root?.findDirectory(BINDINGS_PATH)
    }

    fun getBoardDir(root: VirtualFile?, board: ZephyrBoard?): VirtualFile? {
        if (board == null) return null

        val boards = getBoardsDir(root) ?: return null
        return boards.findDirectory("${board.arch}/${board.name}")
    }

    fun getIncludeDirs(root: VirtualFile?, board: ZephyrBoard?): List<VirtualFile> {
        if (root == null) return emptyList()

        val includes = sequence {
            yieldAll(includePaths)
            board?.let { yield("dts/${it.arch}") }
        }

        return includes.mapNotNull(root::findDirectory).toList()
    }
}