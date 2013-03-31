package com.jetbrains.lang.dart.completion.reference;

import com.jetbrains.lang.dart.completion.base.DartCompletionTestBase;

/**
 * @author: Fedor.Korotkov
 */
public class DartReferenceCompletionTest extends DartCompletionTestBase {
  public DartReferenceCompletionTest() {
    super("completion", "references");
  }

  public void testTest7() throws Throwable {
    doTest("Test7.dart", "Test7Library.dart");
  }

  public void testTest8() throws Throwable {
    doTest("Test8Bar.dart", "Test8.dart", "Test8Foo.dart");
  }

  public void testReferenceWEB6238() throws Throwable {
    myFixture.copyFileToProject("packages/foo/Foo.dart");
    doTest("ReferenceWEB6238.dart", "ReferenceWEB6238_root.dart");
  }
}
