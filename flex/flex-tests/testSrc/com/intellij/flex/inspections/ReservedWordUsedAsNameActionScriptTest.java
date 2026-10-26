// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.inspections;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.sixrr.inspectjs.InspectionJSFixture;
import com.sixrr.inspectjs.validity.ReservedWordUsedAsNameJSInspection;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static com.sixrr.inspectjs.InspectionJSFixtureKt.inspectionJSFixture;

@TestApplication
public class ReservedWordUsedAsNameActionScriptTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);
  private final TestFixture<InspectionJSFixture> inspection =
    inspectionJSFixture(projectFixture, FlexTestUtils.getTestDataPath("/inspections/ReservedWordUsedAsName/"));

  @Test
  public void test() {
    CodeInsightTestFixture myFixture = inspection.get().getFixture();
    myFixture.enableInspections(ReservedWordUsedAsNameJSInspection.class);
    myFixture.testHighlighting("test.as");
  }
}
