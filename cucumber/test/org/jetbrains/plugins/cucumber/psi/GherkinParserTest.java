// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.project.Project;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.plugins.cucumber.CucumberTestUtil;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;

@TestApplication
public class GherkinParserTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  private final TestFixture<ParsingTestFixture> parsingFixture =
    parsingFixture(projectFixture, CucumberTestUtil.getTestDataPath() + "/parsing/", "feature", true, new GherkinParserDefinition());

  @Test
  public void testSimple() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testMultiline_feature_description() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testBackground() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testMultiline_scenario_name() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testScenario_outline() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testScenario_outline_table() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testScenario_outline_table_with_tags() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testNot_a_step() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testMultiline_args() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testNo_steps() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testTags() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testPystring() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testWithout_feature_keyword() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testStep_param() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testRuby8793() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testRuby14051() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testBackground_after_scenario() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testFeatures() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testScenario_outline_param() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testScenario_with_examples() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testTag_before_examples() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testRule() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testIdea375449() {
    parsingFixture.get().doTest(true);
  }
}


