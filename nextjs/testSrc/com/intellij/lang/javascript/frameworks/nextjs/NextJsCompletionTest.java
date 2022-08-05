package com.intellij.lang.javascript.frameworks.nextjs;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.javascript.BaseJSCompletionTestCase;

public class NextJsCompletionTest extends BaseJSCompletionTestCase {

  @Override
  protected String getExtension() {
    return "js";
  }
  
  public void testSimple() {
    myFixture.addFileToProject("pages/smth/component1.js", "export default function Test1() {return <div></div>}");
    myFixture.addFileToProject("pages/smth/component2.js", "export default function Test2() {return <div></div>}");
    myFixture.addFileToProject("pages/test.js", "<form action=\"/smth/<caret>\"");
    myFixture.configureFromTempProjectFile("pages/test.js");
    LookupElement[] elements = myFixture.completeBasic();
    checkWeHaveInCompletion(elements, "component1", "component2");
  }
}
