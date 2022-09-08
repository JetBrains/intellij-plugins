package org.intellij.plugins.postcss;

import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.UsefulTestCase;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@TestDataPath("$CONTENT_ROOT/testData/goto/")
public class PostCssGotoSymbolTest extends PostCssFixtureTestCase {
  public void testSelector() {
    doTest("selector", "#selector test.pcss:1", ".selector test.pcss:2", "#selector test.pcss:7", ".selector test.pcss:7");
  }

  public void testIdOnly() {
    doTest("#selector", "#selector test.pcss:1", "#selector test.pcss:7");
  }

  public void testClassOnly() {
    doTest(".selector", ".selector test.pcss:2", ".selector test.pcss:7");
  }

  public void testCustomSelector() {
    doTest("selector", "selector test.html:2", "selector test.pcss:1", "#selector test.html:4", "#selector test.pcss:6",
           ".selector test.html:5", ".selector test.pcss:7");
  }

  public void testCustomSelectorOnly() {
    doTest(":--selector", "selector test.html:2", "selector test.pcss:1");
  }

  public void testCustomSelectorAndOtherCustom() {
    doTest("--selector", "selector test.html:2", "selector test.pcss:1", "selector test.pcss:6", "selector test.pcss:9");
  }

  public void testCustomMedia() {
    doTest("media-query", "media-query test.html:2", "media-query test.pcss:1", "#media-query test.html:4", "#media-query test.pcss:3",
           ".media-query test.html:5", ".media-query test.pcss:4");
  }

  public void testCustomMediaOnly() {
    doTest("--media-query", "media-query test.html:2", "media-query test.pcss:1");
  }

  public void doTest(@NotNull String name, String @NotNull ... expectedNames) {
    myFixture.copyDirectoryToProject(getTestName(true), ".");
    ((PsiManagerEx)myFixture.getPsiManager()).setAssertOnFileLoadingFilter(VirtualFileFilter.ALL, myFixture.getTestRootDisposable());
    GotoSymbolModel2 model = new GotoSymbolModel2(myFixture.getProject(), myFixture.getTestRootDisposable());
    model.getNames(false);
    final ArrayList<String> actual = new ArrayList<>();
    for (Object o : model.getElementsByName(name, false, "")) {
      if (o instanceof NavigationItem) {
        final ItemPresentation presentation = ((NavigationItem)o).getPresentation();
        TestCase.assertNotNull(presentation);
        actual.add(presentation.getPresentableText() + " " + presentation.getLocationString());
      }
    }
    UsefulTestCase.assertSameElements(actual, expectedNames);
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "goto";
  }
}