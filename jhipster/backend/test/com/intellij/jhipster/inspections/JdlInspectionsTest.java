// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.inspections;

import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

public class JdlInspectionsTest extends BasePlatformTestCase {
  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + "/contrib/jhipster/backend/testData/inspections";
  }

  public void testUnusedEntities() {
    myFixture.enableInspections(List.of(JdlUnusedDeclarationInspection.class));
    myFixture.configureByFile("UnusedEntities.jdl");
    myFixture.checkHighlighting();
  }

  public void testUnusedEnums() {
    myFixture.enableInspections(List.of(JdlUnusedDeclarationInspection.class));
    myFixture.configureByFile("UnusedEnums.jdl");
    myFixture.checkHighlighting();
  }

  public void testDuplicatedEntity() {
    myFixture.enableInspections(List.of(JdlDuplicatedDeclarationInspection.class));
    myFixture.configureByFile("DuplicatedEntity.jdl");
    myFixture.checkHighlighting();
  }

  public void testDuplicatedEnum() {
    myFixture.enableInspections(List.of(JdlDuplicatedDeclarationInspection.class));
    myFixture.configureByFile("DuplicatedEnum.jdl");
    myFixture.checkHighlighting();
  }

  public void testUnknownOption() {
    myFixture.enableInspections(List.of(JdlUnknownOptionInspection.class));
    myFixture.configureByFile("UnknownOptions.jdl");
    myFixture.checkHighlighting();
  }

  public void testIncorrectOptionType() {
    myFixture.enableInspections(List.of(JdlIncorrectOptionTypeInspection.class));
    myFixture.configureByFile("IncorrectOptionTypes.jdl");
    myFixture.checkHighlighting();
  }
}