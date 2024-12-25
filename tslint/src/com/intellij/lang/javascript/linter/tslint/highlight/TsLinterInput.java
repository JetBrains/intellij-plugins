// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.lang.javascript.linter.JSLinterInput;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class TsLinterInput extends JSLinterInput<TsLintState> {

  private final @Nullable VirtualFile myConfig;

  public TsLinterInput(@NotNull PsiFile psiFile,
                       @NotNull TsLintState state,
                       @Nullable EditorColorsScheme colorsScheme,
                       @Nullable VirtualFile config) {
    super(psiFile, state, colorsScheme);
    myConfig = config;
  }

  public @Nullable VirtualFile getConfig() {
    return myConfig;
  }
}
