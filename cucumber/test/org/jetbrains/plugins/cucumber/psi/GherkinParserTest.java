package org.jetbrains.plugins.cucumber.psi;

import com.intellij.testFramework.ParsingTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;

/**
 * @author yole
 */
public class GherkinParserTest extends ParsingTestCase {
  public GherkinParserTest() {
    super("", "feature", new GherkinParserDefinition());
  }

  @NotNull
  @Override
  protected String getTestName(boolean lowercaseFirstLetter) {
    return super.getTestName(true);
  }

  @Override
  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + "/parsing";
  }

  public void testSimple() {
    doTest(true);
  }

  public void testMultiline_feature_description() {
    doTest(true);
  }

  public void testBackground() {
    doTest(true);
  }

  public void testMultiline_scenario_name() {
    doTest(true);
  }

  public void testScenario_outline() {
    doTest(true);
  }

  public void testScenario_outline_table() {
    doTest(true);
  }

  public void testScenario_outline_table_with_tags() {
    doTest(true);
  }

  public void testNot_a_step() {
    doTest(true);
  }

  public void testMultiline_args() {
    doTest(true);
  }

  public void testNo_steps() {
    doTest(true);
  }

  public void testTags() {
    doTest(true);
  }

  public void testPystring() {
    doTest(true);
  }

  public void testWithout_feature_keyword() {
    doTest(true);
  }

  public void testStep_param() {
    doTest(true);
  }

  public void testRuby8793() {
    doTest(true);
  }

  public void testRuby14051() {
    doTest(true);
  }

  public void testBackground_after_scenario() {
    doTest(true);
  }

  public void testFeatures() {
    doTest(true);
  }

  public void testScenario_outline_param() {
    doTest(true);
  }

  public void testScenario_with_examples() {
    doTest(true);
  }
}


