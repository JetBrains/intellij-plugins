package org.jetbrains.plugins.cucumber;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.jetbrains.plugins.cucumber.CucumberUtil.buildRegexpFromCucumberExpression;
import static org.jetbrains.plugins.cucumber.CucumberUtil.isCucumberExpression;
import static org.jetbrains.plugins.cucumber.CucumberUtil.replaceNotNecessaryTextTemplateByRegexp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class CucumberUtilTest {
  @Test
  public void testConvertOutlineStepName() {
    Map<String, String> outlineTableMap = new HashMap<>();
    outlineTableMap.put("name", "Foo");
    outlineTableMap.put("count", "10");

    OutlineStepSubstitution substitution =
      CucumberUtil.substituteTableReferences("Project with name: <name> and <count> participants", outlineTableMap);

    assertEquals("Project with name: Foo and 10 participants", substitution.getSubstitution());
    assertEquals(19, substitution.getOffsetInOutlineStep(19));
    assertEquals(23, substitution.getOffsetInOutlineStep(20));
    assertEquals(30, substitution.getOffsetInOutlineStep(27));
    assertEquals(36, substitution.getOffsetInOutlineStep(28));
  }

  @Test
  public void testBuildRegexpFromCucumberExpression() {
    assertEquals("(-?\\d+) cucumbers", buildRegexpFromCucumberExpression("{int} cucumbers", MapParameterTypeManager.DEFAULT));
    assertEquals("(-?\\d*[.,]?\\d+) cucumbers", buildRegexpFromCucumberExpression("{float} cucumbers", MapParameterTypeManager.DEFAULT));
    assertEquals("provided ([^\\s]+)", buildRegexpFromCucumberExpression("provided {word}", MapParameterTypeManager.DEFAULT));
    assertEquals("provided (\"(?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|'(?:[^'\\\\]*(?:\\\\.[^'\\\\]*)*)')",
                 buildRegexpFromCucumberExpression("provided {string}", MapParameterTypeManager.DEFAULT));
  }

  @Test
  public void testIsCucumberExpression() {
    assertTrue(isCucumberExpression("def {int}"));
    assertFalse(isCucumberExpression("def {int"));
    assertFalse(isCucumberExpression("def \\d{1}"));
    assertFalse(isCucumberExpression("def \\d{1,2}"));
    assertFalse(isCucumberExpression("text"));
    assertFalse(isCucumberExpression("text (\\d)"));
  }

  @Test
  public void testReplaceNotNecessaryTextTemplateByRegexp() {
    String actual = replaceNotNecessaryTextTemplateByRegexp("I have {short}  cucumber(s) in my belly");
    assertEquals("I have {short}  cucumber(?:s)? in my belly", actual);
  }
}
