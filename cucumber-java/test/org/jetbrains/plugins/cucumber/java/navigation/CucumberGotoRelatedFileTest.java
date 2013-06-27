package org.jetbrains.plugins.cucumber.java.navigation;

import com.intellij.ide.actions.GotoRelatedFileAction;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import java.util.List;

/**
 * User: Andrey.Vokin
 * Date: 3/25/13
 */
public class CucumberGotoRelatedFileTest extends LightPlatformCodeInsightFixtureTestCase {
  @SuppressWarnings("JUnitTestCaseWithNonTrivialConstructors")
  public CucumberGotoRelatedFileTest() {
    PlatformTestCase.autodetectPlatformPrefix();
  }

  public void testGotoRelated() {
    CucumberStepsIndex.getInstance(getProject()).reset();
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

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return DESCRIPTOR;
  }

  public static final DefaultLightProjectDescriptor DESCRIPTOR = new DefaultLightProjectDescriptor() {
    @Override
    public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
      PsiTestUtil.addLibrary(module, model, "cucumber-java", PathManager.getHomePath() + "/community/lib", "cucumber-java-1.0.14.jar");
    }
  };
}
