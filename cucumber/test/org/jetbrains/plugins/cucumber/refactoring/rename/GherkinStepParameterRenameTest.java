package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.RunInEdt;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.*;

@RunInEdt(writeIntent = true)
@TestDataPath("$PROJECT_ROOT/contrib/cucumber/testData/refactoring/rename")
@TestApplication
public class GherkinStepParameterRenameTest {

  private static final TestFixture<Path> pathFixture = tempPathFixture();
  private static final TestFixture<Project> projectFixture = projectFixture(pathFixture, OpenProjectTask.build(), true);
  @SuppressWarnings("unused")
  private static final TestFixture<Module> moduleFixture = moduleFixture(projectFixture, pathFixture, true);

  private final TestFixture<String> testNameFixture = testNameFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightFixture(projectFixture, pathFixture);

  private void doTest(String newName) {
    CodeInsightTestFixture myFixture = codeInsightFixture.get();
    myFixture.configureByFile(testNameFixture.get() + ".feature");

    myFixture.renameElementAtCaretUsingHandler(newName);

    myFixture.checkResultByFile(testNameFixture.get() + ".after.feature");
  }

  @Test
  public void testRenameParameterUsage() {
    doTest("newStart");
  }

  @Test
  public void testRenameParameterDefinition() {
    doTest("newStart");
  }
}
