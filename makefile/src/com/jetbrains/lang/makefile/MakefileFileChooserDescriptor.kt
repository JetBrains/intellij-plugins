package name.kropp.intellij.makefile

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.vfs.*

class MakefileFileChooserDescriptor : FileChooserDescriptor(true, false, false, false, false, false) {
  init {
    title = "Makefile"
  }

  override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean) = when {
    !showHiddenFiles && FileElement.isFileHidden(file) -> false
    file.isDirectory -> true
    else -> file.fileType == MakefileFileType // file.name.endsWith(".mk") || file.name == "Makefile"
  }

  override fun isFileSelectable(file: VirtualFile) = !file.isDirectory && isFileVisible(file, true)
}