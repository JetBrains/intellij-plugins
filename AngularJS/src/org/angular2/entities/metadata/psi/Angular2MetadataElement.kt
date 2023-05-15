// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.StubElement
import org.angular2.entities.metadata.stubs.Angular2MetadataElementStub
import org.angular2.entities.metadata.stubs.Angular2MetadataNodeModuleStub
import org.angular2.lang.metadata.psi.MetadataElement
import org.jetbrains.annotations.NonNls

abstract class Angular2MetadataElement<Stub : Angular2MetadataElementStub<*>>(element: Stub) : MetadataElement<Stub>(element) {

  val nodeModule: Angular2MetadataNodeModule?
    get() {
      var stub: StubElement<*>? = stub
      while (stub != null && stub !is Angular2MetadataNodeModuleStub) {
        stub = stub.parentStub
      }
      return if (stub != null) stub.psi as Angular2MetadataNodeModule else null
    }

  override fun getText(): String? {
    return ""
  }

  override fun getTextLength(): Int {
    return 0
  }

  fun loadRelativeFile(path: String, extension: String): PsiFile? {
    return containingFile.viewProvider.virtualFile.parent?.let { baseDir -> loadRelativeFile(baseDir, path, extension) }
  }

  fun loadRelativeFile(baseDir: VirtualFile, path: String, extension: String): PsiFile? {
    val moduleFile = baseDir.findFileByRelativePath(path + extension)
    if (moduleFile != null) {
      return manager.findFile(moduleFile)
    }
    val moduleDir = baseDir.findFileByRelativePath(path)
    return if (moduleDir == null || !moduleDir.isDirectory) {
      null
    }
    else moduleDir.findChild(INDEX_FILE_NAME + extension)?.let { manager.findFile(it) }
  }

  companion object {

    @NonNls
    private val INDEX_FILE_NAME = "index"
  }
}
