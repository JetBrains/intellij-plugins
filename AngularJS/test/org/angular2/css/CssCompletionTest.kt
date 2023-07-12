// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css;

import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import static com.intellij.util.containers.ContainerUtil.sorted;
import static java.util.Arrays.asList;

public class CssCompletionTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "completion";
  }

  public void testLocalCssCompletionHtml() {
    myFixture.configureByFiles("local-stylesheet-ext.html", "local-stylesheet-ext.ts", "local-stylesheet-ext.css", "package.json");
    AngularTestUtil.moveToOffsetBySignature("class=\"<caret>\"", myFixture);
    myFixture.completeBasic();
    assertEquals(asList("", "local-class-ext", "local-class-int"), sorted(myFixture.getLookupElementStrings()));
    myFixture.type('\n');
    AngularTestUtil.moveToOffsetBySignature("id=\"<caret>\"", myFixture);
    myFixture.completeBasic();
    assertEquals(asList("local-id-ext", "local-id-int"), sorted(myFixture.getLookupElementStrings()));
  }

  public void testLocalCssCompletionLocalCss() {
    myFixture.configureByFiles("local-stylesheet-ext.ts", "local-stylesheet-ext.css", "local-stylesheet-ext.html", "package.json");
    AngularTestUtil.moveToOffsetBySignature(".<caret> {", myFixture);
    myFixture.completeBasic();
    assertEquals(asList("class-in-html", "local-class-ext"), sorted(myFixture.getLookupElementStrings()));
    myFixture.type('\n');
    AngularTestUtil.moveToOffsetBySignature("#<caret> {", myFixture);
    myFixture.completeBasic();
    assertEquals(asList("id-in-html", "local-id-ext"), sorted(myFixture.getLookupElementStrings()));
  }

  public void testPreprocessorIncludePaths() {
    myFixture.addFileToProject("angular.json",
                               "{\"projects\":{\"foo\":{\"root\":\"src\",\"architect\":{\"build\":{\"builder\":\"z\"," +
                               "\"options\":{\"stylePreprocessorOptions\":{\"includePaths\":[\"src/foo\"]}}}}}}}");
    myFixture.addFileToProject("src/_var1.scss", "");
    myFixture.addFileToProject("src/foo/_var2.scss", "");
    myFixture.addFileToProject("src/foo/bar/_var3.scss", "");
    myFixture.addFileToProject("src/foobar/main.scss", "@import '<caret>';");
    myFixture.configureFromTempProjectFile("src/foobar/main.scss");
    myFixture.completeBasic();

    //todo remove src context
    assertSameElements(myFixture.getLookupElementStrings(), "~foo", "~foobar", "bar", "var2", "src");
  }

  public void testBaseURLPriority() {
    myFixture.addFileToProject("tsconfig.json", "{\"compilerOptions\": {\"baseUrl\": \"./src/foo\"}}");
    myFixture.addFileToProject("angular.json",
                               "{\"projects\":{\"foo\":{\"root\":\"src\",\"architect\":{\"build\":{\"builder\":\"z\"," +
                               "\"options\":{\"tsConfig\":\"tsconfig.json\",\"stylePreprocessorOptions\":{\"includePaths\":[\"src/foo\"]}}}}}}}");
    myFixture.addFileToProject("src/_var1.scss", "");
    myFixture.addFileToProject("src/foo/_var2.scss", "");
    myFixture.addFileToProject("src/foo/bar/_var3.scss", "");
    myFixture.addFileToProject("src/foobar/main.scss", "@import '<caret>';");
    myFixture.configureFromTempProjectFile("src/foobar/main.scss");
    myFixture.completeBasic();

    //todo remove src context
    assertSameElements(myFixture.getLookupElementStrings(), "~bar", "bar", "var2", "src");
  }

  public void testLegacyPreprocessorIncludePaths() {
    myFixture.addFileToProject(".angular-cli.json",
                               "{ \"apps\": [{\"root\": \"src\", \"stylePreprocessorOptions\": {\"includePaths\": [\"foo\"]}}]}");
    myFixture.addFileToProject("src/_var1.scss", "");
    myFixture.addFileToProject("src/foo/_var2.scss", "");
    myFixture.addFileToProject("src/foo/bar/_var3.scss", "");
    myFixture.configureByText("main.scss", "@import '<caret>';");
    myFixture.completeBasic();
    assertSameElements(myFixture.getLookupElementStrings(), "src", "~foo", "bar", "var2");
  }
}
