package com.intellij.javascript.karma.util;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KarmaUtilTest {
  @Test
  public void testMatchAllKarmaFilenames() {
    assertKarmaConfig("karma.conf.js", false);
    assertKarmaConfig("karma-conf.js", false);
    assertKarmaConfig("karma.spec.js", false);
    assertKarmaConfig("karma-1.js", false);
    assertKarmaConfig("-conf.js", false);
    assertKarmaConfig("-karma.ts", false);
    assertNotKarmaConfig("*karma.ts", false);

    assertKarmaConfig("my.conf.coffee", false);
    assertNotKarmaConfig("a.coffee", false);
    assertNotKarmaConfig("karma", false);
    assertNotKarmaConfig("conf.js", false);
    assertNotKarmaConfig("karma#1.js", false);
  }

  @Test
  public void testMatchMostRelevantNamesOnly() {
    assertKarmaConfig("karma.conf.js", true);
    assertKarmaConfig("karma-conf.js", true);
    assertKarmaConfig("karma-conf.coffee", true);
    assertKarmaConfig("karma.conf.ts", true);
    assertKarmaConfig("karma-conf.es6", true);

    assertNotKarmaConfig("my.conf.coffee", true);
    assertNotKarmaConfig("a.coffee", true);
    assertNotKarmaConfig("karma", true);
    assertNotKarmaConfig("karma$conf.js", true);
    assertNotKarmaConfig("karma#1.js", true);
  }

  private static void assertKarmaConfig(@NotNull String filename, boolean matchMostRelevantNamesOnly) {
    assertTrue(KarmaUtil.isKarmaConfigFile(filename, matchMostRelevantNamesOnly));
  }

  private static void assertNotKarmaConfig(@NotNull String filename, boolean matchMostRelevantNamesOnly) {
    assertFalse(KarmaUtil.isKarmaConfigFile(filename, matchMostRelevantNamesOnly));
  }
}
