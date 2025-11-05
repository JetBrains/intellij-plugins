package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.FixturesKt;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/cucumber/testData/refactoring/rename")
public class GherkinStepParameterRenameTest {

  private final TestFixture<String> testNameFixture = FixturesKt.testNameFixture();
  private final TestFixture<Path> tempDir = FixturesKt.tempPathFixture(null, "IJ");
  private final TestFixture<Project> project = FixturesKt.projectFixture(tempDir, OpenProjectTask.build(), true);
  @SuppressWarnings("unused")
  private final TestFixture<Module> module = FixturesKt.moduleFixture(project, tempDir, false);
  private final TestFixture<CodeInsightTestFixture> fixture = CodeInsightFixtureKt.codeInsightFixture(project, tempDir);

  private void doTest(String newName) {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile(testNameFixture.get() + ".feature");

    myFixture.renameElementAtCaretUsingHandler(newName); // FIXME: for some reason doesn't work with JUnit 5

    myFixture.checkResultByFile(testNameFixture.get() + ".after.feature");
  }

  @Test
  public void renameParameterUsage() {
    doTest("newStart");
  }

  @Test
  public void renameParameterDefinition() {
    doTest("newStart");
  }
}
