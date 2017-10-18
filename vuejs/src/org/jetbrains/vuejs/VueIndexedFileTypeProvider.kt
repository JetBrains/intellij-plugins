package org.jetbrains.vuejs

import com.intellij.lang.javascript.index.IndexedFileTypeProvider
import com.intellij.openapi.fileTypes.FileType

/**
 * @author Irina.Chernushina on 10/18/2017.
 */
class VueIndexedFileTypeProvider : IndexedFileTypeProvider {
  override fun getFileTypesToIndex(): Array<FileType> = arrayOf(VueFileType.INSTANCE)
}