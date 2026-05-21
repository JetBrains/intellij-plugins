// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.NonAsciiCharactersInspection;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

public class CucumberTableInspectionTest extends BasePlatformTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(CucumberTableInspection.class);
  }

  public void testColumnUnused() {
    myFixture.configureByText(GherkinFileType.INSTANCE, """
      Feature: Sample feature
      
        Scenario Outline: Decodes timestamp (<Timestamp>) + Description
          Given a timestamp of <Timestamp> and description
          When the uplink decoder is called
          Then it should be decoded
      
          Examples:
            | Timestamp  | <warning descr="Unused table column">Description</warning>          |
            | 0          | <warning descr="Unused table column">Minimum</warning>              |
            | 0xFF       | <warning descr="Unused table column">Maximum 8 bit value</warning>  |
            | 0xFFFF     | <warning descr="Unused table column">Maximum 16 bit value</warning> |
            | 0xFFFFFF   | <warning descr="Unused table column">Maximum 24 bit value</warning> |
            | 0xFFFFFFFF | <warning descr="Unused table column">Maximum 32 bit value</warning> |
      """);

    myFixture.checkHighlighting();
  }

  public void testColumnUsed() {
    myFixture.configureByText(GherkinFileType.INSTANCE, """
      Feature: Sample feature
      
        Scenario Outline: Decodes with <Description> timestamp (<Timestamp>)
          Given a timestamp of <Timestamp> and description of <Description>
          When the uplink decoder is called
          Then it should be decoded
      
          Examples:
            | Timestamp  | Description          |
            | 0          | Minimum              |
            | 0xFF       | Maximum 8 bit value  |
            | 0xFFFF     | Maximum 16 bit value |
            | 0xFFFFFF   | Maximum 24 bit value |
            | 0xFFFFFFFF | Maximum 32 bit value |
      """);

    myFixture.checkHighlighting();
  }

  // Test for IDEA-261249
  public void testColumnOnlyUsedInScenarioOutline() {
    myFixture.configureByText(GherkinFileType.INSTANCE, """
      Feature: Sample feature
      
        Scenario Outline: Decodes with <Description> timestamp (<Timestamp>)
          Given a timestamp of <Timestamp>
          When the uplink decoder is called
          Then it should be decoded
      
          Examples:
            | Timestamp  | Description          |
            | 0          | Minimum              |
            | 0xFF       | Maximum 8 bit value  |
            | 0xFFFF     | Maximum 16 bit value |
            | 0xFFFFFF   | Maximum 24 bit value |
            | 0xFFFFFFFF | Maximum 32 bit value |
      """);

    myFixture.checkHighlighting();
  }

  // Test for IDEA-245889
  public void testParameterWrappedInDoubleCaret() {
    myFixture.configureByText(GherkinFileType.INSTANCE, """
      Feature: Sample feature
      
        Scenario Outline: Double caret test
          Given my another step definition with param "<<param>>"
          Examples:
            | param |
            | hello |
            | there |
      """);

    myFixture.checkHighlighting();
  }

  public void testOnlyHeaderRowCellsHaveNameIdentifiers() {
    // Only header row cells in Examples hold identifiers, other cells and all cells in data tables do not hold identifiers.
    // Enable NonAsciiCharacterInspection to detect which cells are treated as identifiers.
    myFixture.enableInspections(NonAsciiCharactersInspection.class);
    myFixture.configureByText(GherkinFileType.INSTANCE, """
      Feature: My feature
        Scenario Outline: My scenario
        Given following data table
            | name   | ü|
            | Aslak  | ü  |
        When I ask user <ü> for input
        Then something happens
          Examples:
            | <warning descr="Non-ASCII characters">ü</warning> |
            | Alice |
            | Bob |
            | ü |
      """);

    myFixture.checkHighlighting();
  }
}
