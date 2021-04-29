// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.formatter.FormatterTestCase;


public class GherkinFormatterTest extends FormatterTestCase {
  public void testComments() {
    doTextTest("Feature: test\n" +
               "  #comment\n" +
               "\n" +
               "  Scenario: test\n" +
               "    Given test\n" +
               "\n" +
               "  #comment\n" +
               "  Scenario: user writes a flush left comment and reformats\n" +
               "    Given test\n" +
               "        #test\n" +
               "      | a | b |\n" +
               "      | a | b |\n" +
               "      #comment\n" +
               "    Then test2");
  }

  public void testPystring() {
    doTextTest(
      "Feature: Resolve from property key to value\n" +
      "\n" +
      "  Background:\n" +
      "    Given a Rails project\n" +
      "    And a Rails directory structure\n" +
      "\n" +
      "  Scenario: Resolve with to property with single declaration\n" +
      "    Given a Rails locale file \"en\" with text\n" +
      "    \"\"\"\n" +
      "    en:\n" +
      "      hello: \"Hello\"\n" +
      "      hello_world: Hello world\n" +
      "      hello_rubymine: Hello RubyMine\n" +
      "    \"\"\"\n" +
      "    And I open a Rails controller \"foo\" with text\n" +
      "    \"\"\"\n" +
      "    class FooController < ApplicationController\n" +
      "      def index\n" +
      "        flash[:notice] = t(:\"hello_world\")\n" +
      "      end\n" +
      "    end\n" +
      "    \"\"\"\n" +
      "    And I put the caret at hello_world\n" +
      "    Then reference should resolve to \"Hello world\" in \"en.yml\""
    );
  }

  public void testReadonlyBlock() {
    doTextTest(
      "@javascript\n" +
      "Feature: Autocompletion\n" +
      "\n" +
      "  Scenario:\n" +
      "    Given a blog post named \"Random\" with:\n" +
      "        \"\"\"\n" +
      "        Some Title, Eh?\n" +
      "        ===============\n" +
      "        Here is the first paragraph of my blog post.\n" +
      "        Lorem ipsum dolor sit amet, consectetur adipiscing\n" +
      "        elit.\n" +
      "        \"\"\""
    );
  }

  public void testSimple() {
    doTextTest(
      "Feature: Search\n" +
      "In order to learn more\n" +
      "As an information seeker\n" +
      "I want to find more information\n" +
      "Scenario: Find what I'm looking for\n" +
      "Given I am on the Google search page\n" +
      "When I search for \"rspec\"\n" +
      "Then I should see a link to http://rspec.info/\n" +
      "Scenario Outline: Add two numbers\n" +
      "When I press add\n" +
      "Then the result should be 1",

      "Feature: Search\n" +
      "  In order to learn more\n" +
      "  As an information seeker\n" +
      "  I want to find more information\n" +
      "\n" +
      "  Scenario: Find what I'm looking for\n" +
      "    Given I am on the Google search page\n" +
      "    When I search for \"rspec\"\n" +
      "    Then I should see a link to http://rspec.info/\n" +
      "\n" +
      "  Scenario Outline: Add two numbers\n" +
      "    When I press add\n" +
      "    Then the result should be 1"
    );
  }

  public void testStepTableArg() {
    doTextTest(
      "Feature: Outline Sample\n" +
      "\n" +
      "  Scenario: I have tables\n" +
      "    Given step with table\n" +
      "|||\n" +
      "    |  state   | other_state |\n" +
      "    | missing |  passing|\n" +
      "    | passing| passing |\n" +
      "| failing | passing |\n" +
      "|||\n" +
      "    And other scenario\n" +
      "      | 1     | Description |\n" +
      "      |                                  | This is a description of a thing |\n" +
      "      |                                  | A thing with no name             |\n",

      "Feature: Outline Sample\n" +
      "\n" +
      "  Scenario: I have tables\n" +
      "    Given step with table\n" +
      "      |         |             |\n" +
      "      | state   | other_state |\n" +
      "      | missing | passing     |\n" +
      "      | passing | passing     |\n" +
      "      | failing | passing     |\n" +
      "      |         |             |\n" +
      "    And other scenario\n" +
      "      | 1 | Description                      |\n" +
      "      |   | This is a description of a thing |\n" +
      "      |   | A thing with no name             |\n"
    );
  }

  public void testTable() {
    doTextTest(
      "Feature: Outline Sample\n" +
      "\n" +
      "  Scenario: I have no steps\n" +
      "\n" +
      "  Scenario Outline: Test state\n" +
      "    Given <state> without a table\n" +
      "    Given <other_state> without a table\n" +
      "  Examples: Rainbow colours\n" +
      "    |  state   | other_state |\n" +
      "    | missing |  passing|\n" +
      "    | passing| passing |\n" +
      "| failing | passing |\n" +
      "Examples:Only passing\n" +
      "|  state   | other_state |\n" +
      "| passing  | passing |\n" +
      "|||",

      "Feature: Outline Sample\n" +
      "\n" +
      "  Scenario: I have no steps\n" +
      "\n" +
      "  Scenario Outline: Test state\n" +
      "    Given <state> without a table\n" +
      "    Given <other_state> without a table\n" +
      "    Examples: Rainbow colours\n" +
      "      | state   | other_state |\n" +
      "      | missing | passing     |\n" +
      "      | passing | passing     |\n" +
      "      | failing | passing     |\n" +
      "    Examples:Only passing\n" +
      "      | state   | other_state |\n" +
      "      | passing | passing     |\n" +
      "      |         |             |"
    );
  }

  public void testExamplesWithTag() {
    doTextTest(
      "@javascript\n" +
      "Feature: Autocompletion\n" +
      "\n" +
      "  @tag1\n" +
      "  Scenario Outline\n" +
      "    Given some <value>\n" +
      "\n" +
      "    @tag2\n" +
      "    Examples:\n" +
      "      | value |"
    );
  }

  protected void doTextTest(String text) {
    doTextTest(text, text);
  }

  @Override
  protected String getBasePath() {
    return "";
  }

  @Override
  protected String getFileExtension() {
    return GherkinFileType.INSTANCE.getDefaultExtension();
  }
}
