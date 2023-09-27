package com.intellij.dts.zephyr

import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.name

data class DtsZephyrBoard(val path: String) {
    val name: String?
    val arch: String?

    init {
        val path = try {
            Path.of(path)
        } catch (_: InvalidPathException) {
            null
        }

        name = path?.name
        arch = path?.parent?.name
    }

    val presentableText: @NlsSafe String?
        get() {
            if (name == null) return null
            if (arch == null) return name

            return "$arch/$name"
        }

    val virtualFile: VirtualFile?
        get() {
            return try {
                VfsUtil.findFile(Path.of(path), true)
            } catch (_: InvalidPathException) {
                null
            }
        }
}