// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.rename;

import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.resolve.BaseCucumberJavaResolveTest;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import java.util.ArrayList;
import java.util.List;

/// See IDEA-107390.
@SuppressWarnings("NonAsciiCharacters")
public class CucumberJavaStepRenameTest extends BaseCucumberJavaResolveTest {

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber7ProjectDescriptor();
  }

  @Override
  protected String getRelatedTestDataPath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "renameStep";
  }

  @Override
  public void doTest(@NotNull String stepDefinitionName, @NotNull String oldName, String newName) {
    prepare(oldName, stepDefinitionName);

    myFixture.renameElementAtCaretUsingHandler(newName);

    myFixture.checkResultByFile("test.feature", getTestName(true) + "/after/test.feature", false);
    myFixture.checkResultByFile("Steps.java", getTestName(true) + "/after/Steps.java", false);
    myFixture.testHighlightingAllFiles(true, false, true, "test.feature", "Steps.java");
  }

  /// Prepares the fixtures and verifies that the element under the caret points to a method named `stepDefinitionName`.
  private void prepare(String oldName, String stepDefinitionName) {
    myFixture.enableInspections(new CucumberStepInspection());
    myFixture.copyDirectoryToProject(getTestName(true) + "/before", "");

    myFixture.configureByFiles("test.feature", "Steps.java");

    PsiReference reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
    ResolveResult[] resolveResults = getResolveResult(reference);

    checkReference(resolveResults, stepDefinitionName); // Step definition being resolved is a prerequisite for renaming

    CucumberStepReference stepReference = (CucumberStepReference)myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
    assertNotNull(stepReference);

    List<AbstractStepDefinition> stepDefinitions = new ArrayList<>(stepReference.resolveToDefinitions());
    assertEquals(1, stepDefinitions.size());
    assertEquals(oldName, stepDefinitions.getFirst().getExpression());
  }

  public void testCukexSimple() {
    doTest("i_am_happy", "I am happy", "Me be satisfied");
  }

  public void testCukexSimpleInPolishWithJava8() {
    doTest("Wtedy", "jem ciastko", "pożeram ogóreczka");
  }

  public void testCukexSimpleInPolish() {
    doTest("jestemGłodny", "jestem głodny", "teraz jestem bardzo głodny");
  }

  public void testCukexWithArgument() {
    doTest("i_have_EUR_on_my_account", "I have {int} EUR on my account", "I possess the amount of {int} USD on my acc");
  }

  public void testCukexWithArgumentAndAlternativeText() {
    doTest("i_have_different_feelings",
           "I have no/few/many feeling(s) about this/that",
           "My person has no/few/many feeling(s) about this/that"
    );
  }

  public void testCukexWithArgumentAndOptionalText() {
    doTest("i_have_feelings", "I have {int} feeling(s)", "I really do have {int} feeling(s)");
  }

  public void testRegexSimple() {
    doTest("i_am_angry", "^I am angry$", "I am not happy at all");
  }

  public void testRegexWithArgument() {
    doTest("withdraw_EUR", "^I withdraw (-?\\d+) EUR$", "I spend (-?\\d+) USD");
  }
}
