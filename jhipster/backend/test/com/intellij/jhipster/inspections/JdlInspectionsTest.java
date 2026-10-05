// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.moduleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/jhipster/backend/testData/inspections")
public class JdlInspectionsTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<Path> pathFixture = tempPathFixture();
  @SuppressWarnings("unused")
  private final TestFixture<Module> moduleFixture = moduleFixture(projectFixture, pathFixture, true);
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightFixture(projectFixture, pathFixture);

  @Test
  public void testUnusedEntities() {
    doTest(JdlUnusedDeclarationInspection.class, "UnusedEntities.jdl");
  }

  @Test
  public void testUnusedEnums() {
    doTest(JdlUnusedDeclarationInspection.class, "UnusedEnums.jdl");
  }

  @Test
  public void testDuplicatedEntity() {
    doTest(JdlDuplicatedDeclarationInspection.class, "DuplicatedEntity.jdl");
  }

  @Test
  public void testDuplicatedEnum() {
    doTest(JdlDuplicatedDeclarationInspection.class, "DuplicatedEnum.jdl");
  }

  @Test
  public void testUnknownOption() {
    doTest(JdlUnknownOptionInspection.class, "UnknownOptions.jdl");
  }

  @Test
  public void testIncorrectOptionType() {
    doTest(JdlIncorrectOptionTypeInspection.class, "IncorrectOptionTypes.jdl");
  }

  private void doTest(Class<? extends LocalInspectionTool> inspection, String file) {
    EdtTestUtil.runInEdtAndWait(() -> {
      CodeInsightTestFixture myFixture = codeInsightFixture.get();
      myFixture.enableInspections(List.of(inspection));
      myFixture.configureByFile(file);
      myFixture.checkHighlighting();
    });
  }
}