package org.intellij.plugins.postcss.editor;

import com.maddyhome.idea.copyright.CopyrightProfile;
import com.maddyhome.idea.copyright.psi.UpdateCopyright;
import com.maddyhome.idea.copyright.psi.UpdateCopyrightFactory;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class PostCssCopyrightTest extends PostCssFixtureTestCase {
  public void testUpdateExistingComment() {
    myFixture.configureByFile(getTestName(true) + ".pcss");

    updateCopyright();
    myFixture.checkResultByFile(getTestName(true) + "_after.pcss");

    updateCopyright();
    myFixture.checkResultByFile(getTestName(true) + "_after.pcss");
  }
  
  private void updateCopyright() {
    CopyrightProfile options = new CopyrightProfile();
    options.setNotice("copyright text\ncopyright text");
    options.setKeyword("Copyright");
    UpdateCopyright copyright = UpdateCopyrightFactory.createUpdateCopyright(myFixture.getProject(), myFixture.getModule(),
                                                                             myFixture.getFile(), options);
    copyright.prepare();
    try {
      copyright.complete();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "copyright";
  }
}