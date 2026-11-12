// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestApplication
public class JadeRenameTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    fixture.get().setTestDataPath(JadeTestUtil.getBaseTestDataPath() + "/rename");
  }

  public void _testVar1() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile(myTestName + ".jade");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename(myTestName + "_after.jade", "zzi"));
  }

  @Test
  public void testExtends1() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFiles(myTestName + ".jade", myTestName + "_2.jade");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename(myTestName + "_after.jade", "foobar.jade"));
    PsiDirectory dir = ReadAction.computeBlocking(() -> myFixture.getFile().getParent());
    assertNull(ReadAction.computeBlocking(() -> dir.findFile(myTestName + "_2.jade")));
    assertNotNull(ReadAction.computeBlocking(() -> dir.findFile("foobar.jade")));
  }

  @Test
  public void testInclude1() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFiles(myTestName + ".jade", myTestName + "_2.js");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename(myTestName + "_after.jade", "foobar.js"));
    PsiDirectory dir = ReadAction.computeBlocking(() -> myFixture.getFile().getParent());
    assertNull(ReadAction.computeBlocking(() -> dir.findFile(myTestName + "_2.jade")));
    assertNotNull(ReadAction.computeBlocking(() -> dir.findFile("foobar.js")));
  }

  @Test
  public void testClassSelector() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile(myTestName + ".jade");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename(myTestName + "_after.jade", "z220"));
  }

  @Test
  public void testIdSelector() {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile(myTestName + ".jade");
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testRename(myTestName + "_after.jade", "z220"));
  }

}
