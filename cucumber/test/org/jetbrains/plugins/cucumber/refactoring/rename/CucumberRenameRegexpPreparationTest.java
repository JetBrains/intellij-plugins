package org.jetbrains.plugins.cucumber.refactoring.rename;

import org.jetbrains.plugins.cucumber.psi.refactoring.rename.CucumberStepRenameProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CucumberRenameRegexpPreparationTest {
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
    List<String> result = CucumberStepRenameProcessor.prepareRegexAndGetStaticTexts(source);
    Assert.assertEquals(expected, result.get(0));

    if (sentences.length > result.size() - 1) {
      Assert.fail();
    }

    for (int i = 0; i < sentences.length; i++) {
      String expectedSentence = sentences[i];
      String actualSentence = result.get(i + 1);
      Assert.assertEquals(expectedSentence, actualSentence);
    }
  }
}
