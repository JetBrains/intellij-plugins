// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


public class GherkinElementType extends IElementType {
  public GherkinElementType(@NotNull @NonNls String debugName) {
    super(debugName, GherkinLanguage.INSTANCE);
  }
}
