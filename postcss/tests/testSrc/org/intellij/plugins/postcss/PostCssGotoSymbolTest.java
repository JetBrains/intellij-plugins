package org.intellij.plugins.postcss;

import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/goto")
public class PostCssGotoSymbolTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testSelector() {
    doTest("selector", "#selector test.pcss:1", ".selector test.pcss:2", "#selector test.pcss:7", ".selector test.pcss:7");
  }

  @Test
  public void testIdOnly() {
    doTest("#selector", "#selector test.pcss:1", "#selector test.pcss:7");
  }

  @Test
  public void testClassOnly() {
    doTest(".selector", ".selector test.pcss:2", ".selector test.pcss:7");
  }

  @Test
  public void testCustomSelector() {
    doTest("selector", "selector test.html:2", "selector test.pcss:1", "#selector test.html:4", "#selector test.pcss:6",
           ".selector test.html:5", ".selector test.pcss:7");
  }

  @Test
  public void testCustomSelectorOnly() {
    doTest(":--selector", "selector test.html:2", "selector test.pcss:1");
  }

  @Test
  public void testCustomSelectorAndOtherCustom() {
    doTest("--selector", "selector test.html:2", "selector test.pcss:1", "selector test.pcss:6", "selector test.pcss:9");
  }

  @Test
  public void testCustomMedia() {
    doTest("media-query", "media-query test.html:2", "media-query test.pcss:1", "#media-query test.html:4", "#media-query test.pcss:3",
           ".media-query test.html:5", ".media-query test.pcss:4");
  }

  @Test
  public void testCustomMediaOnly() {
    doTest("--media-query", "media-query test.html:2", "media-query test.pcss:1");
  }

  public void doTest(@NotNull String name, String @NotNull ... expectedNames) {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.copyDirectoryToProject(postCss.get().getTestName(), ".");
    ((PsiManagerEx)myFixture.getPsiManager()).setAssertOnFileLoadingFilter(VirtualFileFilter.ALL, myFixture.getTestRootDisposable());
    GotoSymbolModel2 model = new GotoSymbolModel2(myFixture.getProject(), myFixture.getTestRootDisposable());
    final ArrayList<String> actual = ReadAction.computeBlocking(() -> {
      model.getNames(false);
      final ArrayList<String> result = new ArrayList<>();
      for (Object o : model.getElementsByName(name, false, "")) {
        if (o instanceof NavigationItem) {
          final ItemPresentation presentation = ((NavigationItem)o).getPresentation();
          assertNotNull(presentation);
          result.add(presentation.getPresentableText() + " " + presentation.getLocationString());
        }
      }
      return result;
    });
    assertThat(actual).hasSameElementsAs(Arrays.asList(expectedNames));
  }
}