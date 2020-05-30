// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata.psi;

import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.metadata.stubs.MetadataElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MetadataElement<Stub extends MetadataElementStub<?>> extends FakePsiElement implements StubBasedPsiElement<Stub> {

  private final Stub myStub;
  private final AtomicNotNullLazyValue<PsiElement[]> children = new AtomicNotNullLazyValue<PsiElement[]>() {
    @Override
    protected PsiElement @NotNull [] compute() {
      return ContainerUtil.map2Array(getStub().getChildrenStubs(), PsiElement.class, s -> s.getPsi());
    }
  };

  public MetadataElement(@NotNull Stub stub) {
    myStub = stub;
  }

  @Override
  public PsiFile getContainingFile() {
    StubElement stub = myStub;
    while (stub != null && !(stub instanceof PsiFileStubImpl)) {
      stub = stub.getParentStub();
    }
    if (stub == null) {
      throw new PsiInvalidElementAccessException(this);
    }
    PsiFile result = ((PsiFileStubImpl)stub).getPsi();
    if (result == null) {
      throw new PsiInvalidElementAccessException(
        this, "Metadata file psi has been cleared: " + ((PsiFileStubImpl)stub).getInvalidationReason());
    }
    return result;
  }

  @Override
  public PsiElement getParent() {
    return ObjectUtils.doIfNotNull(myStub.getParentStub(), StubElement::getPsi);
  }

  @Override
  public @NotNull TextRange getTextRangeInParent() {
    return TextRange.EMPTY_RANGE;
  }

  @Override
  public IStubElementType getElementType() {
    return myStub.getStubType();
  }

  @Override
  public Stub getStub() {
    return myStub;
  }

  public MetadataElement findMember(@Nullable String name) {
    return (MetadataElement)ObjectUtils.doIfNotNull(getStub().findMember(name), MetadataElementStub::getPsi);
  }

  @Override
  public PsiElement @NotNull [] getChildren() {
    return children.getValue();
  }

  @Override
  public @Nullable PsiElement getFirstChild() {
    return ArrayUtil.getFirstElement(getChildren());
  }

  @Override
  public @Nullable PsiElement getLastChild() {
    return ArrayUtil.getLastElement(getChildren());
  }

  @Override
  public @Nullable PsiElement getNextSibling() {
    PsiElement parent = getParent();
    if (parent == null) {
      return null;
    }
    PsiElement[] children = parent.getChildren();
    int index = ArrayUtil.indexOf(children, this);
    return index >= 0 && index + 1 < children.length ? children[index + 1] : null;
  }

  @Override
  public @Nullable PsiElement getPrevSibling() {
    PsiElement parent = getParent();
    if (parent == null) {
      return null;
    }
    PsiElement[] children = parent.getChildren();
    int index = ArrayUtil.indexOf(children, this) - 1;
    return index >= 0 ? children[index] : null;
  }
}
