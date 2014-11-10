package com.jetbrains.lang.dart.ide.fileStructure;

public class DartFileStructureSelectionTest extends DartFileStructureTestCase {
  @Override
  protected String getTestDataFolderName() {
    return "selection/";
  }

  public void testConstructor() throws Exception {
    checkTree();
  }

  public void testEnums() throws Exception {
    checkTree();
  }

  public void testInsideClass() throws Exception {
    checkTree();
  }

  public void testTopLevel() throws Exception {
    checkTree();
  }
}
