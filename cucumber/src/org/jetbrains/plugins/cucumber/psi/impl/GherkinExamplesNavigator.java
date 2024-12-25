// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * @author Roman.Chernyatchik
 */
public final class GherkinExamplesNavigator {
  public static @Nullable GherkinExamplesBlockImpl getExamplesByTable(final GherkinTableImpl table) {
    final PsiElement element = table.getParent();
    return element instanceof GherkinExamplesBlockImpl ? (GherkinExamplesBlockImpl)element : null;
  }
}
