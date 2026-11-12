// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.folding.impl;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.moduleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

@TestApplication
public class JadeFoldingPolicyTest {
  private static final TestFixture<Path> pathFixture = tempPathFixture();
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(pathFixture, OpenProjectTask.build(), true);
  @SuppressWarnings("unused")
  private static final TestFixture<Module> moduleFixture = moduleFixture(projectFixture, pathFixture, true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightFixture(projectFixture, pathFixture);

  @Test
  public void testNameWithSpecialCharacter() {
    FoldingPolicyTestUtil.doTest(codeInsightFixture.get(), """
                                   html
                                       #container
                                           p.
                                             text""",
                                 "jade");
  }
}
