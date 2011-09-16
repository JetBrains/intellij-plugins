package com.google.jstestdriver.idea.assertFramework;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class TestFileStructurePack {
  private final List<AbstractTestFileStructure> myTestFileStructures;

  public TestFileStructurePack(List<AbstractTestFileStructure> testFileStructures) {
    myTestFileStructures = testFileStructures;
  }
}
