package com.intellij.flex.resolver;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

public class ActionScriptResolverLightTest extends LightCodeInsightFixtureTestCase {
  public void testJumpToDefaultConstructor() {
    myFixture.configureByText("sample.as", "public class AAAA {var a = new <caret>AAAA();}");
    myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION);
    myFixture.checkResult("public class <caret>AAAA {var a = new AAAA();}");
  }
}
