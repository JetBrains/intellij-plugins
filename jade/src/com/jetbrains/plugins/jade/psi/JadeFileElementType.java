// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi;

import com.intellij.lang.Language;
import com.intellij.psi.tree.IStubFileElementType;
import com.jetbrains.plugins.jade.JadeLanguage;
import org.jetbrains.annotations.NotNull;

public class JadeFileElementType extends IStubFileElementType {
  public JadeFileElementType() {
    super("File:" + getMyLanguage().getDisplayName(), getMyLanguage());
  }

  private static Language getMyLanguage() {
    return JadeLanguage.INSTANCE;
  }

  @Override
  public @NotNull String getExternalId() {
    return getLanguage().getID() + ".file";
  }
}
