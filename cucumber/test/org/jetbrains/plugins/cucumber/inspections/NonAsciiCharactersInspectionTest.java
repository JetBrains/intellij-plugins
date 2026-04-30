package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.NonAsciiCharactersInspection;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

public class NonAsciiCharactersInspectionTest extends BasePlatformTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(NonAsciiCharactersInspection.class);
  }

  public void testNoWarningForNonAsciiCharacters() {
    myFixture.configureByText(GherkinFileType.INSTANCE, """
      Feature: Sample feature
      
        Scenario Outline: Non-ASCII character param "<param>"
          Given A list of values
          Examples:
            | ü |
            | ß |
            | ö |
            | Ж |
            | Г |
      """);

    myFixture.checkHighlighting();
  }
}
