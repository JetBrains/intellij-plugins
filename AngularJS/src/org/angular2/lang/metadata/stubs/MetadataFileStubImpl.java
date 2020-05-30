// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata.stubs;

import com.intellij.lang.Language;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.tree.IStubFileElementType;
import org.angular2.lang.metadata.psi.MetadataFileImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetadataFileStubImpl extends PsiFileStubImpl<MetadataFileImpl> implements PsiFileStub<MetadataFileImpl> {

  private final IStubFileElementType myType;

  public MetadataFileStubImpl(@Nullable MetadataFileImpl file, IStubFileElementType type) {
    super(file);
    myType = type;
  }

  @Override
  public void setPsi(@NotNull MetadataFileImpl psi) {
    super.setPsi(psi);
  }

  @Override
  public @NotNull IStubFileElementType getType() {
    return myType;
  }

  public Language getLanguage() {
    return getType().getLanguage();
  }
}