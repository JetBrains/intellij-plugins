// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import org.jetbrains.annotations.NotNull;

final class JdlEditorWithPreviewState implements FileEditorState {
  private final FileEditorState underlyingState;

  public JdlEditorWithPreviewState(FileEditorState underlyingState) {
    this.underlyingState = underlyingState;
  }

  @Override
  public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
    return otherState instanceof JdlEditorWithPreviewState
           && ((JdlEditorWithPreviewState)otherState).underlyingState.canBeMergedWith(underlyingState, level);
  }

  public FileEditorState getUnderlyingState() {
    return underlyingState;
  }
}
