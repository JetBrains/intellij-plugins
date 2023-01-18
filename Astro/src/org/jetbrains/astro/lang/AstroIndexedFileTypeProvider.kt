// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.lang.javascript.index.IndexedFileTypeProvider
import com.intellij.openapi.fileTypes.FileType

class AstroIndexedFileTypeProvider : IndexedFileTypeProvider {
  override fun getFileTypesToIndex(): Array<FileType> = arrayOf(AstroFileType.INSTANCE)
}
