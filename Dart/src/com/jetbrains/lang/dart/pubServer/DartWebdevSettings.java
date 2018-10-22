// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.pubServer;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "DartWebdevSettings")
public class DartWebdevSettings implements PersistentStateComponent<DartWebdevSettings> {

  public int WEBDEV_PORT = 53322;

  public static DartWebdevSettings getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, DartWebdevSettings.class);
  }

  @Nullable
  @Override
  public DartWebdevSettings getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull DartWebdevSettings state) {
    WEBDEV_PORT = state.WEBDEV_PORT;
  }
}
