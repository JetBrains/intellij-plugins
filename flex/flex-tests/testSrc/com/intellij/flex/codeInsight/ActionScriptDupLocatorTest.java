// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.codeInsight;

import com.intellij.dupLocator.DuplicatesTestFixture;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.dupLocator.DuplicatesTestFixture.duplicatesTestFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class ActionScriptDupLocatorTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<DuplicatesTestFixture> duplicates =
    duplicatesTestFixture(projectFixture, FlexTestUtils.getTestDataPath("duplicates/"), FlexSupportLoader.ECMA_SCRIPT_L4);

  @Test
  public void testAs1() throws Exception {
    duplicates.get().doTest("asdups1.as", false, false, true, 1, 1, "", 2);
  }

  @Test
  public void testAs2() throws Exception {
    duplicates.get().doTest("asdups2.as", false, false, true, 3, 0, "", 4);
  }
}
