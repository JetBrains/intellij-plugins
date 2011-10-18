package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.openapi.application.ex.ApplicationManagerEx;

import java.util.UUID;

public class FlexLibraryIdGenerator {

  private static int ourTestId = 0;

  public static void resetTestState() {
    ourTestId = 0;
  }

  public static String generateId() {
    if (ApplicationManagerEx.getApplicationEx().isUnitTestMode()) {
      return String.valueOf(ourTestId++);
    }
    else {
      return UUID.randomUUID().toString();
    }
  }
}
