// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.rename;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.usages.Usage;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.java.resolve.BaseCucumberJavaResolveTest;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import java.util.Collection;

/// See IDEA-107390.
public class CucumberStepRenameTest extends BaseCucumberJavaResolveTest {

  PsiFile featureFile;
  PsiFile stepDefFile;

  private void prepare(String elementName, String stepDefinitionName) {
    myFixture.copyDirectoryToProject("before", "");

    VirtualFile featureVirtualFile = myFixture.findFileInTempDir("withdraw.feature");
    VirtualFile stepDefVirtualFile = myFixture.findFileInTempDir("WithdrawSteps.java");
    featureFile = PsiManager.getInstance(myFixture.getProject()).findFile(featureVirtualFile);
    stepDefFile = PsiManager.getInstance(myFixture.getProject()).findFile(stepDefVirtualFile);
    myFixture.configureFromExistingVirtualFile(featureVirtualFile);

    int offset = findOffsetBySignature(elementName);
    myFixture.getEditor().getCaretModel().moveToOffset(offset);

    checkReference(elementName, stepDefinitionName); // Step definition being resolved is a prerequisite for renaming

    CucumberStepReference stepReference = (CucumberStepReference)myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
    assertNotNull(stepReference);

    Collection<AbstractStepDefinition> stepDefinitions = stepReference.resolveToDefinitions();
    assertEquals(1, stepDefinitions.size());
  }

  /// Test renaming of `I am happy` -> `Me be satisfied`
  public void testSimpleDefinitionIsRenamed_cukex() {
    prepare("I am <caret>happy", "i_am_happy");

    Collection<Usage> usages = myFixture.testFindUsagesUsingAction();
    assertEquals(3, usages.size());

    myFixture.renameElementAtCaretUsingHandler("Me be satisfied");

    assertTrue("At least 1 step usage should have been renamed", featureFile.getText().contains("And Me be satisfied"));
    assertTrue("Step definition file should have been renamed", stepDefFile.getText().contains("@Then(\"Me be satisfied\")"));
    assertFalse("All step usages should have been renamed", featureFile.getText().contains("I am happy"));
  }

  /// Test renaming of `I am angry` -> `I am not happy at all`
  public void testSimpleDefinitionIsRenamed_regex() {
    prepare("I am <caret>angry", "i_am_angry");

    Collection<Usage> usages = myFixture.testFindUsagesUsingAction();
    assertEquals(2, usages.size());

    myFixture.renameElementAtCaretUsingHandler("I am not happy at all");

    assertTrue("At least 1 step usage should have been renamed", featureFile.getText().contains("And I am not happy at all"));
    assertTrue("Step definition file should have been renamed", stepDefFile.getText().contains("@Then(\"^I am not happy at all$\")"));
    assertFalse("All step usages should have been renamed", featureFile.getText().contains("I am angry"));
  }

  /// Test renaming of `I have {int} EUR on my account` -> `I possess the amount of {int} USD on my acc`
  public void testDefinitionWithArgIsRenamed_cukex() {
    prepare("I have<caret> 42 EUR on my account", "i_have_EUR_on_my_account");

    Collection<Usage> usages = myFixture.testFindUsagesUsingAction();
    assertEquals(4, usages.size());

    myFixture.renameElementAtCaretUsingHandler("I possess the amount of {int} USD on my acc");

    assertTrue("At least 1 step usage should have been renamed", featureFile.getText().contains("I possess the amount of 42 USD on my acc"));
    assertTrue("Step in step definition file should have been renamed", stepDefFile.getText().contains("@Given(\"I possess the amount of {int} USD on my acc\")"));
    assertFalse("Step in step definition file should have been renamed", stepDefFile.getText().contains("EUR on my account"));
    assertFalse("Step usages should have been renamed", featureFile.getText().contains("EUR on my account"));

    checkReference("I possess<caret> the amount of 42 USD on my acc", "i_have_EUR_on_my_account");
  }

  /// Test renaming of `I have {int} feeling(s)` -> `I really do have {int} feeling(s)`
  public void testDefinitionWithArgAndOptionalIsRenamed_cukex() {
    prepare("I have<caret> 7 feelings", "i_have_feelings");

    Collection<Usage> usages = myFixture.testFindUsagesUsingAction();
    assertEquals(2, usages.size());

    myFixture.renameElementAtCaretUsingHandler("I really do have {int} feeling(s)");
    assertTrue("The usage with optional text is renamed", featureFile.getText().contains("I really do have 1 feeling\n"));
    assertTrue("The usage without optional text is renamed", featureFile.getText().contains("I really do have 7 feelings\n"));
    assertTrue("Step in step definition file should have been renamed", stepDefFile.getText().contains("@Then(\"I really do have {int} feeling(s)\")"));
    assertFalse("Step in step definition file should have been renamed", stepDefFile.getText().contains("I have {int} feelings"));

    checkReference("I really <caret>do have 7 feelings", "i_have_feelings");
  }
  
  /// Test renaming of `I have no/few/many feeling(s) about this/that` -> `My person has no/few/many feeling(s) about this/that`
  public void testDefinitionWithArgAndAlternativeIsRenamed_cukex() {
    prepare("I have<caret> few feelings about that", "i_have_feelings2");

    Collection<Usage> usages = myFixture.testFindUsagesUsingAction();
    assertEquals(2, usages.size());

    myFixture.renameElementAtCaretUsingHandler("My person has no/few/many feeling(s) about this/that");
    assertTrue("The usage with alternative text is renamed", featureFile.getText().contains("My person has few feelings about that\n"));
    assertTrue("The usage without optional text is renamed", featureFile.getText().contains("My person has no feeling about this\n"));
    assertTrue("Step in step definition file should have been renamed", stepDefFile.getText().contains("@Then(\"My person has no/few/many feeling(s) about this/that\")"));
    assertFalse("Step in step definition file should have been renamed", stepDefFile.getText().contains("I have no/few/many feeling(s) about this/that"));

    checkReference("My person has few feelings<caret> about that", "i_have_feelings2");
  }

  /// Test renaming of `^I withdraw (-?\\\d+) EUR` -> `I spend (-?\\\d+) USD`
  public void testDefinitionWithArgIsRenamed_regex() {
    prepare("I withdraw<caret> 42 EUR", "withdraw_EUR");

    Collection<Usage> usages = myFixture.testFindUsagesUsingAction();
    assertEquals(4, usages.size());

    myFixture.renameElementAtCaretUsingHandler("I spend (-?\\d+) USD");

    assertTrue("At least 1 step usage should have been renamed", featureFile.getText().contains("I spend 42 USD"));
    assertTrue("Step in step definition file should have been renamed", stepDefFile.getText().contains("@When(\"^I spend (-?\\\\d+) USD$\")"));
    assertFalse("Step in step definition file should have been renamed", stepDefFile.getText().contains("I withdraw"));
    assertFalse("Step usages should have been renamed", featureFile.getText().contains("I withdraw"));

    checkReference("I spend<caret> 42 USD", "withdraw_EUR");
  }

  @Override
  protected String getRelatedTestDataPath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "renameStep";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber7ProjectDescriptor();
  }
}
