// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata

import com.intellij.json.psi.JsonValue
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IStubFileElementType
import org.angular2.entities.metadata.stubs.Angular2MetadataNodeModuleStub
import org.angular2.lang.metadata.MetadataJsonFileType
import org.angular2.lang.metadata.MetadataJsonLanguage
import org.angular2.lang.metadata.psi.MetadataStubFileElementType
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl
import org.jetbrains.annotations.NonNls

@NlsSafe
private const val ANGULAR_METADATA_LANGUAGE_NAME = "Angular Metadata JSON"

object Angular2MetadataFileType : MetadataJsonFileType() {

  override fun isMyFileType(file: VirtualFile): Boolean {
    val fileName = file.nameSequence
    if (!fileName.endsWith(METADATA_SUFFIX)) return false
    return file.parent?.takeIf { it.isValid }
      ?.findChild(fileName.subSequence(0, fileName.length - METADATA_SUFFIX.length).toString() + D_TS_SUFFIX) != null
  }

  override fun getName(): String = ANGULAR_METADATA_LANGUAGE_NAME
  override fun getDescription(): String = ANGULAR_METADATA_LANGUAGE_NAME
  override fun getDisplayName(): String = ANGULAR_METADATA_LANGUAGE_NAME

  override val fileElementType: IStubFileElementType<*>
    get() = FILE

  override fun createRootStub(fileStub: MetadataFileStubImpl, jsonRoot: JsonValue) {
    Angular2MetadataNodeModuleStub(fileStub, jsonRoot)
  }

  private val FILE = object : MetadataStubFileElementType(MetadataJsonLanguage) {
    override fun getStubVersion(): Int {
      return Angular2MetadataElementTypes.STUB_VERSION
    }
  }

  @NonNls
  internal val METADATA_SUFFIX = ".metadata.json"

  @NonNls
  internal val D_TS_SUFFIX = ".d.ts"
}