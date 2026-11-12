// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class JadeSpellcheckerTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.setTestDataPath(JadeTestUtil.getBaseTestDataPath() + "/spellchecker/");
    myFixture.enableInspections(new GrazieSpellCheckingInspection());
  }

  @Test
  public void testAll() {
    fixture.get().testHighlighting(false, false, true, myTestName + ".jade");
  }
}
