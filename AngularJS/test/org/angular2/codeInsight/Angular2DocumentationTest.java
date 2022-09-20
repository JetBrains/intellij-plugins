package org.angular2.codeInsight;

import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import static com.intellij.webSymbols.WebTestUtil.checkDocumentationAtCaret;
import static org.angular2.modules.Angular2TestModule.*;

public class Angular2DocumentationTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "documentation";
  }

  public void testTagName() {
    doTest();
  }

  public void testSimpleInput() {
    doTest();
  }

  public void testSimpleInputBinding() {
    doTest();
  }

  public void testSimpleOutputBinding() {
    doTest();
  }

  public void testSimpleBananaBox() {
    doTest();
  }

  public void testDirectiveWithMatchingInput() {
    doTest();
  }

  public void testDirectiveWithoutMatchingInput() {
    doTest();
  }

  public void testGlobalAttribute() {
    doTest();
  }

  public void testFieldWithoutDocs() {
    doTest();
  }

  public void testFieldWithDocsPrivate() {
    doTest();
  }

  public void testExtendedEventKey() {
    doTest();
  }

  public void testCdkNoDataRow() {
    configureLink(myFixture, ANGULAR_CDK_14_2_0);
    myFixture.configureByFile(getTestName(true) + ".html");
    checkDocumentationAtCaret(myFixture);
  }

  public void testCdkNoDataRowNotImported() {
    configureCopy(myFixture, ANGULAR_CDK_14_2_0);
    myFixture.configureByFiles(getTestName(true) + ".html", getTestName(true) + ".ts");
    checkDocumentationAtCaret(myFixture);
  }

  public void testUnknownDirective() {
    doTest();
  }

  private void doTest() {
    myFixture.configureByFiles(getTestName(true) + ".html",
                               "package.json", "deps/list-item.component.ts", "deps/ng_for_of.ts", "deps/ng_if.ts", "deps/dir.ts",
                               "deps/ng_plural.ts");
    checkDocumentationAtCaret(myFixture);
  }
}
