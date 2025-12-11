package com.intellij.flex.resolver;

import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase;
import com.intellij.openapi.actionSystem.IdeActions;

public class ActionScriptResolverLightTest extends JSDaemonAnalyzerLightTestCase {
  public void testJumpToDefaultConstructor() {
    myFixture.configureByText("sample.as", "public class AAAA {var a = new <caret>AAAA();}");
    myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION);
    myFixture.checkResult("public class <caret>AAAA {var a = new AAAA();}");
  }

  @Override
  protected String getBasePath() {
    return "";
  }

  @Override
  protected String getExtension() {
    return "as";
  }
}
