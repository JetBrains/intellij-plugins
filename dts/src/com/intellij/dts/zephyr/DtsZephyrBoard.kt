package com.intellij.dts.zephyr

import com.intellij.openapi.vfs.VirtualFile

data class DtsZephyrBoard(val file: VirtualFile) {
  val name: String get() = file.name
  val arch: String? get() = file.parent?.name
}