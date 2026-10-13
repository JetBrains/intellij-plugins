// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.inspections;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.moduleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/jhipster/backend/testData/highlighting")
public class JdlHighlightingTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<Path> pathFixture = tempPathFixture();
  @SuppressWarnings("unused")
  private final TestFixture<Module> moduleFixture = moduleFixture(projectFixture, pathFixture, true);
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightFixture(projectFixture, pathFixture);

  @BeforeEach
  void setUp() {
    codeInsightFixture.get().enableInspections(List.of(
      JdlIncorrectOptionTypeInspection.class,
      JdlUnknownOptionInspection.class,
      JdlDuplicatedDeclarationInspection.class
    ));
  }

  @Test
  public void testNorthwind() {
    doTest("Northwind.jdl");
  }

  @Test
  public void testMicroservices() {
    doTest("Microservices.jdl");
  }

  @Test
  public void testSpace() {
    doTest("Space.jdl");
  }

  private void doTest(String file) {
    EdtTestUtil.runInEdtAndWait(() -> {
      CodeInsightTestFixture myFixture = codeInsightFixture.get();
      myFixture.configureByFile(file);
      myFixture.checkHighlighting(true, false, true);
    });
  }
}