package org.jetbrains.plugins.cucumber;

import com.intellij.codeInsight.CodeInsightTestCase;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.plugins.cucumber.psi.structure.GherkinStructureViewElement;

/**
 * User: Andrey.Vokin
 * Date: 1/10/13
 */
public class CucumberStructureViewTestCucumber extends CodeInsightTestCase {
  private static final String BASE_PATH = "/structureView/";

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
    configureByFile(BASE_PATH + getTestName(false) + ".feature");
    return getTopLevelItems();
  }

  private Object[] getTopLevelItems() {
    VirtualFile virtualFile = myFile.getVirtualFile();

    final FileType fileType = virtualFile.getFileType();

    TreeBasedStructureViewBuilder
      builder = (TreeBasedStructureViewBuilder)StructureViewBuilder.PROVIDER.getStructureViewBuilder(fileType, virtualFile, getProject());
    final StructureViewModel structureViewModel = builder.createStructureViewModel();

    Object[] children = structureViewModel.getRoot().getChildren();
    structureViewModel.dispose();
    return children;
  }

  @Override
  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath();
  }
}
