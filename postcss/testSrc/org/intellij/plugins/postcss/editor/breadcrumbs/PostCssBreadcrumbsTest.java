package org.intellij.plugins.postcss.editor.breadcrumbs;

import com.intellij.testFramework.TestDataPath;
import com.intellij.ui.components.breadcrumbs.Crumb;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;

import java.util.List;

@TestDataPath("$CONTENT_ROOT/testData/editor/breadcrumbs/")
public class PostCssBreadcrumbsTest extends PostCssFixtureTestCase {
  public void testCustomSelector() {
    doTest(":--selector");
  }

  public void testCustomSelectorAtRule() {
    doTest("custom-selector :--selector");
  }

  public void testCustomSelectorAtRuleEmpty() {
    doTest("custom-selector");
  }

  public void testCustomMediaAtRule() {
    doTest("custom-media --my-media");
  }

  public void testCustomMediaAtRuleEmpty() {
    doTest("custom-media");
  }

  public void testApplyAtRule() {
    doTest("apply");
  }

  public void testApplyAtRuleEmpty() {
    doTest("apply");
  }

  public void testNestAtRule() {
    doTest("nest a, &");
  }

  public void testAmpersandInClass() {
    doTest(".cla&s");
  }

  public void testAmpersandInId() {
    doTest("#i&d");
  }

  public void testAmpersandInSelector() {
    doTest("d&iv");
  }

  public void testAmpersandInCustomSelector() {
    doTest(":--&elector");
  }

  private void doTest(String elementInfo) {
    myFixture.configureByFile(getTestName(true) + ".pcss");
    List<Crumb> breadcrumbs = myFixture.getBreadcrumbsAtCaret();
    assertNotEmpty(breadcrumbs);
    assertEquals(elementInfo, ContainerUtil.getLastItem(breadcrumbs).getText());
  }
}