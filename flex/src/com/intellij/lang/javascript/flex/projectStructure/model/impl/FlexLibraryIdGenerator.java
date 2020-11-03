// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.openapi.application.ApplicationManager;

import java.util.UUID;

public final class FlexLibraryIdGenerator {

  private static int ourTestId = 0;

  public static void resetTestState() {
    ourTestId = 0;
  }

  public static String generateId() {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return String.valueOf(ourTestId++);
    }
    else {
      return UUID.randomUUID().toString();
    }
  }
}
