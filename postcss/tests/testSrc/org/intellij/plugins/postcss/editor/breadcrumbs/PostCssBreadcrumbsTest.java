package org.intellij.plugins.postcss.editor.breadcrumbs;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.ui.components.breadcrumbs.Crumb;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.postcss.PostCssFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/editor/breadcrumbs")
public class PostCssBreadcrumbsTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testCustomSelector() {
    doTest(":--selector");
  }

  @Test
  public void testCustomSelectorAtRule() {
    doTest("custom-selector :--selector");
  }

  @Test
  public void testCustomSelectorAtRuleEmpty() {
    doTest("custom-selector");
  }

  @Test
  public void testCustomMediaAtRule() {
    doTest("custom-media --my-media");
  }

  @Test
  public void testCustomMediaAtRuleEmpty() {
    doTest("custom-media");
  }

  @Test
  public void testApplyAtRule() {
    doTest("apply");
  }

  @Test
  public void testApplyAtRuleEmpty() {
    doTest("apply");
  }

  @Test
  public void testNestAtRule() {
    doTest("nest a, &");
  }

  @Test
  public void testAmpersandInClass() {
    doTest(".cla&s");
  }

  @Test
  public void testAmpersandInId() {
    doTest("#i&d");
  }

  @Test
  public void testAmpersandInSelector() {
    doTest("d&iv");
  }

  @Test
  public void testAmpersandInCustomSelector() {
    doTest(":--&elector");
  }

  private void doTest(String elementInfo) {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile(postCss.get().getTestName() + ".pcss");
    List<Crumb> breadcrumbs = ReadAction.computeBlocking(() -> myFixture.getBreadcrumbsAtCaret());
    assertFalse(breadcrumbs.isEmpty());
    assertEquals(elementInfo, ContainerUtil.getLastItem(breadcrumbs).getText());
  }
}