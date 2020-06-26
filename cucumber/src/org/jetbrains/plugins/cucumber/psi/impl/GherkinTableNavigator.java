// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi.impl;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow;
import com.intellij.psi.PsiElement;

/**
 * @author Roman.Chernyatchik
 * @date Sep 10, 2009
 */
public final class GherkinTableNavigator {
  private GherkinTableNavigator() {
  }

  @Nullable
  public static GherkinTableImpl getTableByRow(final GherkinTableRow row) {
    final PsiElement element = row.getParent();
    return element instanceof GherkinTableImpl ? (GherkinTableImpl)element : null;
  }
}
