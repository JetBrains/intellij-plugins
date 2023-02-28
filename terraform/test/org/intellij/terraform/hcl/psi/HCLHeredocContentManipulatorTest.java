// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightPlatformTestCase;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

@SuppressWarnings({"WeakerAccess", "ArraysAsListWithZeroOrOneArgument"})
public class HCLHeredocContentManipulatorTest extends LightPlatformTestCase {
  protected HCLElementGenerator myElementGenerator;
  protected HCLHeredocContentManipulator myContentManipulator;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myElementGenerator = createElementGenerator();
    myContentManipulator = new HCLHeredocContentManipulator();
  }

  @NotNull
  protected HCLElementGenerator createElementGenerator() {
    return new HCLElementGenerator(getProject());
  }

  public void testEmptyContent() throws Exception {
    final HCLHeredocContent content = content();
    final List<String> lines = content.getLines();
    assertEquals(Collections.<String>emptyList(), lines);
  }

  public void testSingleEmptyLineContent() throws Exception {
    final HCLHeredocContent content = content("");
    dump(content);
    final List<String> lines = content.getLines();
    then(lines).hasSize(1).containsOnly("");
  }

  public void testContentLines() throws Exception {
    final HCLHeredocContent content = content("abc");
    dump(content);
    final List<String> lines = content.getLines();
    assertEquals(Arrays.asList("abc"), lines);
  }

  public void testContentNodeTree() throws Exception {
    final HCLHeredocContent content = content("abc");
    dump(content);
    assertEquals(1, content.getLinesCount());
    final List<String> lines = content.getLines();
    assertEquals(Arrays.asList("abc"), lines);
    assertEquals("abc\n", content.getValue());
  }

  public void testKeepSame() throws Exception {
    doTest(content("abc"), TextRange.create(0, 3), "abc", "abc");
  }

  public void testReplaceSingleLineContent() throws Exception {
    doTest(content("abc"), TextRange.create(0, 1), "!", "!bc");
    doTest(content("abc"), TextRange.create(0, 2), "!", "!c");
    doTest(content("abc"), TextRange.create(0, 3), "!", "!");
    doTest(content("abc"), TextRange.create(1, 2), "!", "a!c");
    doTest(content("abc"), TextRange.create(1, 3), "!", "a!");
    doTest(content("abc"), TextRange.create(2, 3), "!", "ab!");
  }

  public void testLineBetweenOthers() throws Exception {
    doTest(content("012", "456", "890"), TextRange.create(0, 3), "!", "!", "456", "890");
    doTest(content("012", "456", "890"), TextRange.create(0, 4), "!", "!456", "890");
    doTest(content("012", "456", "890"), TextRange.create(4, 7), "!", "012", "!", "890");
    doTest(content("012", "456", "890"), TextRange.create(4, 8), "!", "012", "!890");
    doTest(content("012", "456", "890"), TextRange.create(7, 10), "!", "012", "456!0");
    doTest(content("012", "456", "890"), TextRange.create(7, 11), "!", "012", "456!");
    doTest(content("012", "456", "890"), TextRange.create(7, 12), "!", "012", "456!");
  }

  public void testSeparateSingleLine() throws Exception {
    doTest(content("abc"), TextRange.create(0, 1), "\n", "", "bc");
    doTest(content("abc"), TextRange.create(1, 2), "\n", "a", "c");
    doTest(content("abc"), TextRange.create(2, 3), "\n", "ab", "");

    doTest(content("abc"), TextRange.create(0, 1), "a\n", "a", "bc");
    doTest(content("abc"), TextRange.create(1, 2), "b\n", "ab", "c");
    doTest(content("abc"), TextRange.create(2, 3), "c\n", "abc", "");

    doTest(content("abc"), TextRange.create(0, 4), "\nabc", "", "abc");
    doTest(content("abc"), TextRange.create(0, 4), "a\nbc", "a", "bc");
    doTest(content("abc"), TextRange.create(0, 4), "ab\nc", "ab", "c");
    doTest(content("abc"), TextRange.create(0, 4), "abc\n", "abc");
  }

  public void testSeparateMultipleLines() throws Exception {
    doTest(content("012", "456", "890"), TextRange.create(0, 4), "\n", "", "456", "890");
    doTest(content("012", "456", "890"), TextRange.create(4, 7), "\n", "012", "", "", "890");
    doTest(content("012", "456", "890"), TextRange.create(7, 10), "\n", "012", "456", "0");
    doTest(content("012", "456", "890"), TextRange.create(7, 11), "\n", "012", "456", "");
    doTest(content("012", "456", "890"), TextRange.create(7, 12), "\n", "012", "456");
    doTest(content("012", "456", "890"), TextRange.create(8, 10), "\n", "012", "456", "", "0");
    doTest(content("012", "456", "890"), TextRange.create(4, 12), "\n", "012", "");
  }

  public void testReplaceTextAcrossLines() throws Exception {
    doTest(content("012", "456", "890"), TextRange.create(2, 6), "x", "01x6", "890");
    doTest(content("012", "456", "890"), TextRange.create(2, 8), "x", "01x890");
    doTest(content("012", "456", "890"), TextRange.create(2, 8), "\n", "01", "890");
  }

  public void testNewLineAddedBetweenExistingLineAndEol() throws Exception {
    final HCLHeredocContent before = content("abc");
    final ASTNode firstChildNode = before.getNode().getFirstChildNode();
    final ASTNode lastChildNode = before.getNode().getLastChildNode();
    dump(before);
    final HCLHeredocContent after = doReplace(before, TextRange.create(0, 4), "abc\ndef\n");
    then(after).isNotNull();
    dump(after);
    then(after.getNode().getLastChildNode()).isNotNull().isSameAs(lastChildNode);
    then(after.getNode().getFirstChildNode()).isNotNull().isSameAs(firstChildNode);
  }

  public void testNewLineSeparatorInLineWouldKeepLastEol() throws Exception {
    doTestLastEolKeepIntact("012", TextRange.create(0, 1), "\n");
    doTestLastEolKeepIntact("012", TextRange.create(1, 2), "\n");
    doTestLastEolKeepIntact("012", TextRange.create(2, 3), "\n");
    doTestLastEolKeepIntact("012", TextRange.create(0, 3), "\n");
  }

  public void testRemovedLineSeparatorInLineWouldKeepLastEol() throws Exception {
    doTestLastEolKeepIntact("0\n2", TextRange.create(1, 2), "1");
    doTestLastEolKeepIntact("0\n2\n4", TextRange.create(1, 2), "1");
    doTestLastEolKeepIntact("0\n2\n4\n6\n8", TextRange.create(1, 8), "xyz");
  }

  public void testRemovedLineWouldKeepLastEol() throws Exception {
    doTestLastEolKeepIntact("0\n2", TextRange.create(0, 2), "");
    doTestLastEolKeepIntact("0\n2\n4", TextRange.create(2, 4), "");
    doTestLastEolKeepIntact("0\n2\n4\n6\n8", TextRange.create(2, 8), "xyz");
  }

  public void testFullInjectionLikeChangeKeepLastEol() throws Exception {
    doTestLastEolKeepIntact("0\n2", TextRange.create(0, 3), "0\n2");
    doTestLastEolKeepIntact("0\n2", TextRange.create(0, 3), "012");
    doTestLastEolKeepIntact("0\n2\n4", TextRange.create(0, 5), "0\n2\n4");
    doTestLastEolKeepIntact("0\n2\n4", TextRange.create(0, 5), "0\nx\n4");
    doTestLastEolKeepIntact("0\n2\n4", TextRange.create(0, 5), "xyz");
    doTestLastEolKeepIntact("0\n2\n4\n6\n8", TextRange.create(2, 9), "012\n456\n8");
    doTestLastEolKeepIntact("012\n456\n8", TextRange.create(2, 9), "0\n2\n4\n6\n8");

    doTestLastEolKeepIntact("012\n${}\n8", TextRange.create(2, 9), "0\n2\n${}\n8");
    doTestLastEolKeepIntact("012\n${}\n8", TextRange.create(2, 9), "0\n2\n${\n}\n8");

    doTestLastEolKeepIntact("012\n${}\n8", TextRange.create(2, 9), "");
  }

  private void doTestLastEolKeepIntact(String s, TextRange range, String replacement) {
    final HCLHeredocContent before = content(s);
    final ASTNode lastChildNode = before.getNode().getLastChildNode();
    dump(before);
    final HCLHeredocContent after = doReplace(before, range, replacement);
    then(after).isNotNull();
    dump(after);
    then(after.getNode().getLastChildNode()).isNotNull().isSameAs(lastChildNode);
  }

  public void testReplaceEmptyString() throws Exception {
    doTest(content(), TextRange.create(0, 0), "text", "text");
    doTest(content(), TextRange.create(0, 0), "te\nxt", "te", "xt");
    doTestFullTextReplacement(content(), "text", content("text"));
    doTestFullTextReplacement(content(), "te\nxt", content("te", "xt"));

    doTest(content("", "", ""), TextRange.create(0, 3), "text", "text");
  }

  public void testMayBeEmptiedCompletely() throws Exception {
    doTestText(content("a\nb\nc"), TextRange.from(0, 6), "", content());
  }

  public void testReplaceInText() throws Exception {
    doTestText(
        content("""
                  [
                    {
                      "name": "jenkins",
                      "image": "jenkins",
                      "cpu": 10,
                      "memory": 500,
                      "essential": true,
                      "portMappings": [
                        {
                          "containerPort": 80,
                          "hostPort": 80
                        }
                      ]
                    }
                  ]"""),
        TextRange.from(19, 7), "leeroy",
        content("""
                  [
                    {
                      "name": "leeroy",
                      "image": "jenkins",
                      "cpu": 10,
                      "memory": 500,
                      "essential": true,
                      "portMappings": [
                        {
                          "containerPort": 80,
                          "hostPort": 80
                        }
                      ]
                    }
                  ]"""));
  }

  public void testReplaceFullText() throws Exception {
    doTestFullTextReplacement(content("jenkins"), "leeroy", content("leeroy"));
    doTestFullTextReplacement(content("jenkins"), "leeroy\n", content("leeroy"));
    doTestFullTextReplacement(content("a\nb"), "c", content("c"));
    doTestFullTextReplacement(content("a\nb"), "c\n", content("c"));
    doTestFullTextReplacement(content("a\nb"), "a\nb\nc\n", content("a\n", "b\n", "c\n"));
    doTestFullTextReplacement(content("a\nb"), "a\nb\nc\n", content("a", "b", "c"));
    doTestFullTextReplacement(content("a\nb"), "\n\n\n", content("", "", ""));
    doTestFullTextReplacement(content(""), "x", content("x"));
    doTestFullTextReplacement(content(""), "", content());
    doTestFullTextReplacement(content(""), "\n", content(""));
    doTestFullTextReplacement(content(), "\n", content(""));
  }

  public void testReplaceFullText2() throws Exception {
    doTestFullTextReplacement(content(false, "jenkins\n"), "leeroy\n", content(false, "leeroy\n"));
  }

  public void testReplacementLines() throws Exception {
    doReplacementLinesTest("");
    doReplacementLinesTest("\n", "", true);
    doReplacementLinesTest("a\n", "a", true);
    doReplacementLinesTest("leeroy\njenkins\n", "leeroy", true, "jenkins", true);
    doReplacementLinesTest("a", "a", false);
    doReplacementLinesTest("leeroy\njenkins", "leeroy", true, "jenkins", false);
  }

  @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
  private void doReplacementLinesTest(String input, Object... expected) {
    final List<Pair<String, Boolean>> list = HCLHeredocContentManipulator.Companion.getReplacementLines(input);
    final ArrayList<Pair> pairs = new ArrayList<>(expected.length / 2);
    for (int i = 0; i < expected.length; i++) {
      Object s = expected[i];
      Object b = expected[i + 1];
      i++;
      pairs.add(new Pair(s, b));
    }
    assertEquals(pairs, list);
  }

  private String replaceEOLs(String s) {
    return s.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
  }

  protected void doTest(final HCLHeredocContent content, final TextRange range, final String replacement, String... expected) {
    dump(content);
    final HCLHeredocContent changed = doReplace(content, range, replacement);
    assertNotNull(changed);
    dump(changed);
    final List<String> lines = changed.getLines();
    assertEquals(Arrays.asList(expected), removeSuffix(lines, "\n"));
  }

  @SuppressWarnings("unused")
  private void dump(PsiElement element) {
    // System.out.println(DebugUtil.psiToString(element, false, true));
  }

  @NotNull
  private HCLHeredocContent doReplace(final HCLHeredocContent content, final TextRange range, final String replacement) {
    final HCLHeredocContent[] changed = new HCLHeredocContent[1];
    ApplicationManager.getApplication().runWriteAction(() -> {
      changed[0] = myContentManipulator.handleContentChange(content, range, replacement);
    });
    return changed[0];
  }

  protected void doTestText(final HCLHeredocContent content, final TextRange range, final String replacement, HCLHeredocContent expected) {
    dump(content);
    final HCLHeredocContent changed = doReplace(content, range, replacement);
    assertNotNull(changed);
    dump(changed);
    assertEquals(replaceEOLs(expected.getText()), replaceEOLs(changed.getText()));
  }

  protected void doTestFullTextReplacement(final HCLHeredocContent content, final String replacement, HCLHeredocContent expected) {
    dump(content);
    final HCLHeredocContent[] changed = new HCLHeredocContent[1];
    ApplicationManager.getApplication().runWriteAction(() -> {
      changed[0] = myContentManipulator.handleContentChange(content, replacement);
    });
    assertNotNull(changed[0]);
    dump(changed[0]);
    assertEquals(replaceEOLs(expected.getText()), replaceEOLs(changed[0].getText()));
  }

  private List<String> removeSuffix(List<String> input, String suffix) {
    final ArrayList<String> result = new ArrayList<>();
    for (String s : input) {
      if (s.endsWith(suffix)) {
        s = s.substring(0, s.length() - suffix.length());
      }
      result.add(s);
    }
    return result;
  }

  protected HCLHeredocContent content(String... lines) {
    return content(true, lines);
  }

  protected HCLHeredocContent content(boolean appendNewLines, String... lines) {
    return myElementGenerator.createHeredocContent(Arrays.asList(lines), appendNewLines, false, 0);
  }
}
