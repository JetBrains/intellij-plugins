// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model;

import junit.framework.TestCase;
import org.intellij.terraform.config.model.version.MalformedConstraintException;
import org.intellij.terraform.config.model.version.Version;
import org.intellij.terraform.config.model.version.VersionConstraint;
import org.junit.Assert;

public class VersionConstraintTest extends TestCase {
  public void testAnyVersionParsed() {
    assertNotNull(VersionConstraint.Companion.getAnyVersion());
  }

  public void testConstraintParsed() throws Exception {
    Object[][] params = {
        {">= 1.2", 1, false},
        {"1.0", 1, false},
        {">= 1.x", 0, true},
        {">= 1.2, < 1.0", 2, false},

        // Out of bounds
        {"11387778780781445675529500000000000000000", 0, true},
        {"~> 5.IntellijIdeaRulezzz", 0, true},
        {"~> 5.IntellijIdeaRulezzz ", 0, true},
        {"~> 5.0 ", 1, false},
    };
    for (Object[] param : params) {
      doConstraintParseTest((String) param[0], (int) param[1], (boolean) param[2]);
    }
  }

  private void doConstraintParseTest(String s, int count, boolean error) throws Exception {
    try {
      VersionConstraint constraint = VersionConstraint.Companion.parse(s);
      Assert.assertFalse("Exception should have raised for " + s, error);
      Assert.assertEquals("Count mismatch for " + s, count, constraint.getConstraints().size());
    } catch (MalformedConstraintException e) {
      if (!error) Assert.fail("On \"" + s + "\": " + e.getMessage());
    }
  }

  public void testConstraintCheck() throws Exception {
    Object[][] params = {
        {">= 1.0, < 1.2", "1.1.5", true},
        {"< 1.0, < 1.2", "1.1.5", false},
        {"= 1.0", "1.1.5", false},
        {"= 1.0", "1.0.0", true},
        {"1.0", "1.0.0", true},
        {"~> 1.0", "2.0", false},
        {"~> 1.0", "1.1", true},
        {"~> 1.0", "1.2.3", true},
        {"~> 1.0.0", "1.2.3", false},
        {"~> 1.0.0", "1.0.7", true},
        {"~> 1.0.0", "1.1.0", false},
        {"~> 1.0.7", "1.0.4", false},
        {"~> 1.0.7", "1.0.7", true},
        {"~> 1.0.7", "1.0.8", true},
        {"~> 1.0.7", "1.0.7.5", true},
        {"~> 1.0.7", "1.0.6.99", false},
        {"~> 1.0.7", "1.0.8.0", true},
        {"~> 1.0.9.5", "1.0.9.5", true},
        {"~> 1.0.9.5", "1.0.9.4", false},
        {"~> 1.0.9.5", "1.0.9.6", true},
        {"~> 1.0.9.5", "1.0.9.5.0", true},
        {"~> 1.0.9.5", "1.0.9.5.1", true},
        {"~> 2.0", "2.1.0-beta", false},
        {"~> 2.1.0-a", "2.2.0", false},
        {"~> 2.1.0-a", "2.1.0", false},
        {"~> 2.1.0-a", "2.1.0-beta", true},
        {"~> 2.1.0-a", "2.2.0-alpha", false},
        {"> 2.0", "2.1.0-beta", false},
        {">= 2.1.0-a", "2.1.0-beta", true},
        {">= 2.1.0-a", "2.1.1-beta", false},
        {">= 2.0.0", "2.1.0-beta", false},
        {">= 2.1.0-a", "2.1.1", true},
        {">= 2.1.0-a", "2.1.1-beta", false},
        {">= 2.1.0-a", "2.1.0", true},
        {"<= 2.1.0-a", "2.0.0", true},
    };
    for (Object[] param : params) {
      doConstraintCheckTest((String) param[0], (String) param[1], (boolean) param[2]);
    }
  }

  private void doConstraintCheckTest(String c, String v, boolean expected) throws Exception {
    VersionConstraint constraint = VersionConstraint.Companion.parse(c);
    Version version = Version.Companion.parse(v);
    boolean actual = constraint.check(version);
    assertEquals(String.format("For constraint '%s' and version '%s' expected %b, got %b", c, v, expected, actual), expected, actual);
  }

  public void testConstraintEmptyCheck() throws Exception {
    Object[][] params = {
        {"= 1.0, 1.0.0", false},
        {"= 1.0, 1.0", false},
        {"= 1.0, 1", false},

        {"= 1.0, 1.1.5", true},

        {">= 1.0, < 1.2", false},
        {"< 1.0, < 1.2", false},
        {"< 1.0, > 1.2", true},
        {"< 1.0, ~> 1.2", true},
        {"> 1.0, ~> 1.2", false},

        {"> 1.2, ~> 1.2", false},
        {"< 1.2, ~> 1.2", true},
        {"<= 1.2, ~> 1.2", false},
        {"<= 1.2, >= 1.2", false},
        {"<= 1.2, ~> 1.2, != 1.2", true},
        {"<= 1.2, >= 1.2, != 1.2", true},

        {"0", false},
        {"0.0", false},

        {"!= 0", false},
        {"< 0", true},

        {"<= 1.2, 1.3", true},

        {"~> 1.2, < 1.3", false},
        {"~> 1.2, <= 1.2", false},
        {"~> 1.2, < 1.2", true},

        {"~> 1.2, > 1.2", false},
        {"~> 1.2, > 1.3", false},
        {"~> 1.2, >= 1.3", false},
        {"~> 1.2, >= 1.2", false},
        {"~> 1.2.0, > 1.2", false},
        {"~> 1.2.0, >= 1.2", false},
        {"~> 1.2.0, > 1.3", true},
        {"~> 1.2.0, >= 1.3", true},
    };
    for (Object[] param : params) {
      doConstraintEmptyCheckTest((String) param[0], (boolean) param[1]);
    }
  }

  private void doConstraintEmptyCheckTest(String c, boolean expected) throws Exception {
    VersionConstraint constraint = VersionConstraint.Companion.parse(c);
    boolean actual = constraint.isEmpty();
    assertEquals(String.format("For constraint '%s' emptiness expected %b, got %b", c, expected, actual), expected, actual);
  }


}
