package com.intellij.dts.zephyr

import java.nio.file.Path

data class DtsZephyrBoard(val path: Path) {
  val name: String get() = path.fileName.toString()
  val arch: String? get() = path.parent?.fileName?.toString()
}