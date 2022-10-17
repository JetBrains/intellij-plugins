/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.frameworks.jboss.drools;

import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.UsefulTestCase;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * @author Sergey Vasiliev
 */
public abstract class BasicDroolsTestCase extends UsefulTestCase {

  public static final String TEST_DATA_PATH = "/contrib/drools/tests/testData/";

  private static final String TEST_DATA_ROOT_PATH = PathManager.getHomePath().replace(File.separatorChar, '/') + TEST_DATA_PATH;

  protected static String getTestDataRootPath() {
    return TEST_DATA_ROOT_PATH;
  }

  @NonNls
  protected String getBasePath() {
    return TEST_DATA_PATH;
  }

  @NonNls
  protected final String getTestDataPath() {
    return PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath();
  }
}