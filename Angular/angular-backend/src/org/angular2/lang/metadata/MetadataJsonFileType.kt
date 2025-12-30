// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata

import com.intellij.json.psi.JsonValue
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.psi.tree.IStubFileElementType
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl
import javax.swing.Icon

abstract class MetadataJsonFileType protected constructor() : FileType, FileTypeIdentifiableByVirtualFile {

  abstract val fileElementType: IStubFileElementType<*>

  override fun getDefaultExtension(): String {
    return "json"
  }

  override fun getIcon(): Icon? {
    return null
  }

  override fun isBinary(): Boolean {
    return true
  }

  override fun isReadOnly(): Boolean {
    return true
  }

  abstract fun createRootStub(fileStub: MetadataFileStubImpl, jsonRoot: JsonValue)
}
