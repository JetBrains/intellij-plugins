package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.util.ArrayUtil;
import org.jetbrains.plugins.cucumber.psi.refactoring.rename.GherkinStepRenameProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CucumberRenameRegexPreparationTest {
  @Test
  public void testClass() {
    doTest("test[abc]test", "test([abc])test", "test", "test");
    doTest("[abc]test", "([abc])test", "", "test");
    doTest("test[abc]", "test([abc])", "test", "");
    doTest("test([abc])test", "test((?:[abc]))test", "test", "test");
  }

  @Test
  public void testGroup() {
    doTest("test(abcd|efgh)test", "test((?:abcd|efgh))test", "test", "test");
  }

  @Test
  public void testNonCapturingGroup() {
    doTest("test(?:abcd)test", "test((?:abcd))test", "test", "test");
  }

  @Test
  public void testNestedParenthesis() {
    doTest("c(a(.*)and(?:a))b", "c((?:a(?:.*)and(?:a)))b", "c", "b");
  }

  @Test
  public void testClassInsideGroup() {
    doTest("a([bcd]*)", "a((?:[bcd]*))", "a", "");
  }

  @Test
  public void testNeighbourGroups() {
    doTest("a(.*)(.*)", "a((?:.*)(?:.*))", "a", "");
  }

  private static void doTest(String source, String expected, String... sentences) {
    List<String> result = GherkinStepRenameProcessor.prepareRegexAndGetStaticTexts(source);
    Assert.assertEquals(sentences.length, result.size() - 1);

    String preparedRegex = result.remove(0);
    Assert.assertEquals(expected, preparedRegex);

    Assert.assertArrayEquals(sentences, ArrayUtil.toStringArray(result));
  }
}
