// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.testIntegration;

import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

public class DartTestFinderTest extends DartCodeInsightFixtureTestCase {

  PsiFile libFile;
  PsiFile testFile;
  PsiFile badLibFile;
  PsiFile badTestFile;
  PsiFile badTestFile2;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    libFile = myFixture.addFileToProject("lib/foo.dart", "foo() {}");
    testFile = myFixture.addFileToProject("test/foo_test.dart", "tests() {}");

    badLibFile = myFixture.addFileToProject("lib2/foo.dart", "foo() {}");
    badTestFile = myFixture.addFileToProject("test2/foo_test.dart", "tests() {}");
    badTestFile2 = myFixture.addFileToProject("test/foo_tes2t.dart", "tests() {}");
  }

  public void testIsDartLibFile() {
    assertFalse(DartTestFinder.isDartLibFile(null));
    assertTrue(DartTestFinder.isDartLibFile(libFile));
    assertFalse(DartTestFinder.isDartLibFile(testFile));

    assertFalse(DartTestFinder.isDartLibFile(badLibFile));
    assertFalse(DartTestFinder.isDartLibFile(badTestFile));
    assertFalse(DartTestFinder.isDartLibFile(badTestFile2));
  }

  public void testIsDartTestFile() {
    assertFalse(DartTestFinder.isDartTestFile(null));
    assertFalse(DartTestFinder.isDartTestFile(libFile));
    assertTrue(DartTestFinder.isDartTestFile(testFile));

    assertFalse(DartTestFinder.isDartTestFile(badLibFile));
    assertFalse(DartTestFinder.isDartTestFile(badTestFile));
    assertFalse(DartTestFinder.isDartTestFile(badTestFile2));
  }

  public void testGetGrandParentDirectoryPath() {
    assertNull(null);
    assertEquals("/src", DartTestFinder.getGrandParentDirectoryPath(libFile));
    assertEquals("/src", DartTestFinder.getGrandParentDirectoryPath(testFile));
  }
}
