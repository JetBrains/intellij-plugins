package org.jetbrains.plugins.cucumber.groovy.steps

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection
import org.jetbrains.plugins.cucumber.psi.GherkinStep

/**
 * @author Max Medvedev
 */
class CreateStepTest extends GrCucumberLightTestCase {

  void 'test simple step creation'() {
    doTest '''\
Feature: Git Cherry-Pick When Auto-Commit is selected
  Scenario: Simple cherry-pick
    Given a comm<caret>it
''', '''\
import cucumber.runtime.PendingException

this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~/^a commit$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}\
'''
  }

  void testEscapeChars() {
    doTest('''\
Feature: Git Cherry-Pick When Auto-Commit is selected
  Scenario: Simple cherry-pick
    Given a comm<caret>it 'fix'
''', '''\
import cucumber.runtime.PendingException

this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~/^a commit 'fix'$/) { ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}\
''')
  }

  void testEscapeSlashD() {
    doTest('''\
Feature: Git Cherry-Pick When Auto-Commit is selected
  Scenario: Simple cherry-pick
    Given a comm<caret>it 'fix' 5
''', '''\
import cucumber.runtime.PendingException

this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~/^a commit 'fix' (\\d+)$/) { int arg1 ->
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException()
}\
''')
  }

  protected void doTest(String feature, String expectedStepDef) {
    myFixture.enableInspections(CucumberStepInspection)

    final file = myFixture.configureByText('feature.feature', feature)
    final stepDef = myFixture.addFileToProject('myStepDefs.groovy', '''\
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)
''')

    WriteCommandAction.runWriteCommandAction project, {
      PsiDocumentManager.getInstance(myFixture.project).commitAllDocuments()
      final at = file.findElementAt(myFixture.editor.caretModel.offset)
      final step = PsiTreeUtil.getParentOfType(at, GherkinStep)
      new GrStepDefinitionCreator().createStepDefinition(step, stepDef)
      PsiDocumentManager.getInstance(myFixture.project).commitAllDocuments()
    }

    assertEquals(expectedStepDef, stepDef.text)
  }
}
