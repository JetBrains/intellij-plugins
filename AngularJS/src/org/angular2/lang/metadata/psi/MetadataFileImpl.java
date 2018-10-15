// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata.psi;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiFileEx;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiBinaryFileImpl;
import com.intellij.psi.impl.source.PsiFileWithStubSupport;
import com.intellij.psi.impl.source.StubbedSpine;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.stubs.StubTree;
import com.intellij.psi.stubs.StubTreeLoader;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.reference.SoftReference;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.NotNull;

public class MetadataFileImpl extends PsiBinaryFileImpl implements PsiFileWithStubSupport, PsiFileEx {

  private static final Logger LOG = Logger.getInstance(MetadataFileImpl.class);

  private volatile SoftReference<StubTree> myStub;
  private final Object myStubLock = new Object();
  private final Language myLanguage;

  public MetadataFileImpl(FileViewProvider fileViewProvider, Language language) {
    super((PsiManagerImpl)fileViewProvider.getManager(), fileViewProvider);
    myLanguage = language;
  }

  @NotNull
  @Override
  public Language getLanguage() {
    return myLanguage;
  }

  @Override
  public int getTextLength() {
    return 0;
  }

  @Override
  public int getStartOffsetInParent() {
    return 0;
  }

  @Override
  public int getTextOffset() {
    return 0;
  }

  @Override
  public TextRange getTextRange() {
    return TextRange.EMPTY_RANGE;
  }

  @Override
  @NotNull
  public StubTree getStubTree() {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    StubTree stubTree = SoftReference.dereference(myStub);
    if (stubTree != null) return stubTree;

    // build newStub out of lock to avoid deadlock
    StubTree newStubTree = (StubTree)StubTreeLoader.getInstance().readOrBuild(getProject(), getVirtualFile(), this);
    if (newStubTree == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("No stub for class file in index: " + getVirtualFile().getPresentableUrl());
      }
      ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(myLanguage);
      newStubTree = new StubTree(new MetadataFileStubImpl(this, (IStubFileElementType)parserDefinition.getFileNodeType()));
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

  @NotNull
  @Override
  public StubbedSpine getStubbedSpine() {
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
        ((PsiFileStubImpl)stubTree.getRoot()).clearPsi("metadata onContentReload");
      }
    }
  }

}
