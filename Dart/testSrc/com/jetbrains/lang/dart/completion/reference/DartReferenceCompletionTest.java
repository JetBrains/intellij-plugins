package com.jetbrains.lang.dart.completion.reference;

import com.jetbrains.lang.dart.completion.base.DartCompletionTestBase;

public class DartReferenceCompletionTest extends DartCompletionTestBase {
  public DartReferenceCompletionTest() {
    super("completion", "references");
  }

  public void testTest7() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", testName + "Library.dart");
  }

  public void testTest8() throws Throwable {
    final String testName = getTestName(false);
    doTest(testName + ".dart", testName + "Foo.dart", testName + "Bar.dart");
  }

  public void testReferenceWEB6238() throws Throwable {
    myFixture.copyFileToProject("packages/foo/Foo.dart");
    myFixture.addFileToProject("pubspec.yaml", "");
    final String testName = getTestName(false);
    doTest(testName + ".dart", testName + "_root.dart");
  }
}
