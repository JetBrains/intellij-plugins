// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata

import com.intellij.json.psi.JsonValue
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IStubFileElementType
import org.angular2.entities.metadata.stubs.Angular2MetadataNodeModuleStub
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.metadata.MetadataJsonFileType
import org.angular2.lang.metadata.MetadataJsonLanguage
import org.angular2.lang.metadata.psi.MetadataStubFileElementType
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

class Angular2MetadataFileType private constructor() : MetadataJsonFileType() {

  override fun isMyFileType(file: VirtualFile): Boolean {
    val fileName = file.nameSequence
    if (!fileName.endsWith(METADATA_SUFFIX)) return false
    return file.parent?.takeIf { it.isValid }
      ?.findChild(fileName.subSequence(0, fileName.length - METADATA_SUFFIX.length).toString() + D_TS_SUFFIX) != null
  }

  override fun getName(): String {
    return "Angular Metadata JSON"
  }

  override fun getDescription(): String {
    return Angular2Bundle.message("filetype.angular-metadata-json.description")
  }

  @Nls
  override fun getDisplayName(): String {
    return Angular2Bundle.message("filetype.angular-metadata-json.display.name")
  }

  override val fileElementType: IStubFileElementType<*>
    get() = FILE

  override fun createRootStub(fileStub: MetadataFileStubImpl, jsonRoot: JsonValue) {
    Angular2MetadataNodeModuleStub(fileStub, jsonRoot)
  }

  companion object {
    @JvmField
    val INSTANCE = Angular2MetadataFileType()
    private val FILE = object : MetadataStubFileElementType(MetadataJsonLanguage.INSTANCE) {
      override fun getStubVersion(): Int {
        return Angular2MetadataElementTypes.STUB_VERSION
      }
    }

    @NonNls
    const val METADATA_SUFFIX = ".metadata.json"

    @NonNls
    const val D_TS_SUFFIX = ".d.ts"
  }
}
