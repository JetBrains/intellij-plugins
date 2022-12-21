// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata.psi;

import com.intellij.lang.Language;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.tree.IStubFileElementType;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.NotNull;

public class MetadataStubFileElementType extends IStubFileElementType<MetadataFileStubImpl> {

  public MetadataStubFileElementType(Language language) {
    super(language);
  }

  @Override
  public @NotNull String getExternalId() {
    return getLanguage() + ":" + toString();
  }

  @Override
  public @NotNull MetadataFileStubImpl deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) {
    return new MetadataFileStubImpl(null, this);
  }
}
