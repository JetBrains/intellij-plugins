// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.frameworks.jboss.drools;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public abstract class DroolsLightTestCase extends LightJavaCodeInsightFixtureTestCase {

  private static final String DROOLS_LIBRARY_PATH =
    FileUtil.toSystemIndependentName(PathManager.getHomePath() + BasicDroolsTestCase.TEST_DATA_PATH + "/lib/");

  protected abstract String getTestDirectory();


  protected String getPluginTestDataRoot() {
    return BasicDroolsTestCase.TEST_DATA_PATH;
  }

  @Override
  protected final String getBasePath() {
    return getPluginTestDataRoot() + getTestDirectory();
  }

  @Override
  protected final void setUp() throws Exception {
    super.setUp();


    myFixture.allowTreeAccessForAllFiles();

    performSetUp();
  }

  /**
   * Perform custom setup.
   *
   */
  protected void performSetUp() {
  }


  @Override
  protected final void tearDown() throws Exception {

    super.tearDown();
  }

  /**
   * Perform custom tear down.
   *
   */
  protected void performTearDown() {
  }


  protected static void addLibrary(ModifiableRootModel model,
                                   String groupId, String version, String... artifactIds) {
    String libraryBase = DROOLS_LIBRARY_PATH + groupId + "/" + version + "/";
    Set<String> jarNames = new HashSet<>(artifactIds.length);
    for (String id : artifactIds) {
      jarNames.add(groupId + "-" + id + "-" + version + ".jar");
    }
    PsiTestUtil.addLibrary(model, groupId + version, libraryBase, ArrayUtilRt.toStringArray(jarNames));
  }

  @Override
  @NonNls
  protected final String getTestDataPath() {
    return PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath();
  }
}
