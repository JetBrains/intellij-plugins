// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;

/**
 * {@link DartModuleRootListener} helps to keep "Dart Packages" library (based on Dart-specific pubspec.yaml and .packages files) up-to-date.
 *
 * @see DartStartupActivity
 * @see DartFileListener
 */
public class DartModuleRootListener implements ModuleRootListener {
  @Override
  public void rootsChanged(@NotNull ModuleRootEvent event) {
    if (DartSdk.getDartSdk(event.getProject()) == null) return;

    DartFileListener.scheduleDartPackageRootsUpdate(event.getProject());

    ApplicationManager.getApplication()
      .invokeLater(() -> DartAnalysisServerService.getInstance(event.getProject()).ensureAnalysisRootsUpToDate(),
                   ModalityState.NON_MODAL,
                   DartAnalysisServerService.getInstance(event.getProject()).getDisposedCondition());
  }
}
