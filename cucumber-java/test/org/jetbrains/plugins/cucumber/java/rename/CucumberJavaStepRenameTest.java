// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.rename;

import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveFixture;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;
import static org.jetbrains.plugins.cucumber.java.resolve.CucumberJavaResolveFixtures.cucumberJavaResolveFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class CucumberJavaStepRenameTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture(CucumberJavaTestUtil.createCucumber7ProjectDescriptor());
  private final TestFixture<CucumberResolveFixture> cucumber =
    cucumberJavaResolveFixture(projectFixture, CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "renameStep");

  private void doTest(@NotNull String stepDefinitionName, @NotNull String oldName, String newName) {
    CucumberResolveFixture resolve = cucumber.get();
    CodeInsightTestFixture myFixture = resolve.getFixture();
    prepare(oldName, stepDefinitionName);

    String testName = resolve.getTestName();
    EdtTestUtil.runInEdtAndWait(() -> {
      myFixture.renameElementAtCaretUsingHandler(newName);

      myFixture.checkResultByFile("test.feature", testName + "/after/test.feature", false);
      myFixture.checkResultByFile("Steps.java", testName + "/after/Steps.java", false);
      myFixture.testHighlightingAllFiles(true, false, true, "test.feature", "Steps.java");
    });
  }

  /// Prepares the fixtures and verifies that the element under the caret points to a method named `stepDefinitionName`.
  private void prepare(String oldName, String stepDefinitionName) {
    CucumberResolveFixture resolve = cucumber.get();
    CodeInsightTestFixture myFixture = resolve.getFixture();
    myFixture.enableInspections(new CucumberStepInspection());
    myFixture.copyDirectoryToProject(resolve.getTestName() + "/before", "");

    myFixture.configureByFile("test.feature");

    PsiFile file = myFixture.getFile();
    int caretOffset = ReadAction.computeBlocking(() -> myFixture.getCaretOffset());
    PsiReference reference = ReadAction.computeBlocking(() -> file.findReferenceAt(caretOffset));
    ResolveResult[] resolveResults = resolve.getResolveResult(reference);

    resolve.checkReference(resolveResults, stepDefinitionName); // Step definition being resolved is a prerequisite for renaming

    List<AbstractStepDefinition> stepDefinitions = ReadAction.computeBlocking(() -> {
      CucumberStepReference stepReference = (CucumberStepReference)file.findReferenceAt(caretOffset);
      assertNotNull(stepReference);
      return new ArrayList<>(stepReference.resolveToDefinitions());
    });
    assertEquals(1, stepDefinitions.size());
    assertEquals(oldName, ReadAction.computeBlocking(() -> stepDefinitions.getFirst().getExpression()));
  }

  @Test
  public void testCukexSimple() {
    doTest("i_am_happy", "I am happy", "Me be satisfied");
  }

  @Test
  public void testCukexSimpleWithManyAnnotations() {
    doTest("foo", "I am close", "I am finding a bug");
  }

  @Test
  @SuppressWarnings("NonAsciiCharacters")
  public void testCukexSimpleInPolishWithJava8() {
    doTest("Wtedy", "jem ciastko", "pożeram ogóreczka");
  }

  @Test
  @SuppressWarnings("NonAsciiCharacters")
  public void testCukexSimpleInPolish() {
    doTest("jestemGłodny", "jestem głodny", "teraz jestem bardzo głodny");
  }

  @Test
  public void testCukexWithArgument() {
    doTest("i_have_EUR_on_my_account", "I have {int} EUR on my account", "I possess the amount of {int} USD on my acc");
  }

  @Test
  public void testCukexWithArgument_CustomParameterType() {
    doTest("customParameterTypeIsUsed", "custom parameter type {customType} is used", "a type {customType} is mentioned");
  }

  @Test
  public void testCukexWithArgumentAndAlternativeText() {
    doTest("i_have_different_feelings",
           "I have no/few/many feeling(s) about this/that",
           "My person has no/few/many feeling(s) about this/that"
    );
  }

  @Test
  public void testCukexWithArgumentAndOptionalText() {
    doTest("i_have_feelings", "I have {int} feeling(s)", "I really do have {int} feeling(s)");
  }

  @Test
  public void testNoDefinition() {
    CodeInsightTestFixture myFixture = cucumber.get().getFixture();
    myFixture.copyDirectoryToProject(cucumber.get().getTestName() + "/before", "");
    myFixture.configureByFile("test.feature");
    CommonRefactoringUtil.RefactoringErrorHintException exception =
      assertThrows(CommonRefactoringUtil.RefactoringErrorHintException.class,
                   () -> EdtTestUtil.runInEdtAndWait(() -> myFixture.renameElementAtCaretUsingHandler("newName")));
    String message = Objects.requireNonNullElse(exception.getMessage(), "");
    assertTrue(message.contains(
                 "Cannot perform refactoring. This step can't be renamed due to no definition, several definitions or several steps pointing to one definition. Please rename it manually."),
               message);
  }

  @Test
  public void testRegexSimple() {
    doTest("i_am_angry", "^I am angry$", "I am not happy at all");
  }

  @Test
  public void testRegexSimpleWithManyAnnotations() {
    doTest("foo", "^I am close$", "I am finding a bug");
  }

  @Test
  public void testRegexWithArgument() {
    doTest("withdraw_EUR", "^I withdraw (-?\\d+) EUR$", "I spend (-?\\d+) USD");
  }
}
