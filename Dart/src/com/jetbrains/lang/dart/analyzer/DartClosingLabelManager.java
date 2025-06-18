// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.analyzer;

import com.intellij.codeInsight.hints.declarative.DeclarativeInlayHintsSettings;
import com.intellij.codeInsight.hints.declarative.impl.DeclarativeInlayHintsPassFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.jetbrains.lang.dart.hints.DartInlayHintsProvider;

import java.util.Arrays;

public class DartClosingLabelManager {
  public static DartClosingLabelManager getInstance() {
    return ApplicationManager.getApplication().getService(DartClosingLabelManager.class);
  }

  public void setShowClosingLabels(boolean value) {
    if (value != getShowClosingLabels()) {
      DeclarativeInlayHintsSettings.Companion.getInstance().setProviderEnabled(DartInlayHintsProvider.PROVIDER_ID, value);

      Arrays.stream(EditorFactory.getInstance().getAllEditors())
        .filter(editor -> editor.getProject() != null && DartAnalysisServerService.isLocalAnalyzableFile(editor.getVirtualFile()))
        .forEach(editor -> DeclarativeInlayHintsPassFactory.Companion.scheduleRecompute(editor, editor.getProject()));
    }
  }

  public boolean getShowClosingLabels() {
    return Boolean.TRUE.equals(DeclarativeInlayHintsSettings.Companion.getInstance().isProviderEnabled(DartInlayHintsProvider.PROVIDER_ID));
  }
}
