// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.ObjectUtils;
import org.angular2.lang.metadata.stubs.MetadataElementStub;
import org.jetbrains.annotations.NotNull;

public abstract class MetadataElement<Stub extends MetadataElementStub> extends FakePsiElement implements StubBasedPsiElement<Stub> {

  private final Stub myStub;

  public MetadataElement(@NotNull Stub stub) {
    myStub = stub;
  }

  @Override
  public PsiElement getParent() {
    return ObjectUtils.doIfNotNull(myStub.getParentStub(), StubElement::getPsi);
  }

  @NotNull
  @Override
  public TextRange getTextRangeInParent() {
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

  public MetadataElement findMember(String name) {
    return (MetadataElement)ObjectUtils.doIfNotNull(getStub().findMember(name), MetadataElementStub::getPsi);
  }

}
