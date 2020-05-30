// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.testIntegration;

import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

public class DartTestFinderTest extends DartCodeInsightFixtureTestCase {

  public void testIsTest() {
    myFixture.addFileToProject("pubspec.yaml", "");
    doTestIsTest("foo_test.dart", false);
    doTestIsTest("test/footest.dart", false);
    doTestIsTest("test/foo_test.txt", false);
    doTestIsTest("test/foo_test.dart", true);
    doTestIsTest("test/src/footest.dart", false);
    doTestIsTest("test/src/foo_test.dart", true);
    doTestIsTest("test/src/a/b/c/footest.dart", false);
    doTestIsTest("test/src/a/b/c/foo_test.dart", true);
    doTestIsTest("lib/foo_test.dart", false);
    doTestIsTest("web/foo_test.dart", false);
    doTestIsTest("bin/src/foo_test.dart", false);
  }

  private void doTestIsTest(String relPath, boolean expectedIsTest) {
    PsiFile file = myFixture.addFileToProject(relPath, "");
    assertEquals(relPath, expectedIsTest, new DartTestFinder().isTest(file));
  }

  public void testFindTest() {
    myFixture.addFileToProject("pubspec.yaml", "");
    PsiFile testFoo = myFixture.addFileToProject("test/foo.dart", "");
    PsiFile testFooTest = myFixture.addFileToProject("test/foo_test.dart", "");
    myFixture.addFileToProject("test/footest.dart", "");
    PsiFile testSrcFooTest = myFixture.addFileToProject("test/src/a/b/c/foo_test.dart", "");
    myFixture.addFileToProject("test/src/a/b/c/foo_bar_test.dart", "");
    PsiFile binFoo = myFixture.addFileToProject("bin/src/x/y/z/foo.dart", "");
    PsiFile binFooNotDart = myFixture.addFileToProject("bin/src/x/y/z/foo.txt", "");
    PsiFile libFoo = myFixture.addFileToProject("lib/foo.dart", "");

    DartTestFinder testFinder = new DartTestFinder();
    assertEmpty(testFinder.findTestsForClass(testFoo));
    assertEmpty(testFinder.findTestsForClass(binFooNotDart));
    assertSameElements(testFinder.findTestsForClass(binFoo), testFooTest, testSrcFooTest);
    assertSameElements(testFinder.findTestsForClass(libFoo), testFooTest, testSrcFooTest);
  }

  public void testFindSubjectByTest() {
    myFixture.addFileToProject("pubspec.yaml", "");
    PsiFile testFoo = myFixture.addFileToProject("test/foo.dart", "");
    PsiFile testFooTest = myFixture.addFileToProject("test/foo_test.dart", "");
    myFixture.addFileToProject("test/footest.dart", "");
    PsiFile testSrcFooTest = myFixture.addFileToProject("test/src/a/b/c/foo_test.dart", "");
    PsiFile binFoo = myFixture.addFileToProject("bin/src/x/y/z/foo.dart", "");
    myFixture.addFileToProject("bin/src/x/y/z/foo.txt", "");
    PsiFile libFoo = myFixture.addFileToProject("lib/foo.dart", "");
    myFixture.addFileToProject("lib/foo_bar.dart", "");
    PsiFile libFooTest = myFixture.addFileToProject("lib/foo_test.dart", "");

    DartTestFinder testFinder = new DartTestFinder();
    assertEmpty(testFinder.findClassesForTest(testFoo));
    assertEmpty(testFinder.findClassesForTest(libFooTest));
    assertSameElements(testFinder.findClassesForTest(testFooTest), binFoo, libFoo);
    assertSameElements(testFinder.findClassesForTest(testSrcFooTest), binFoo, libFoo);
  }
}
