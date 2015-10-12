package com.intellij.javascript.karma.util;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class KarmaUtilTest {
  @Test
  public void testKarmaFilename() throws Exception {
    assertKarmaConfig("karma.conf.js");
    assertKarmaConfig("karma-conf.js");
    assertKarmaConfig("karma.spec.js");
    assertKarmaConfig("karma-1.js");
    assertKarmaConfig("-conf.js");
    assertKarmaConfig("-karma.ts");
    assertNotKarmaConfig("*karma.ts");

    assertKarmaConfig("my.conf.coffee");
    assertNotKarmaConfig("a.coffee");
    assertNotKarmaConfig("karma");
    assertNotKarmaConfig("conf.js");
    assertNotKarmaConfig("karma#1.js");
  }

  private static void assertKarmaConfig(@NotNull String filename) {
    Assert.assertTrue(KarmaUtil.isKarmaConfigFile(filename));
  }

  private static void assertNotKarmaConfig(@NotNull String filename) {
    Assert.assertFalse(KarmaUtil.isKarmaConfigFile(filename));
  }
}
