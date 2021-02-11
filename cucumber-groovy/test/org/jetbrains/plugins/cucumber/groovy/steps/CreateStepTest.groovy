// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy.steps

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import groovy.transform.CompileStatic
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection
import org.jetbrains.plugins.cucumber.psi.GherkinStep
import org.junit.Test

import static org.junit.Assert.assertEquals

@CompileStatic
class CreateStepTest extends GrCucumberLightTestCase {

  @Test
  void 'simple step creation'() {
    doTest '''\
Feature: Git Cherry-Pick When Auto-Commit is selected
  Scenario: Simple cherry-pick
    Given a comm<caret>it
''', '''\
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~/^a commit$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}\
'''
  }

  @Test
  void escapeChars() {
    doTest('''\
Feature: Git Cherry-Pick When Auto-Commit is selected
  Scenario: Simple cherry-pick
    Given a comm<caret>it 'fix'
''', '''\
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~/^a commit 'fix'$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}\
''')
  }

  @Test
  void escapeSlashD() {
    doTest('''\
Feature: Git Cherry-Pick When Auto-Commit is selected
  Scenario: Simple cherry-pick
    Given a comm<caret>it 'fix' 5
''', '''\
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~/^a commit 'fix' (\\d+)$/) { int arg1 ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}\
''')
  }

  private void doTest(String feature, String expectedStepDef) {
    fixture.enableInspections(CucumberStepInspection)

    final file = fixture.configureByText('feature.feature', feature)
    final stepDef = fixture.addFileToProject('myStepDefs.groovy', '''\
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)
''')

    WriteCommandAction.runWriteCommandAction project, {
      PsiDocumentManager.getInstance(project).commitAllDocuments()
      final at = file.findElementAt(fixture.editor.caretModel.offset)
      final step = PsiTreeUtil.getParentOfType(at, GherkinStep)
      new GrStepDefinitionCreator().createStepDefinition(step, stepDef, false)
      PsiDocumentManager.getInstance(project).commitAllDocuments()
    }

    assertEquals(expectedStepDef, stepDef.text)
  }
}
