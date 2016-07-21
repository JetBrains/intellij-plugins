package org.intellij.plugins.postcss.fileStructure;

import com.intellij.testFramework.FileStructureTestBase;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssTestUtils;

@TestDataPath("$CONTENT_ROOT/testData/fileStructure/")
public class PostCssFileStructureTest extends FileStructureTestBase {

  public void testNestAtRules() {
    checkTree();
  }
     
  public void testAmpersand() {
    checkTree();
}

  public void testAtRules() {
    checkTree();
  }

  public void testCustomSelectors() {
    checkTree();
  }

  public void testCustomSelectorsWithNesting() {
    checkTree();
  }

  @Override
  protected String getFileExtension() {
    return "pcss";
  }

  @Override
  protected String getBasePath() {
    return PostCssTestUtils.getTestDataBasePath(getClass());
  }
}