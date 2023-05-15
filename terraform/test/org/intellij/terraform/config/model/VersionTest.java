// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model;

import junit.framework.TestCase;
import org.intellij.terraform.config.model.version.MalformedVersionException;
import org.intellij.terraform.config.model.version.Version;
import org.junit.Assert;

public class VersionTest extends TestCase {

  public void testVersionParsed() throws Exception {
    Object[][] params = {
        {"", true},
        {"1.2.3", false},
        {"1.0", false},
        {"1", false},
        {"1.2.beta", true},
        {"1.21.beta", true},
        {"foo", true},
        {"1.2-5", false},
        {"1.2-beta.5", false},
        {"\n1.2", true},
        {"1.2.0-x.Y.0+metadata", false},
        {"1.2.0-x.Y.0+metadata-width-hypen", false},
        {"1.2.3-rc1-with-hypen", false},
        {"1.2.3.4", false},
        {"1.2.0.4-x.Y.0+metadata", false},
        {"1.2.0.4-x.Y.0+metadata-width-hypen", false},
        {"1.2.0-X-1.2.0+metadata~dist", false},
        {"1.2.3.4-rc1-with-hypen", false},
        {"1.2.3.4", false},
        {"v1.2.3", false},
        {"foo1.2.3", true},
        {"1.7rc2", false},
        {"v1.7rc2", false},
        {"1.0-", false},
    };
    for (Object[] param : params) {
      doVersionTest((String) param[0], (boolean) param[1]);
    }
  }

  private void doVersionTest(String s, boolean error) throws Exception {
    try {
      Version.Companion.parse(s);
      Assert.assertFalse("Exception should have raised for " + s, error);
    } catch (MalformedVersionException e) {
      if (!error) Assert.fail("On \"" + s + "\": " + e.getMessage());
    }
  }

  public void testVersionCompare() throws Exception {
    Object[][] params = {
        {"1.2.3", "1.4.5", -1},
        {"1.2-beta", "1.2-beta", 0},
        {"1.2", "1.1.4", 1},
        {"1.2", "1.2-beta", 1},
        {"1.2+foo", "1.2+beta", 0},
        {"v1.2", "v1.2-beta", 1},
        {"v1.2+foo", "v1.2+beta", 0},
        {"v1.2.3.4", "v1.2.3.4", 0},
        {"v1.2.0.0", "v1.2", 0},
        {"v1.2.0.0.1", "v1.2", 1},
        {"v1.2", "v1.2.0.0", 0},
        {"v1.2", "v1.2.0.0.1", -1},
        {"v1.2.0.0", "v1.2.0.0.1", -1},
        {"v1.2.3.0", "v1.2.3.4", -1},
        {"1.7rc2", "1.7rc1", 1},
        {"1.7rc2", "1.7", -1},
        {"1.2.0", "1.2.0-X-1.2.0+metadata~dist", 1},
    };

    for (Object[] param : params) {
      doVersionCompareTest((String) param[0], (String) param[1], (int) param[2]);
    }
  }

  public void testPrereleasesCompare() throws Exception {
    Object[][] params = {
        {"1.2-beta.2", "1.2-beta.2", 0},
        {"1.2-beta.1", "1.2-beta.2", -1},
        {"1.2-beta.2", "1.2-beta.11", -1},
        {"3.2-alpha.1", "3.2-alpha", 1},
        {"1.2-beta.2", "1.2-beta.1", 1},
        {"1.2-beta.11", "1.2-beta.2", 1},
        {"1.2-beta", "1.2-beta.3", -1},
        {"1.2-alpha", "1.2-beta.3", -1},
        {"1.2-beta", "1.2-alpha.3", 1},
        {"3.0-alpha.3", "3.0-rc.1", -1},
        {"3.0-alpha3", "3.0-rc1", -1},
        {"3.0-alpha.1", "3.0-alpha.beta", -1},
        {"5.4-alpha", "5.4-alpha.beta", 1},
        {"v1.2-beta.2", "v1.2-beta.2", 0},
        {"v1.2-beta.1", "v1.2-beta.2", -1},
        {"v3.2-alpha.1", "v3.2-alpha", 1},
        {"v3.2-rc.1-1-g123", "v3.2-rc.2", 1},
    };

    for (Object[] param : params) {
      doVersionCompareTest((String) param[0], (String) param[1], (int) param[2]);
    }
  }

  private void doVersionCompareTest(String a, String b, int expected) throws Exception {
    Version v1 = Version.Companion.parse(a);
    Version v2 = Version.Companion.parse(b);

    int actual = v1.compareTo(v2);
    Assert.assertEquals(String.format("Comparison error for %s and %s, got %d expected %d", a, b, actual, expected), expected, actual);
  }
}
