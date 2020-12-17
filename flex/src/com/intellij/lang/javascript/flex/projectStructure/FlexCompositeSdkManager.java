// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlexCompositeSdkManager implements Disposable {
  private final Map<List<String>, FlexCompositeSdk> mySdks = new ConcurrentHashMap<>();

  public static FlexCompositeSdkManager getInstance() {
    return ApplicationManager.getApplication().getService(FlexCompositeSdkManager.class);
  }

  public @NotNull FlexCompositeSdk getOrCreateSdk(@NotNull String[] names) {
    return mySdks.computeIfAbsent(Arrays.asList(names), (n) -> new FlexCompositeSdk(names, this));
  }

  @Override
  public void dispose() {
  }
}
