package org.jetbrains.plugins.cucumber;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.jetbrains.plugins.cucumber.psi.structure.GherkinStructureViewElement;

import java.io.File;

/**
 * User: Andrey.Vokin
 * Date: 1/10/13
 */
public class CucumberStructureViewTestCucumber extends CodeInsightFixtureTestCase {
  private static final String BASE_PATH = "/structureView/";

  private VirtualFile myFile;

  public void setUp() throws Exception {
    PlatformTestCase.initPlatformPrefix(IDEA_MARKER_CLASS, "PlatformLangXml");
    super.setUp();
  }

  public void testStructureView() throws Exception {
    final Object[] objects = doSimpleTest();
    assertEquals(3, objects.length);

    final GherkinStructureViewElement background = (GherkinStructureViewElement)objects[0];
    assertEquals(4, background.getChildren().length);

    final GherkinStructureViewElement scenarioOne = (GherkinStructureViewElement)objects[1];
    assertEquals(3, scenarioOne.getChildren().length);

    final GherkinStructureViewElement scenarioTwo = (GherkinStructureViewElement)objects[2];
    assertEquals(4, scenarioTwo.getChildren().length);
  }

  protected Object[] doSimpleTest() throws Exception {
    final String relatedPath = BASE_PATH + getTestName(false) + ".feature";
    myFile =  LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + relatedPath.replace(File.separatorChar, '/'));
    myFixture.copyFileToProject(relatedPath);
    return getTopLevelItems();
  }

  private Object[] getTopLevelItems() {
    final FileType fileType = myFile.getFileType();

    TreeBasedStructureViewBuilder
      builder = (TreeBasedStructureViewBuilder)StructureViewBuilder.PROVIDER.getStructureViewBuilder(fileType, myFile, getProject());
    final StructureViewModel structureViewModel = builder.createStructureViewModel();

    Object[] children = structureViewModel.getRoot().getChildren();
    structureViewModel.dispose();
    return children;
  }

  @Override
  protected String getBasePath() {
    return CucumberTestUtil.getShortPluginPath() + CucumberTestUtil.getShortTestPath();
  }
}
