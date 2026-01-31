package org.osmorc.frameworkintegration;

import org.jetbrains.osgi.jps.model.LibraryBundlificationRule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LibraryBundlificationRuleTest {
  @Test
  public void testEqual() {
    LibraryBundlificationRule rule1 = new LibraryBundlificationRule();
    LibraryBundlificationRule rule2 = new LibraryBundlificationRule();
    assertTrue(rule1.equals(rule2));

    rule1.setRuleRegex(".+\\.jar");
    rule2.setRuleRegex(".+\\.zip");
    assertFalse(rule1.equals(rule2));
  }

  @Test
  public void testValidation() {
    LibraryBundlificationRule rule = new LibraryBundlificationRule();
    rule.validate();

    rule.setRuleRegex("lib[Jj");
    try {
      rule.validate();
      fail();
    }
    catch (IllegalArgumentException e) { }

    rule.setRuleRegex(".*");
    rule.setAdditionalProperties("\\u1wtf");
    try {
      rule.validate();
      fail();
    }
    catch (IllegalArgumentException e) { }
  }
}
