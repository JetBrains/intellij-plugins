package com.jetbrains.lang.dart.completion.editor;

import com.jetbrains.lang.dart.completion.base.DartCompletionTestBase;

public class DartEditorCompletionTest extends DartCompletionTestBase {
  public DartEditorCompletionTest() {
    super("completion", "editor");
  }

  public void testGenericBrace1() throws Throwable {
    doTest('<');
  }

  public void testGenericBrace2() throws Throwable {
    doTest('<');
  }

  public void testGenericBrace3() throws Throwable {
    doTest('<');
  }

  public void testLess() throws Throwable {
    doTest('<');
  }
}
