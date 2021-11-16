package com.jetbrains.lang.makefile

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileElement
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.vfs.VirtualFile

class MakefileFileChooserDescriptor : FileChooserDescriptor(true, false, false, false, false, false) {
  init {
    title = MakefileLangBundle.message("file.chooser.title")
  }

  override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean) = when {
    !showHiddenFiles && FileElement.isFileHidden(file) -> false
    file.isDirectory -> true
    else -> FileTypeRegistry.getInstance().isFileOfType(file, MakefileFileType) // file.name.endsWith(".mk") || file.name == "Makefile"
  }

  override fun isFileSelectable(file: VirtualFile?) = file != null && !file.isDirectory && isFileVisible(file, true)
}