package com.jetbrains.lang.dart.completion.editor;

import com.jetbrains.lang.dart.completion.base.DartCompletionTestBase;

public class DartEditorCompletionTest extends DartCompletionTestBase {
  public DartEditorCompletionTest() {
    super("completion", "editor");
  }

  public void testDocComment() throws Throwable {
    doTest('\n');
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

  public void testString1() throws Throwable {
    doTest('{');
  }

  public void testString2() throws Throwable {
    doTest('{');
  }

  public void testString3() throws Throwable {
    doTest('{');
  }
}
