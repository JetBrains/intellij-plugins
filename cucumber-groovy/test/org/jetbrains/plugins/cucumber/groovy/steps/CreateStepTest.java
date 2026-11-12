// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.steps;

import com.intellij.groovy.testFramework.BaseTest;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberTestUtil;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.EdtTestUtil.runInEdtAndWait;
import static org.jetbrains.plugins.groovy.util.GroovyFixture.groovyFixture;
import static org.jetbrains.plugins.groovy.util.GroovyFixture.groovyProjectFixture;

@TestApplication
public class CreateStepTest implements BaseTest {
  private static final TestFixture<Project> project = groovyProjectFixture();
  private final TestFixture<JavaCodeInsightTestFixture> groovy = groovyFixture(project, GrCucumberTestUtil.DESCRIPTOR);

  @Override
  public @NotNull JavaCodeInsightTestFixture getFixture() {
    return groovy.get();
  }

  @Test
  public void simple_step_creation() {
    runInEdtAndWait(() -> {
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
    });
  }

  @Test
  public void escapeChars() {
    runInEdtAndWait(() -> {
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
    });
  }

  @Test
  public void escapeSlashD() {
    runInEdtAndWait(() -> {
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
    });
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

    Assertions.assertEquals(expectedStepDef, stepDef.getText());
  }
}
