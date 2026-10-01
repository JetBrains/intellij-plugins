package org.angularjs.refactoring;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.refactoring.rename.HeadlessRenameProcessor;
import com.intellij.refactoring.rename.HeadlessRenamePsiElementProcessor;
import com.intellij.refactoring.rename.HeadlessRenameResult;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.angularjs.AngularTestUtil;
import org.angularjs.index.AngularJSDirectivesSupport;

public class DirectiveHeadlessRenameTest extends BasePlatformTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "rename";
  }

  public void testAttributeDirectiveRenamesItsUsages() {
    myFixture.configureByFiles("attribute.js", "attribute.html", "angular.js");
    JSImplicitElement directive = AngularJSDirectivesSupport.findDirective(getProject(), "fooBar");
    assertNotNull(directive);
    assertInstanceOf(HeadlessRenamePsiElementProcessor.processorOf(directive), AngularJSDirectiveRenameProcessor.class);

    HeadlessRenameResult planned = HeadlessRenameProcessor.analyze(getProject(), directive, "fooBar2");
    HeadlessRenameResult.Planned plan = assertInstanceOf(planned, HeadlessRenameResult.Planned.class);
    HeadlessRenameResult result = plan.getPlan().apply();

    assertInstanceOf(result, HeadlessRenameResult.Applied.class);
    myFixture.checkResultByFile("attribute.js", "attribute.after.js", false);
    myFixture.checkResult("attribute.html", """
      <style type="text/css">
        foo-bar {

        }
      </style>

      <div foo-bar2></div>""", true);
  }
}
