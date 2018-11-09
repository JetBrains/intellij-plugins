package org.intellij.plugins.postcss.editor.breadcrumbs;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.impl.util.editor.CssBreadcrumbsInfoProvider;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;

@TestDataPath("$CONTENT_ROOT/testData/editor/breadcrumbs/")
public class PostCssBreadcrumbsTest extends PostCssFixtureTestCase {
  private static final CssBreadcrumbsInfoProvider BREADCRUMBS_INFO_PROVIDER = new CssBreadcrumbsInfoProvider();

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
    doTest("apply --my-custom-property-set");
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
    final PsiFile file = myFixture.configureByFile(getTestName(true) + ".pcss");
    PsiElement elementAtCaret = file.findElementAt(myFixture.getCaretOffset());
    while (elementAtCaret != null && !BREADCRUMBS_INFO_PROVIDER.acceptElement(elementAtCaret)) {
      elementAtCaret = elementAtCaret.getParent();
    }
    assertNotNull("Can't find element with breadcrumb", elementAtCaret);
    assertEquals(elementInfo, BREADCRUMBS_INFO_PROVIDER.getElementInfo(elementAtCaret));
  }
}