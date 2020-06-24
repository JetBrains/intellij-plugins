// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.CfmlPsiElement;
import com.intellij.lang.Language;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 */
public abstract class CfmlStubElementType<S extends StubElement<?>, T extends CfmlPsiElement> extends IStubElementType<S, T> {
  public CfmlStubElementType(@NotNull @NonNls final String debugName, @Nullable final Language language) {
    super(debugName, CfmlLanguage.INSTANCE);
  }

  @Override
  @NotNull
  public String getExternalId() {
    return "cfml." + super.toString();
  }

  @Override
  public void indexStub(@NotNull S stub, @NotNull IndexSink sink) {
  }
}
