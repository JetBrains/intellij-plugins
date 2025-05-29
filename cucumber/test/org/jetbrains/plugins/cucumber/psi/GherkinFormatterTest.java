// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.formatter.FormatterTestCase;


public class GherkinFormatterTest extends FormatterTestCase {
  public void testComments() {
    doTextTest("""
                 Feature: test
                   #comment

                   Scenario: test
                     Given test

                   #comment
                   Scenario: user writes a flush left comment and reformats
                     Given test
                         #test
                       | a | b |
                       | a | b |
                       #comment
                     Then test2""");
  }

  public void testPystring() {
    doTextTest(
      """
        Feature: Resolve from property key to value

          Background:
            Given a Rails project
            And a Rails directory structure

          Scenario: Resolve with to property with single declaration
            Given a Rails locale file "en" with text
            ""\"
            en:
              hello: "Hello"
              hello_world: Hello world
              hello_rubymine: Hello RubyMine
            ""\"
            And I open a Rails controller "foo" with text
            ""\"
            class FooController < ApplicationController
              def index
                flash[:notice] = t(:"hello_world")
              end
            end
            ""\"
            And I put the caret at hello_world
            Then reference should resolve to "Hello world" in "en.yml\""""
    );
  }

  public void testReadonlyBlock() {
    doTextTest(
      """
        @javascript
        Feature: Autocompletion

          Scenario:
            Given a blog post named "Random" with:
                ""\"
                Some Title, Eh?
                ===============
                Here is the first paragraph of my blog post.
                Lorem ipsum dolor sit amet, consectetur adipiscing
                elit.
                ""\""""
    );
  }

  public void testSimple() {
    doTextTest(
      """
        Feature: Search
        In order to learn more
        As an information seeker
        I want to find more information
        Scenario: Find what I'm looking for
        Given I am on the Google search page
        When I search for "rspec"
        Then I should see a link to http://rspec.info/
        Scenario Outline: Add two numbers
        When I press add
        Then the result should be 1""",

      """
        Feature: Search
          In order to learn more
          As an information seeker
          I want to find more information

          Scenario: Find what I'm looking for
            Given I am on the Google search page
            When I search for "rspec"
            Then I should see a link to http://rspec.info/

          Scenario Outline: Add two numbers
            When I press add
            Then the result should be 1"""
    );
  }

  public void testStepTableArg() {
    doTextTest(
      """
        Feature: Outline Sample

          Scenario: I have tables
            Given step with table
        |||
            |  state   | other_state |
            | missing |  passing|
            | passing| passing |
        | failing | passing |
        |||
            And other scenario
              | 1     | Description |
              |                                  | This is a description of a thing |
              |                                  | A thing with no name             |
        """,

      """
        Feature: Outline Sample

          Scenario: I have tables
            Given step with table
              |         |             |
              | state   | other_state |
              | missing | passing     |
              | passing | passing     |
              | failing | passing     |
              |         |             |
            And other scenario
              | 1 | Description                      |
              |   | This is a description of a thing |
              |   | A thing with no name             |
        """
    );
  }

  public void testTable() {
    doTextTest(
      """
        Feature: Outline Sample

          Scenario: I have no steps

          Scenario Outline: Test state
            Given <state> without a table
            Given <other_state> without a table
          Examples: Rainbow colours
            |  state   | other_state |
            | missing |  passing|
            | passing| passing |
        | failing | passing |
        Examples:Only passing
        |  state   | other_state |
        | passing  | passing |
        |||""",

      """
        Feature: Outline Sample

          Scenario: I have no steps

          Scenario Outline: Test state
            Given <state> without a table
            Given <other_state> without a table
            Examples: Rainbow colours
              | state   | other_state |
              | missing | passing     |
              | passing | passing     |
              | failing | passing     |
            Examples:Only passing
              | state   | other_state |
              | passing | passing     |
              |         |             |"""
    );
  }

  public void testExamplesWithTag() {
    doTextTest(
      """
        @javascript
        Feature: Autocompletion

          @tag1
          Scenario Outline
            Given some <value>

            @tag2
            Examples:
              | value |"""
    );
  }

  // Test for IDEA-278652
  public void testTagsForScenarioOutline() {
    doTextTest("""
                 Feature: Withdraw money (table-based)
                 
                   Some description
                 
                   @allure.id:2048
                   @moved_to_pcwf
                   @allure.label.epic:MP-11487
                   @allure.label.testType:positive
                   @allure.label.service:pc
                   Scenario Outline: Withdrawing money subtracts correct amount (start: <start>)
                     Given I have <start> EUR on my account
                     When I withdraw <subtract_amount> EUR
                     Then My account has <end> EUR left
                 
                     Examples:
                       | start | subtract_amount | end |
                       | 100   | 25              | 75  |
                       | 150   | 50              | 100 |
                 """);
  }

  public void testTagsForScenario() {
    doTextTest("""
                 Feature: Withdraw money
                   As a customer
                   In order to avoid going to the bank
                   I want to withdraw money from an ATM
                 
                   Scenario: Withdraw less money than the account has
                     Given I have 200 EUR on my account
                     When I withdraw 100 EUR
                     Then I get 100 EUR from the ATM
                     And My account has 100 EUR left
                     And I am happy
                 
                   @allure.id:2048
                   @moved_to_pcwf
                   @allure.label.epic:MP-11487
                   @allure.label.testType:positive
                   @allure.label.service:pc
                   Scenario: Withdraw more money than the account has
                     Given I have 50 EUR on my account
                     When I withdraw 100 EUR
                     Then I get 0 EUR from the ATM
                     And My account has 500 EUR left
                     And error message about the lack of money is displayed
                     And I am angry
                 
                   Scenario: Withdraw more money than the account has (copy)
                     Given I have 60 EUR on my account
                     When I withdraw 100 EUR
                     Then I get 0 EUR from the ATM
                     And My account has 60 EUR left
                     And error message about the lack of money is displayed
                     And I am angry
                 
                   Scenario: Withdraw negative amount from the account
                     Given I have 70 EUR on my account
                     When I withdraw -1 EUR
                     Then I get 0 EUR from the ATM
                     And My account has 70 EUR left
                     And error message about incorrect amount is displayed
                     And I am angry
                 """);
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
