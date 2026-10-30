package com.intellij.lang.javascript.frameworks.nextjs;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.javascript.BaseJSCompletionTestCase.LookupString;
import com.intellij.lang.javascript.JSCompletionFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSCompletionFixture.checkWeHaveInCompletion;
import static com.intellij.lang.javascript.JSCompletionFixture.jsCompletionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class NextJsCompletionTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSCompletionFixture> js =
    jsCompletionFixture(projectFixture, new JSFixtureOptions(JSTestUtils.getTestDataPath() + JSCompletionFixture.BASE_PATH, "js"));

  @Test
  public void testSimple() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    myFixture.addFileToProject("pages/smth/component1.js", "export default function Test1() {return <div></div>}");
    myFixture.addFileToProject("pages/smth/component2.js", "export default function Test2() {return <div></div>}");
    myFixture.addFileToProject("pages/test.js", "<form action=\"/smth/<caret>\"");
    myFixture.configureFromTempProjectFile("pages/test.js");
    LookupElement[] elements = myFixture.completeBasic();
    checkWeHaveInCompletion(elements,
                            new LookupString("component1", "component1.js"),
                            new LookupString("component2", "component2.js"));
  }
}
