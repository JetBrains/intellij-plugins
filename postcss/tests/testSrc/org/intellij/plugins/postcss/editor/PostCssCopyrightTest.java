package org.intellij.plugins.postcss.editor;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.maddyhome.idea.copyright.CopyrightProfile;
import com.maddyhome.idea.copyright.psi.UpdateCopyright;
import com.maddyhome.idea.copyright.psi.UpdateCopyrightFactory;
import org.intellij.plugins.postcss.PostCssFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/editor/copyright")
public class PostCssCopyrightTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testUpdateExistingComment() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile(postCss.get().getTestName() + ".pcss");

    updateCopyright();
    myFixture.checkResultByFile(postCss.get().getTestName() + "_after.pcss");

    updateCopyright();
    myFixture.checkResultByFile(postCss.get().getTestName() + "_after.pcss");
  }
  
  private void updateCopyright() {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    CopyrightProfile options = new CopyrightProfile();
    options.setNotice("copyright text\ncopyright text");
    options.setKeyword("Copyright");
    EdtTestUtil.runInEdtAndWait(() -> {
      UpdateCopyright copyright = UpdateCopyrightFactory.createUpdateCopyright(myFixture.getProject(), myFixture.getModule(),
                                                                               myFixture.getFile(), options);
      copyright.prepare();
      try {
        copyright.complete();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }
}