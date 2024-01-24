// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.steps;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.junit.Assert;
import org.junit.Test;

public class CreateStepTest extends GrCucumberLightTestCase {
  @Test
  public void simple_step_creation() {
    doTest("""
Feature: Git Cherry-Pick When Auto-Commit is selected
  Scenario: Simple cherry-pick
    Given a comm<caret>it
""", """
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~/^a commit$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}""");
  }

  @Test
  public void escapeChars() {
    doTest("""
Feature: Git Cherry-Pick When Auto-Commit is selected
  Scenario: Simple cherry-pick
    Given a comm<caret>it 'fix'
""", """
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~/^a commit 'fix'$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}""");
  }

  @Test
  public void escapeSlashD() {
    doTest("""
Feature: Git Cherry-Pick When Auto-Commit is selected
  Scenario: Simple cherry-pick
    Given a comm<caret>it 'fix' 5
""", """
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~/^a commit 'fix' (\\d+)$/) { int arg1 ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}""");
  }

  private void doTest(String feature, String expectedStepDef) {
    getFixture().enableInspections(CucumberStepInspection.class);

    PsiFile file = getFixture().configureByText("feature.feature", feature);
    PsiFile stepDef = getFixture().addFileToProject("myStepDefs.groovy", """
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)
""");

    WriteCommandAction.runWriteCommandAction(getProject(), () -> {
        PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
        PsiElement at = file.findElementAt(getFixture().getEditor().getCaretModel().getOffset());
        GherkinStep step = PsiTreeUtil.getParentOfType(at, GherkinStep.class);
        new GrStepDefinitionCreator().createStepDefinition(step, stepDef, false);
        PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
      }
    );

    Assert.assertEquals(expectedStepDef, stepDef.getText());
  }
}
