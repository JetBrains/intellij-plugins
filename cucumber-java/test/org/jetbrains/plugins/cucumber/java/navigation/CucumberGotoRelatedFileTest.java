package org.jetbrains.plugins.cucumber.java.navigation;

import com.intellij.ide.actions.GotoRelatedFileAction;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

import java.util.List;

/**
 * User: Andrey.Vokin
 * Date: 3/25/13
 */
public class CucumberGotoRelatedFileTest extends LightPlatformCodeInsightFixtureTestCase {
  public CucumberGotoRelatedFileTest() {
    PlatformTestCase.autodetectPlatformPrefix();
  }

  public void testGotoRelated() {
    myFixture.copyDirectoryToProject("gotoRelated", "");
    myFixture.configureByFile("gotoRelated/test.feature");
    List<GotoRelatedItem> items = GotoRelatedFileAction.getItems(myFixture.getFile(), myFixture.getEditor(), null);
    assertEquals(1, items.size());
    PsiElement gotoElement = items.get(0).getElement();
    assertTrue(gotoElement instanceof PsiJavaFile);
    assertEquals("ShoppingStepdefs.java", ((PsiJavaFile)gotoElement).getName());
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH;
  }
}
