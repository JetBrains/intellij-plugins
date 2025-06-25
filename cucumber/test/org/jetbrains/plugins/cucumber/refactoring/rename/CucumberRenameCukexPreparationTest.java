package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.util.ArrayUtil;
import org.jetbrains.plugins.cucumber.psi.refactoring.rename.GherkinStepRenameProcessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class CucumberRenameCukexPreparationTest {
  @Test
  public void testSimpleText() {
    doTest("I am happy", "I am happy", "I am happy");
  }

  @Test
  public void testIntParameter() {
    doTest("I have {int} apples", "I have (-?\\d+) apples", "I have ", " apples");
    doTest(
      "I have {int} apples, I repeat, {int} apples",
      "I have (-?\\d+) apples, I repeat, (-?\\d+) apples",
      "I have ", " apples, I repeat, ", " apples"
    );
    doTest("{int} apples", "(-?\\d+) apples", "", " apples");
    doTest("apples {int}", "apples (-?\\d+)", "apples ", "");
  }

  @Test
  public void testStringParameter() {
    doTest("I say {string}", "I say (\"(?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'(?:[^'\\\\]*(?:\\\\.[^'\\\\]*)*)')", "I say ", "");
    doTest("{string} is good", "(\"(?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'(?:[^'\\\\]*(?:\\\\.[^'\\\\]*)*)') is good", "", " is good");
    doTest(
      "I say {string} loudly",
      "I say (\"(?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'(?:[^'\\\\]*(?:\\\\.[^'\\\\]*)*)') loudly",
      "I say ", " loudly"
    );
  }

  @Test
  public void testWordParameter() {
    doTest("my {word} name", "my ([^\\s]+) name", "my ", " name");
    doTest("{word} is fine", "([^\\s]+) is fine", "", " is fine");
    doTest("call me {word}", "call me ([^\\s]+)", "call me ", "");
  }

  @Test
  public void testFloatParameter() {
    doTest("price is {float}", "price is (-?\\d*[.,]?\\d+)", "price is ", "");
    doTest("{float} dollars", "(-?\\d*[.,]?\\d+) dollars", "", " dollars");
  }

  @Test
  public void testMultipleParameters() {
    doTest("{int} {string}", "(-?\\d+) (\"(?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'(?:[^'\\\\]*(?:\\\\.[^'\\\\]*)*)')", "", " ", "");
    doTest("I have {int} {word} items", "I have (-?\\d+) ([^\\s]+) items", "I have ", " ", " items");
    doTest("{string} costs {float} dollars",
           "(\"(?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'(?:[^'\\\\]*(?:\\\\.[^'\\\\]*)*)') costs (-?\\d*[.,]?\\d+) dollars",
           "", " costs ", " dollars"
    );
  }

  @Test
  public void testAlternativeText() {
    doTest("I am happy/sad today", "I am (happy|sad) today", "I am ", " today");
    doTest("good/bad weather", "(good|bad) weather", "", " weather");
    doTest("morning/afternoon/evening", "(morning|afternoon|evening)", "", "");
  }

  @Test
  public void testOptionalText() {
    doTest("I (may) see", "I (may)? see", "I ", " see");
    doTest("(definitely) working", "(definitely)? working", "", " working");
    doTest("going (home), now!", "going (home)?, now!", "going ", ", now!");
  }

  @Test
  public void testSmoke() {
    doTest("I may/might see {int} cucumber(s)", "I (may|might) see (-?\\d+) cucumber(s)?", "I ", " see ", " cucumber", "");
  }

  private static void doTest(String sourceCukex, String expectedPattern, String... expectedStaticTexts) {
    String preparedRegex = GherkinStepRenameProcessor.prepareRegexFromCukex(sourceCukex);
    Assert.assertEquals(expectedPattern, preparedRegex);

    List<String> actualStaticTexts = GherkinStepRenameProcessor.getStaticTextsFromCukex(sourceCukex);
    Assert.assertArrayEquals(expectedStaticTexts, ArrayUtil.toStringArray(actualStaticTexts));
  }
}
