// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata.psi

import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiInvalidElementAccessException
import com.intellij.psi.impl.PsiFileEx
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.file.PsiBinaryFileImpl
import com.intellij.psi.impl.source.PsiFileWithStubSupport
import com.intellij.psi.impl.source.StubbedSpine
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.stubs.StubTree
import com.intellij.psi.stubs.StubTreeLoader
import org.angular2.lang.metadata.MetadataJsonFileType
import org.angular2.lang.metadata.MetadataJsonLanguage
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl
import org.jetbrains.annotations.NonNls
import java.lang.ref.SoftReference

class MetadataFileImpl(fileViewProvider: FileViewProvider, private val myFileType: MetadataJsonFileType) : PsiBinaryFileImpl(
  fileViewProvider.manager as PsiManagerImpl, fileViewProvider), PsiFileWithStubSupport, PsiFileEx {

  @Volatile
  private var myStub: SoftReference<StubTree>? = null
  private val myStubLock = Any()

  override fun getContainingFile(): PsiFile {
    if (!isValid) throw PsiInvalidElementAccessException(this)
    return this
  }

  override fun getLanguage(): Language {
    return MetadataJsonLanguage.INSTANCE
  }

  override fun getChildren(): Array<PsiElement> {
    val root = stubTree.root as MetadataFileStubImpl
    return root.childrenStubs.map { it.psi }.toTypedArray<PsiElement>()
  }

  override fun getStubTree(): StubTree {
    ApplicationManager.getApplication().assertReadAccessAllowed()

    myStub?.get()?.let { return it }

    // build newStub out of lock to avoid deadlock
    var newStubTree = StubTreeLoader.getInstance().readOrBuild(project, virtualFile, this) as StubTree?
    if (newStubTree == null) {
      if (LOG.isDebugEnabled) {
        LOG.debug("No stub for class file in index: " + virtualFile.presentableUrl)
      }
      newStubTree = StubTree(MetadataFileStubImpl(this, myFileType.fileElementType))
    }

    synchronized(myStubLock) {
      myStub?.get()?.let { return it }

      val fileStub = newStubTree.root as PsiFileStubImpl<PsiFile>
      fileStub.setPsi(this)

      myStub = SoftReference(newStubTree)
      return newStubTree
    }
  }

  override fun getStubbedSpine(): StubbedSpine {
    return stubTree.spine
  }

  override fun isContentsLoaded(): Boolean {
    return myStub != null
  }

  override fun onContentReload() {
    ApplicationManager.getApplication().assertWriteAccessAllowed()

    synchronized(myStubLock) {
      val stubTree = myStub?.get()
      myStub = null
      if (stubTree != null) {

        (stubTree.root as PsiFileStubImpl<*>).clearPsi("metadata onContentReload")
      }
    }
  }

  @NonNls
  override fun toString(): String {
    return "MetadataFile:$name"
  }

  companion object {

    @NonNls
    private val LOG = Logger.getInstance(MetadataFileImpl::class.java)
  }
}
