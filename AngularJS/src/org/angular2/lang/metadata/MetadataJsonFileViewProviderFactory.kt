// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import org.angular2.lang.metadata.psi.MetadataFileImpl

class MetadataJsonFileViewProviderFactory : FileViewProviderFactory {

  override fun createFileViewProvider(file: VirtualFile,
                                      language: Language?,
                                      manager: PsiManager,
                                      eventSystemEnabled: Boolean): FileViewProvider {
    return MetadataFileViewProvider(manager, file, eventSystemEnabled)
  }

  class MetadataFileViewProvider internal constructor(manager: PsiManager,
                                                      file: VirtualFile,
                                                      eventSystemEnabled: Boolean)
    : SingleRootFileViewProvider(manager, file, eventSystemEnabled, MetadataJsonLanguage.INSTANCE) {
    init {
      assert(file.fileType is MetadataJsonFileType)
    }

    override fun createFile(project: Project, file: VirtualFile, fileType: FileType): PsiFile {
      return MetadataFileImpl(this, fileType as MetadataJsonFileType)
    }

    override fun createCopy(copy: VirtualFile): SingleRootFileViewProvider {
      return MetadataFileViewProvider(manager, copy, false)
    }
  }
}
