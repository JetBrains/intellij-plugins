// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.lang.javascript.index.IndexedFileTypeProvider
import com.intellij.openapi.fileTypes.FileType

class VueIndexedFileTypeProvider : IndexedFileTypeProvider {
  override fun getFileTypesToIndex(): Array<FileType> = arrayOf(VueFileType)
}
