package org.angular2.codeInsight;

import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import static com.intellij.javascript.web.WebTestUtil.checkDocumentationAtCaret;

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

  private void doTest() {
    myFixture.configureByFiles(getTestName(true) + ".html",
                               "package.json", "deps/list-item.component.ts", "deps/ng_for_of.ts", "deps/ng_if.ts", "deps/dir.ts", "deps/ng_plural.ts");
    checkDocumentationAtCaret(myFixture);
  }

}
