package org.jetbrains.plugins.cucumber.groovy.steps

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection
import org.jetbrains.plugins.cucumber.psi.GherkinStep

/**
 * @author Max Medvedev
 */
class CreateStepTest extends GrCucumberLightTestCase {
  final String basePath = null

  void testEscapeChars() {
    doTest('''\
Feature: Git Cherry-Pick When Auto-Commit is selected
  Scenario: Simple cherry-pick
    Given a comm<caret>it 'fix'
''', '''\
import cucumber.runtime.PendingException

this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~'^a commit \\'fix\\'$') {->
    // Express the Regexp above with the code you wish you had
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

    ApplicationManager.application.runWriteAction {
      PsiDocumentManager.getInstance(myFixture.project).commitAllDocuments()
      final at = file.findElementAt(myFixture.editor.caretModel.offset)
      final step = PsiTreeUtil.getParentOfType(at, GherkinStep)
      new GrStepDefinitionCreator().createStepDefinition(step, stepDef)
      PsiDocumentManager.getInstance(myFixture.project).commitAllDocuments()
    }

    assertEquals(expectedStepDef, stepDef.text)
  }
}
