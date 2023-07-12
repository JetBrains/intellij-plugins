// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.semantics;

import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCaseBase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lexer.Lexer;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angular2.lang.html.lexer.Angular2HtmlLexerTest;
import org.angularjs.AngularTestUtil;

import static com.intellij.lang.javascript.JSTestUtils.testMethodHasOption;
import static org.angular2.modules.Angular2TestModule.*;

public class Angular2SemanticsHighlightingTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  private void checkSymbolNames() {
    ExpectedHighlightingData data =
      new ExpectedHighlightingData(myFixture.getDocument(myFixture.getFile()), false, true, true, false);
    data.checkSymbolNames();
    data.init();
    ((CodeInsightTestFixtureImpl)myFixture).collectAndCheckHighlighting(data);
  }

  public void testCustomComponent() {
    configureCopy(myFixture, ANGULAR_CORE_8_2_14, ANGULAR_COMMON_8_2_14, ANGULAR_FORMS_8_2_14);
    myFixture.configureByFiles("customComponent.html", "customComponent.ts", "customComponent2.ts");
    checkSymbolNames();
  }
}
