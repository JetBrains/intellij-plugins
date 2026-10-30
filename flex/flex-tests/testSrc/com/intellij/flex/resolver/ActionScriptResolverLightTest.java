package com.intellij.flex.resolver;

import com.intellij.lang.javascript.JSDaemonAnalyzerFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSDaemonAnalyzerFixture.jsDaemonAnalyzerFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;

@TestApplication
public class ActionScriptResolverLightTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSDaemonAnalyzerFixture> js =
    jsDaemonAnalyzerFixture(projectFixture, new JSFixtureOptions(JSTestUtils.getTestDataPath(), "as"));

  @Test
  public void testJumpToDefaultConstructor() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    myFixture.configureByText("sample.as", "public class AAAA {var a = new <caret>AAAA();}");
    myFixture.performEditorAction(IdeActions.ACTION_GOTO_DECLARATION);
    myFixture.checkResult("public class <caret>AAAA {var a = new AAAA();}");
  }
}
