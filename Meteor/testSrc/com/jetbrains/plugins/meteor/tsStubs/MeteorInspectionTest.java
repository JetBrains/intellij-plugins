package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.plugins.meteor.spacebars.inspection.MeteorUnresolvedSymbolInspection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;


@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/Meteor/testData/inspections")
public class MeteorInspectionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = sharedCodeInsightFixture(projectFixture);

  @BeforeEach
  void setUp() {
    MeteorTestUtil.enableMeteor();
    MeteorTestUtil.initMeteorDirs(codeInsight.get().getProject());
  }

  @AfterEach
  void tearDown() {
    MeteorTestUtil.disableMeteor();
  }

  @Test
  public void testUnresolvedTemplate() {
    doTest("UnresolvedTemplate");
  }

  private void doTest(String testName) {
    ApplicationManager.getApplication().invokeAndWait(() -> codeInsight.get().testInspection(testName, new LocalInspectionToolWrapper(new MeteorUnresolvedSymbolInspection())));
  }
}
