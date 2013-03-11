package org.jetbrains.plugins.cucumber.java.highlighting;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepInspection;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

/**
 * User: Andrey.Vokin
 * Date: 3/1/13
 */
public class CucumberHighlightingTest extends LightPlatformCodeInsightFixtureTestCase {
  public CucumberHighlightingTest() {
    PlatformTestCase.autodetectPlatformPrefix();
  }


  public void testStepParameterHighlighting() {
    doTest();
  }

  protected void doTest() {
    myFixture.enableInspections(new CucumberStepInspection());
    myFixture.copyDirectoryToProject("stepParameters", "");
    myFixture.configureByFile("stepParameters/test.feature");
    myFixture.testHighlighting(true, true, true);
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "highlighting";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.allowTreeAccessForAllFiles();
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return DESCRIPTOR;
  }

  public static final DefaultLightProjectDescriptor DESCRIPTOR = new DefaultLightProjectDescriptor() {
    @Override
    public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
      VirtualFile sourceRoot = VirtualFileManager.getInstance().refreshAndFindFileByUrl("temp:///src");
      if (sourceRoot != null) {
        contentEntry.removeSourceFolder(contentEntry.getSourceFolders()[0]);
        contentEntry.addSourceFolder(sourceRoot, true);
      }
    }
  };
}
