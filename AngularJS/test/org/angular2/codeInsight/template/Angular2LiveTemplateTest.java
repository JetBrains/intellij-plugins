// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.impl.LiveTemplateCompletionContributor;
import org.angular2.Angular2CodeInsightFixtureTestCase;

import static com.intellij.lang.javascript.BaseJSCompletionTestCase.checkNoCompletion;
import static com.intellij.lang.javascript.BaseJSCompletionTestCase.checkWeHaveInCompletion;

public class Angular2LiveTemplateTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    LiveTemplateCompletionContributor.setShowTemplatesInTests(true, getTestRootDisposable());
  }

  public void testComponent() {
    myFixture.configureByText("foo.ts", "a-comp<caret>");
    LookupElement[] elements = myFixture.completeBasic();
    checkWeHaveInCompletion(elements, "a-component");
  }

  public void testRxjsOperatorImport() {
    myFixture.configureByText("foo.ts", "a-rxjs-operator-imp<caret>");
    LookupElement[] elements = myFixture.completeBasic();
    checkWeHaveInCompletion(elements, "a-rxjs-operator-import");
  }

  public void testComponentNoCompletion() {
    myFixture.configureByText("foo.ts", "var a = a-comp<caret>");
    LookupElement[] elements = myFixture.completeBasic();
    checkNoCompletion(elements, "a-component");
  }
  
  public void testOutputEvent() {
    myFixture.configureByText("foo.ts", "class Foo {\na-outp<caret>\n}");
    LookupElement[] elements = myFixture.completeBasic();
    checkWeHaveInCompletion(elements, "a-output-event");
  }

  public void testOutputEventLastPart() {
    myFixture.configureByText("foo.ts", "class Foo {\na-output-ev<caret>\n}");
    LookupElement[] elements = myFixture.completeBasic();
    checkWeHaveInCompletion(elements, "a-output-event");
  }

  public void testRoutePath404() {
    myFixture.configureByText("foo.ts", "var z = [a-rou<caret>]");
    LookupElement[] elements = myFixture.completeBasic();
    checkWeHaveInCompletion(elements, "a-route-path-404");
  }
}
