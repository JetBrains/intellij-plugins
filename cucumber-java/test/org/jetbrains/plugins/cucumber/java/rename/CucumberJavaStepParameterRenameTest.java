package org.jetbrains.plugins.cucumber.java.rename;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.idea.TestFor;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt;
import com.intellij.project.IntelliJProjectConfiguration;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPackage;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.RunInEdt;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.FixturesKt;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.refactoring.rename.GherkinStepParameterRenameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.jetbrains.plugins.cucumber.java.CucumberJavaVersionUtil.CUCUMBER_CORE_VERSION_5;

/// Tests that [GherkinStepParameterRenameTest][GherkinStepParameterRenameTest]
/// works fine in a language-specific setting (Java).
///
/// The "language-specific-setting" part matters! To learn why, see IDEA-376182.
@RunInEdt(writeIntent = true)
@TestDataPath("$PROJECT_ROOT/contrib/cucumber-java/testData/renameStepParameter")
@TestApplication
public class CucumberJavaStepParameterRenameTest {

  private static final TestFixture<Path> tempDir = FixturesKt.tempPathFixture();
  private static final TestFixture<Project> project = FixturesKt.projectFixture(tempDir, OpenProjectTask.build(), true);
  @SuppressWarnings("unused")
  private static final TestFixture<Module> module = FixturesKt.moduleFixture(project, tempDir);

  private final TestFixture<String> testNameFixture = FixturesKt.testNameFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = CodeInsightFixtureKt.codeInsightFixture(project, tempDir);

  @BeforeAll
  public static void setUpProjectDescriptor() {
    String version = "7";
    ModifiableRootModel model = ModuleRootManager.getInstance(module.get()).getModifiableModel();
    IntelliJProjectConfiguration.LibraryRoots libraryRoots;
    libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-core-" + version);
    PsiTestUtil.addProjectLibrary(model, "cucumber-core", libraryRoots.getClassesPaths());

    libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-java-" + version);
    PsiTestUtil.addProjectLibrary(model, "cucumber-java", libraryRoots.getClassesPaths());

    if (VersionComparatorUtil.compare(version, CUCUMBER_CORE_VERSION_5) >= 0) {
      libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-java8-" + version);
      PsiTestUtil.addProjectLibrary(model, "cucumber-java8", libraryRoots.getClassesPaths());
    }

    CucumberJavaTestUtil.attachCucumberExpressionsLibrary(model);

    WriteAction.runAndWait(() -> {
      model.commit();
    });
  }

  private void doTest(String newName) {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.copyDirectoryToProject(testNameFixture.get() + "/before", "");
    myFixture.configureByFile("test.feature");
    myFixture.testHighlighting("test.feature"); // ensure everything is resolved

    PsiPackage cucumberPackage = ReadAction.compute(() -> {
      return JavaPsiFacade.getInstance(myFixture.getProject()).findPackage("io.cucumber.java.en");
    });
    Assertions.assertNotNull(cucumberPackage); // verify that cucumber-jvm JAR is attached
    myFixture.renameElementAtCaretUsingHandler(newName);

    myFixture.checkResultByFile("test.feature", testNameFixture.get() + "/after/test.feature", false);
  }

  @Test
  public void parameterDefinition() {
    doTest("newStart");
  }

  @Test
  @TestFor(issues = "IDEA-376182")
  public void parameterUsage() {
    doTest("newStart");
  }

  @Test
  @TestFor(issues = "IDEA-376182")
  public void parameterUsageOnUnresolvedStep() {
    doTest("newStart");
  }

  @Test
  @TestFor(issues = "IDEA-374108")
  public void parameterUsageWithCaretJustBeforeClosingBracket() {
    doTest("helloDarkness");
  }
}