package org.jetbrains.plugins.cucumber;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.jetbrains.plugins.cucumber.CucumberUtil.STANDARD_PARAMETER_TYPES;
import static org.jetbrains.plugins.cucumber.CucumberUtil.buildRegexpFromCucumberExpression;
import static org.junit.Assert.assertEquals;

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
    assertEquals("([+-]?\\d+) cucumbers", buildRegexpFromCucumberExpression("{int} cucumbers", STANDARD_PARAMETER_TYPES));
    assertEquals("([-+]?\\d*\\.?\\d+) cucumbers", buildRegexpFromCucumberExpression("{float} cucumbers", STANDARD_PARAMETER_TYPES));
    assertEquals("provided ([^\\s]+)", buildRegexpFromCucumberExpression("provided {word}", STANDARD_PARAMETER_TYPES));
    assertEquals("provided (?:(?:\"([^\"]*)\")|(:?'([^']*)'))",
                 buildRegexpFromCucumberExpression("provided {string}", STANDARD_PARAMETER_TYPES));
  }
}
