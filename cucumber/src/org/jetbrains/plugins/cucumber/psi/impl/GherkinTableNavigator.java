// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow;

/**
 * @author Roman.Chernyatchik
 */
public final class GherkinTableNavigator {
  private GherkinTableNavigator() {
  }

  public static @Nullable GherkinTableImpl getTableByRow(final GherkinTableRow row) {
    final PsiElement element = row.getParent();
    return element instanceof GherkinTableImpl ? (GherkinTableImpl)element : null;
  }
}
