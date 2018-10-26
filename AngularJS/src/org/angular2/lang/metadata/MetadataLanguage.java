// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.json.psi.JsonValue;
import com.intellij.lang.DependentLanguage;
import com.intellij.lang.Language;
import com.intellij.psi.tree.IStubFileElementType;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.NotNull;

public abstract class MetadataLanguage extends Language implements DependentLanguage {

  protected MetadataLanguage(@NotNull String ID) {
    super(ID);
  }

  protected abstract IStubFileElementType getFileElementType();

  public abstract void createRootStub(MetadataFileStubImpl result, JsonValue value);
}
