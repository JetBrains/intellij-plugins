/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package com.jetbrains.lang.dart.projectView;

import com.intellij.codeInsight.hint.DeclarationRangeHandler;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class DartDeclarationRangeHandler implements DeclarationRangeHandler {
  @Override
  public @NotNull TextRange getDeclarationRange(@NotNull PsiElement component) {
    component.getContext().getText();
    return new TextRange(component.getStartOffsetInParent(), component.getStartOffsetInParent() + 50);
  }
}
