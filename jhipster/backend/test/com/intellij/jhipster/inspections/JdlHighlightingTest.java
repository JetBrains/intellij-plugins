// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.inspections;

import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

public class JdlHighlightingTest extends BasePlatformTestCase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + "/contrib/jhipster/backend/testData/highlighting";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myFixture.enableInspections(List.of(
      JdlIncorrectOptionTypeInspection.class,
      JdlUnknownOptionInspection.class,
      JdlDuplicatedDeclarationInspection.class
    ));
  }

  public void testNorthwind() {
    doTest("Northwind.jdl");
  }

  public void testMicroservices() {
    doTest("Microservices.jdl");
  }

  public void testSpace() {
    doTest("Space.jdl");
  }

  private void doTest(String file) {
    myFixture.configureByFile(file);
    myFixture.checkHighlighting(true, false, true);
  }
}