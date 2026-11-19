package org.angularjs.refactoring;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.refactoring.rename.HeadlessRenameProcessor;
import com.intellij.refactoring.rename.HeadlessRenamePsiElementProcessor;
import com.intellij.refactoring.rename.HeadlessRenameResult;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.angularjs.index.AngularJSDirectivesSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestApplication
public class DirectiveHeadlessRenameTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "rename");
  }

  @Test
  public void testAttributeDirectiveRenamesItsUsages() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("attribute.js", "attribute.html", "angular.js");
    JSImplicitElement directive = ReadAction.compute(() -> AngularJSDirectivesSupport.findDirective(myFixture.getProject(), "fooBar"));
    assertNotNull(directive);
    assertInstanceOf(AngularJSDirectiveRenameProcessor.class, ReadAction.compute(() -> HeadlessRenamePsiElementProcessor.processorOf(directive)));

    HeadlessRenameResult planned = ReadAction.compute(() -> HeadlessRenameProcessor.analyze(myFixture.getProject(), directive, "fooBar2"));
    HeadlessRenameResult.Planned plan = assertInstanceOf(HeadlessRenameResult.Planned.class, planned);
    HeadlessRenameResult result = EdtTestUtil.runInEdtAndGet(() -> plan.getPlan().apply());

    assertInstanceOf(HeadlessRenameResult.Applied.class, result);
    myFixture.checkResultByFile("attribute.js", "attribute.after.js", false);
    myFixture.checkResult("attribute.html", """
      <style type="text/css">
        foo-bar {

        }
      </style>

      <div foo-bar2></div>""", true);
  }
}
