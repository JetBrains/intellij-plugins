// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil;

import com.intellij.openapi.util.TextRange;
import com.intellij.testFramework.UsefulTestCase;

import java.util.ArrayList;

public class HILInjectorTest extends UsefulTestCase {
  public void testOneInjection() throws Exception {
    doTestRanges("${}", 0, 3);
    doTestRanges("  ${}  ", 2, 3);
    doTestRanges("'${}'", 1, 3);
    doTestRanges("\"${}\"", 1, 3);
    doTestRanges("\"${{}}\"", 1, 5);
    doTestRanges("{${}}", 1, 3);
  }

  public void testInceptionInjection() throws Exception {
    doTestRanges("${${}}", 0, 6);
    doTestRanges("  ${${}}  ", 2, 6);
    doTestRanges("'${${}}'", 1, 6);
    doTestRanges("\"${${}}\"", 1, 6);
    doTestRanges("\"${${{}}}\"", 1, 8);
  }

  public void testIncorrectInceptionInjection() throws Exception {
    doTestRanges("${${}", 0, 5);
    doTestRanges("  ${${}  ", 2, 7);
    doTestRanges("'${${}'", 1, 6);
    doTestRanges("\"${${}\"", 1, 6);
    doTestRanges("${${}${}", 0, 8);
    doTestRanges("${${${}${}", 0, 10);
  }

  public void testMultipleInjections() throws Exception {
    doTestRanges("${}${}", 0, 3, 3, 3);
    doTestRanges("${}}${}", 0, 3, 4, 3);
    doTestRanges("${}{}${}", 0, 3, 5, 3);
    doTestRanges("  ${}  ${}  ", 2, 3, 7, 3);
    doTestRanges("\"${}\" '${}'", 1, 3, 7, 3);
    doTestRanges("\"${}' '${}\"", 1, 3, 7, 3);
  }

  public void testComplexInjection() throws Exception {
    doTestRanges("${a(\"b\",12)}", 0, 12);
  }

  public void testInjectionWithEscapes() throws Exception {
    String text = "${join(\"\\\",\\\"\", values(var.developers))}";
    doTestRanges(text, 0, text.length());
  }

  public void testNoInjection() throws Exception {
    doTestRanges("$${}");
    doTestRanges("  $${}  ");
    doTestRanges("'$${}'");
    doTestRanges("\"$${}\"");
    doTestRanges("%%{}");
    doTestRanges("  %%{}  ");
    doTestRanges("'%%{}'");
    doTestRanges("\"%%{}\"");
  }

  public void testMultilineInjection() throws Exception {
    doTestRanges("${\n}", 0, 4);
    doTestRanges("  ${\n}  ", 2, 4);
    doTestRanges("'${\n}'", 1, 4);
    doTestRanges("\"${\n}\"", 1, 4);
  }

  public void testClosingBraceInString() throws Exception {
    doTestRanges("${\"}\"}", 0, 6);
    doTestRanges("${replace(\"}\", \"$\")}", 0, 20);
  }

  public void testOneTemplate() throws Exception {
    doTestRanges("%{}", 0, 3);
    doTestRanges("%{else}", 0, 7);
    doTestRanges("  %{}  ", 2, 3);
    doTestRanges("'%{}'", 1, 3);
    doTestRanges("\"%{}\"", 1, 3);
  }

  public void testMixEscapingTemplate() throws Exception {
    doTestRanges("$%{}", 1, 3);
    doTestRanges("  %${}  ", 3, 3);
    doTestRanges("'$%{}'", 2, 3);
    doTestRanges("\"%${}\"", 2, 3);
  }

  /**
   * @param range pairs of [start, length]
   */
  private void doTestRanges(String text, int... range) throws Exception {
    final ArrayList<TextRange> expected = new ArrayList<>();
    for (int i = 0; i < range.length; i += 2) {
      final int start = range[i];
      final int end = start + range[i + 1];
      expected.add(new TextRange(start, end));
    }
    final ArrayList<TextRange> actual = ILLanguageInjector.Companion.getILRangesInText(text, ILLanguageInjector.Type.ANY);
    assertSameElements(actual, expected);
  }
}
