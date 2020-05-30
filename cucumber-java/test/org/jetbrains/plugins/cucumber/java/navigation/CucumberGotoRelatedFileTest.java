package org.jetbrains.plugins.cucumber.java.navigation;

import com.intellij.ide.actions.GotoRelatedSymbolAction;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

import java.util.List;

public class CucumberGotoRelatedFileTest extends CucumberJavaCodeInsightTestCase {
  public void testGotoRelated() {
    myFixture.copyDirectoryToProject("gotoRelated", "");
    myFixture.configureByFile("gotoRelated/test.feature");
    List<GotoRelatedItem> items = GotoRelatedSymbolAction.getItems(myFixture.getFile(), myFixture.getEditor(), null);
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
