// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.configurations.LocatableRunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;
import org.jetbrains.annotations.Nullable;

public final class BndRunConfigurationOptions extends LocatableRunConfigurationOptions {
  private final StoredProperty<String> bndRunFile = string(null).provideDelegate(this, "bndRunFile");
  private final StoredProperty<Boolean> useAlternativeJre = property(false).provideDelegate(this, "useAlternativeJre");
  private final StoredProperty<String> alternativeJrePath = string(null).provideDelegate(this, "alternativeJrePath");

  public final @Nullable String getBndRunFile() {
    return bndRunFile.getValue(this);
  }

  public final void setBndRunFile(@Nullable String file) {
    bndRunFile.setValue(this, file);
  }

  public final boolean getUseAlternativeJre() {
    return useAlternativeJre.getValue(this);
  }

  public final void setUseAlternativeJre(boolean use) {
    useAlternativeJre.setValue(this, use);
  }

  public final @Nullable String getAlternativeJrePath() {
    return alternativeJrePath.getValue(this);
  }

  public final void setAlternativeJrePath(@Nullable String path) {
    alternativeJrePath.setValue(this, path);
  }
}