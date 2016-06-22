package org.jetbrains.plugins.cucumber.psi;

import com.intellij.testFramework.ParsingTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;

/**
 * @author yole
 */
public class GherkinParserTest extends ParsingTestCase {
  @SuppressWarnings("JUnitTestCaseWithNonTrivialConstructors")
  public GherkinParserTest() {
    super("", "feature", new GherkinParserDefinition());
  }

  @NotNull
  @Override
  protected String getTestName(boolean lowercaseFirstLetter) {
    return super.getTestName(true);
  }

  protected String getTestDataPath() {
    return CucumberTestUtil.getTestDataPath() + "/parsing";
  }

  public void testSimple() throws Exception {
    doTest(true);
  }

  public void testMultiline_feature_description() throws Exception {
    doTest(true);
  }

  public void testBackground() throws Exception {
    doTest(true);
  }

  public void testMultiline_scenario_name() throws Exception {
    doTest(true);
  }

  public void testScenario_outline() throws Exception {
    doTest(true);
  }

  public void testScenario_outline_table() throws Exception {
    doTest(true);
  }

  public void testScenario_outline_table_with_tags() throws Exception {
    doTest(true);
  }

  public void testNot_a_step() throws Exception {
    doTest(true);
  }

  public void testMultiline_args() throws Exception {
    doTest(true);
  }

  public void testNo_steps() throws Exception {
    doTest(true);
  }

  public void testTags() throws Exception {
    doTest(true);
  }

  public void testPystring() throws Exception {
    doTest(true);
  }

  public void testWithout_feature_keyword() throws Exception {
    doTest(true);
  }

  public void testStep_param() throws Exception {
    doTest(true);
  }

  public void testRuby8793() throws Exception {
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
}


