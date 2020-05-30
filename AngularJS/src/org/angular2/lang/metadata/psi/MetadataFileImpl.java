// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata.psi;

import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.impl.PsiFileEx;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiBinaryFileImpl;
import com.intellij.psi.impl.source.PsiFileWithStubSupport;
import com.intellij.psi.impl.source.StubbedSpine;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.stubs.StubTree;
import com.intellij.psi.stubs.StubTreeLoader;
import com.intellij.reference.SoftReference;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.metadata.MetadataJsonFileType;
import org.angular2.lang.metadata.MetadataJsonLanguage;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class MetadataFileImpl extends PsiBinaryFileImpl implements PsiFileWithStubSupport, PsiFileEx {

  @NonNls private static final Logger LOG = Logger.getInstance(MetadataFileImpl.class);

  private volatile SoftReference<StubTree> myStub;
  private final Object myStubLock = new Object();

  private final MetadataJsonFileType myFileType;

  public MetadataFileImpl(FileViewProvider fileViewProvider, MetadataJsonFileType fileType) {
    super((PsiManagerImpl)fileViewProvider.getManager(), fileViewProvider);
    myFileType = fileType;
  }

  @Override
  public PsiFile getContainingFile() {
    if (!isValid()) throw new PsiInvalidElementAccessException(this);
    return this;
  }

  @Override
  public @NotNull Language getLanguage() {
    return MetadataJsonLanguage.INSTANCE;
  }

  @Override
  public PsiElement @NotNull [] getChildren() {
    MetadataFileStubImpl root = (MetadataFileStubImpl)getStubTree().getRoot();
    return ContainerUtil.map2Array(root.getChildrenStubs(), PsiElement.class, s -> s.getPsi());
  }

  @Override
  public @NotNull StubTree getStubTree() {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    StubTree stubTree = SoftReference.dereference(myStub);
    if (stubTree != null) return stubTree;

    // build newStub out of lock to avoid deadlock
    StubTree newStubTree = (StubTree)StubTreeLoader.getInstance().readOrBuild(getProject(), getVirtualFile(), this);
    if (newStubTree == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("No stub for class file in index: " + getVirtualFile().getPresentableUrl());
      }
      newStubTree = new StubTree(new MetadataFileStubImpl(this, myFileType.getFileElementType()));
    }

    synchronized (myStubLock) {
      stubTree = SoftReference.dereference(myStub);
      if (stubTree != null) return stubTree;

      stubTree = newStubTree;

      @SuppressWarnings("unchecked") PsiFileStubImpl<PsiFile> fileStub = (PsiFileStubImpl)stubTree.getRoot();
      fileStub.setPsi(this);

      myStub = new SoftReference<>(stubTree);
    }

    return stubTree;
  }

  @Override
  public @NotNull StubbedSpine getStubbedSpine() {
    return getStubTree().getSpine();
  }

  @Override
  public boolean isContentsLoaded() {
    return myStub != null;
  }

  @Override
  public void onContentReload() {
    ApplicationManager.getApplication().assertWriteAccessAllowed();

    synchronized (myStubLock) {
      StubTree stubTree = SoftReference.dereference(myStub);
      myStub = null;
      if (stubTree != null) {
        //noinspection HardCodedStringLiteral
        ((PsiFileStubImpl)stubTree.getRoot()).clearPsi("metadata onContentReload");
      }
    }
  }

  @Override
  @NonNls
  public String toString() {
    return "MetadataFile:" + getName();
  }
}
