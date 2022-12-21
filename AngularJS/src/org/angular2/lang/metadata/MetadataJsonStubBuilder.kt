// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata

import com.intellij.json.JsonLanguage
import com.intellij.json.psi.impl.JsonFileImpl
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.stubs.BinaryFileStubBuilder
import com.intellij.psi.stubs.Stub
import com.intellij.util.indexing.FileContent
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl

class MetadataJsonStubBuilder : BinaryFileStubBuilder {

  override fun acceptsFile(file: VirtualFile): Boolean {
    return file.fileType is MetadataJsonFileType
  }

  override fun buildStubTree(fileContent: FileContent): Stub {
    val fileType = fileContent.fileType as MetadataJsonFileType

    val text = LoadTextUtil.getTextByBinaryPresentation(
      fileContent.content, fileContent.file)

    val jsonFile = PsiFileFactory
      .getInstance(fileContent.project)
      .createFileFromText(JsonLanguage.INSTANCE, text) as JsonFileImpl

    val result = MetadataFileStubImpl(null, fileType.fileElementType)
    jsonFile.topLevelValue?.let {
      fileType.createRootStub(result, it)
    }
    return result
  }

  override fun getStubVersion(): Int {
    return 22
  }
}
