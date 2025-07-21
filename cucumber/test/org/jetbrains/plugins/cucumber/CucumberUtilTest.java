package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.plugins.cucumber.psi.refactoring.rename.GherkinStepRenameProcessor;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.jetbrains.plugins.cucumber.CucumberUtil.*;
import static org.junit.Assert.*;

public class CucumberUtilTest {
  @Test
  public void testIsCucumberExpression() {
    assertTrue(isCucumberExpression("strings are cukexp by default"));
    assertFalse(isCucumberExpression("^definitely a regexp$"));
    assertFalse(isCucumberExpression("^definitely a regexp"));
    assertFalse(isCucumberExpression("definitely a regexp$"));
    assertFalse(isCucumberExpression("/surely a regexp/"));
    assertTrue(isCucumberExpression("this look(s) like a cukexp"));
    assertTrue(isCucumberExpression("\\(text)"));
  }

  @Test
  public void testCukexHighlightRanges() {
    // Should correctly handle a single parameter
    String expression1 = "I have {int} cucumber in my belly!";
    List<TextRange> expected1 = List.of(new TextRange(7, 12));
    assertEquals(expected1, getCukexHighlightRanges(expression1));

    // Should correctly handle multiple parameters
    String expression2 = "There are {int} cukes and {string} flights";
    List<TextRange> expected2 = List.of(new TextRange(10, 15), new TextRange(26, 34));
    assertEquals(expected2, getCukexHighlightRanges(expression2));

    // Should correctly handle escaped opening braces
    String expression3 = "I have {int} \\{string}s in my belly!";
    List<TextRange> expected3 = List.of(new TextRange(7, 12));
    assertEquals(expected3, getCukexHighlightRanges(expression3));

    // Should correctly handle a single parameter and optional text.
     String expression4 = "I have {int} cucumber(s) in my belly!";
     List<TextRange> expected4 = List.of(new TextRange(7, 12));
     assertEquals(expected4, getCukexHighlightRanges(expression4));
  }

  @Test
  public void testCukexRanges() {
    // Should correctly handle simple text
    String expression0 = "I am happy";
    List<TextRange> expected0 = List.of();
    assertEquals(expected0, getCukexRanges(expression0));
    assertEquals(List.of("I am happy"), textRangesOutsideToSubstrings(expression0, expected0));
    
    // Should correctly handle a single parameter
    String expression1 = "I have {int} cucumber in my belly!";
    List<TextRange> expected1 = List.of(new TextRange(7, 12));
    assertEquals(expected1, getCukexRanges(expression1));
    assertEquals(List.of("I have ", " cucumber in my belly!"), textRangesOutsideToSubstrings(expression1, expected1));

    // Should correctly handle multiple parameters
    String expression2 = "There are {int} cukes and {string} flights";
    List<TextRange> expected2 = List.of(new TextRange(10, 15), new TextRange(26, 34));
    assertEquals(expected2, getCukexRanges(expression2));
    assertEquals(List.of("There are ", " cukes and ", " flights"), textRangesOutsideToSubstrings(expression2, expected2));

    // Should correctly handle escaped opening braces
    String expression3 = "I have {int} \\{string} in my belly!";
    List<TextRange> expected3 = List.of(new TextRange(7, 12));
    assertEquals(expected3, getCukexRanges(expression3));
    assertEquals(List.of("I have ", " \\{string} in my belly!"), textRangesOutsideToSubstrings(expression3, expected3));

    // Should correctly handle a single parameter and optional text
    String expression4 = "I have {int} cucumber(s) in my belly!";
    List<TextRange> expected4 = List.of(new TextRange(7, 12), new TextRange(21, 24));
    assertEquals(expected4, getCukexRanges(expression4));
    assertEquals(List.of("I have ", " cucumber", " in my belly!"), textRangesOutsideToSubstrings(expression4, expected4));

    // Should correctly handle a single parameter and alternative text
    String expression5 = "I have one/few/many cucumber(s) in my be||y";
    List<TextRange> expected5 = List.of(new TextRange(7, 19), new TextRange(28, 31));
    assertEquals(expected5, getCukexRanges(expression5));
    assertEquals(List.of("I have ", " cucumber", " in my be||y"), textRangesOutsideToSubstrings(expression5, expected5));
  }

  @Test
  public void testConvertOutlineStepName() {
    Map<String, String> outlineTableMap = new HashMap<>();
    outlineTableMap.put("name", "Foo");
    outlineTableMap.put("count", "10");

    OutlineStepSubstitution substitution = substituteTableReferences("Project with name: <name> and <count> participants", outlineTableMap);

    assertEquals("Project with name: Foo and 10 participants", substitution.getSubstitution());
    assertEquals(19, substitution.getOffsetInOutlineStep(19));
    assertEquals(23, substitution.getOffsetInOutlineStep(20));
    assertEquals(30, substitution.getOffsetInOutlineStep(27));
    assertEquals(36, substitution.getOffsetInOutlineStep(28));
  }

  @Test
  public void testReplaceOptionalTextWithRegex() {
    String actual0 = replaceOptionalTextWithRegex("I have {int} cucumber(s) in my belly");
    assertEquals("I have {int} cucumber(s)? in my belly", actual0);

    String actual1 = replaceOptionalTextWithRegex("I have {short}  cucumber(s) in my belly");
    assertEquals("I have {short}  cucumber(s)? in my belly", actual1);

    String actual2 = replaceOptionalTextWithRegex("I (would like) {int} cucumber(s) (please)");
    assertEquals("I (would like)? {int} cucumber(s)? (please)?", actual2);

    String actual3 = replaceOptionalTextWithRegex("I have (one|many) cucumber(s) (in (my|your) belly)");
    assertEquals("I have (one|many)? cucumber(s)? (in (my|your)? belly)", actual3);
  }

  @Test
  public void testReplaceAlternativeTextWithRegex() {
    String actual1 = replaceAlternativeTextWithRegex("I have one/few/many cucumber(s) in my be||y");
    assertEquals("I have (one|few|many) cucumber(s) in my be\\|\\|y", actual1);

    String actual2 = replaceAlternativeTextWithRegex("I print a word(s) red/blue using slash and a pipe |");
    assertEquals("I print a word(s) (red|blue) using slash and a pipe \\|", actual2);
  }

  @Test
  public void testBuildRegexpFromCucumberExpression() {
    String actual1 = buildRegexpFromCucumberExpression("{int} cucumbers");
    assertEquals("^(-?\\d+) cucumbers$", actual1);

    String actual2 = buildRegexpFromCucumberExpression("{float} cucumbers");
    assertEquals("^(-?\\d*[.,]?\\d+) cucumbers$", actual2);

    String actual3 = buildRegexpFromCucumberExpression("provided {int} cucumbers");
    assertEquals("^provided (-?\\d+) cucumbers$", actual3);

    String actual4 = buildRegexpFromCucumberExpression("provided {word}");
    assertEquals("^provided ([^\\s]+)$", actual4);

    String actual5 = buildRegexpFromCucumberExpression("provided {string}");
    assertEquals("^provided (\"(?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'(?:[^'\\\\]*(?:\\\\.[^'\\\\]*)*)')$", actual5);

    String actual6 = buildRegexpFromCucumberExpression("I have one/few/many cucumber(s) in my be||y");
    assertEquals("^I have (one|few|many) cucumber(s)? in my be\\\\|\\\\|y$", actual6);

    // FIXME: Does not support escaping for now
    // String actual7 = buildRegexpFromCucumberExpression("I have \\{int} {int} feeling(s)");
    // assertEquals("^I have \\{int} (-?\\d+) feeling(s)$", actual7);
  }

  @Test
  public void testGetTheBiggestWordToSearchByIndex() {
    String actual = getTheBiggestWordToSearchByIndex("I have cucumber(s) text");
    assertEquals("have", actual);

    actual = getTheBiggestWordToSearchByIndex("I have cucumber/gherkin value");
    assertEquals("value", actual);

    actual = getTheBiggestWordToSearchByIndex("I have cucumber\\d");
    assertEquals("have", actual);
  }

  @Test
  public void testGetNewStepName_good_0() {
    String oldStepDef = "I have few/many feeling(s)";
    String oldStepName = "I have few feelings";

    String oldStepDefRegex = GherkinStepRenameProcessor.prepareRegexFromCukex(oldStepDef);
    assertEquals("I have (few|many) feeling(s)?", oldStepDefRegex);

    String newName = GherkinStepRenameProcessor.getNewStepName(
      oldStepName,
      Pattern.compile(oldStepDefRegex),
      /*newStaticTexts*/ List.of("I really do have ", " feeling", ", I swear")
    );

    assertEquals("I really do have few feelings, I swear", newName);
  }

  @Test
  public void testGetNewStepName_good_1() {
    String oldStepDef = "I have {int} feeling(s)";
    String oldStepName = "I have 1 feeling";

    String oldStepDefRegex = GherkinStepRenameProcessor.prepareRegexFromCukex(oldStepDef);
    assertEquals("I have (-?\\d+) feeling(s)?", oldStepDefRegex);

    String newName = GherkinStepRenameProcessor.getNewStepName(
      oldStepName,
      Pattern.compile(oldStepDefRegex),
      /*newStaticTexts*/ List.of("I really do have ", " feeling")
    );

    assertEquals("I really do have 1 feeling", newName);
  }

  @Test
  public void testGetNewStepName_good_2() {
    String oldStepDef = "I have {int} feeling(s)";
    String oldStepName = "I have 7 feelings";

    String oldStepDefRegex = GherkinStepRenameProcessor.prepareRegexFromCukex(oldStepDef);
    assertEquals("I have (-?\\d+) feeling(s)?", oldStepDefRegex);

    String newName = GherkinStepRenameProcessor.getNewStepName(
      oldStepName,
      Pattern.compile(oldStepDefRegex),
      /*newStaticTexts*/ List.of("I really do have ", " feeling")
    );

    assertEquals("I really do have 7 feelings", newName);
  }

  @Ignore("Does not support escaping for now")
  @Test
  public void testGetNewStepName_escaped() {
    String oldStepDef = "I have \\{int} {int} feeling(s)";
    String oldStepName = "I have 2 feelings";

    String oldStepDefRegex = GherkinStepRenameProcessor.prepareRegexFromCukex(oldStepDef);
    assertEquals("I have \\\\{int} (-?\\d+) feeling(s)?", oldStepDefRegex);

    String newName = GherkinStepRenameProcessor.getNewStepName(
      oldStepName,
      Pattern.compile(oldStepDefRegex),
      /*newStaticTexts*/ List.of("I really do have ", " feeling")
    );

    assertEquals("I really do have 7 feelings", newName);
  }
}
