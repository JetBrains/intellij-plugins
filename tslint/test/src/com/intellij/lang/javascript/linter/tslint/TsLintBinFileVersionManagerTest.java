package com.intellij.lang.javascript.linter.tslint;

import com.intellij.execution.ExecutionException;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

public class TsLintBinFileVersionManagerTest {
  @Test
  public void testVersion() throws Exception {
    TsLintBinFileVersionManager manager = new TsLintBinFileVersionManager();
    assertParsed(SemVer.parseFromText("2.4.0"), manager, "2.4.0");
    assertParsed(SemVer.parseFromText("2.1.1"), manager, "2.1.1\n");
    assertNotParsed(manager, "");
    assertNotParsed(manager, "usage: node ./tslint/bin/tslint");
    assertNotParsed(manager, "--help 2.1.1");
  }

  private static void assertParsed(@Nullable SemVer expected,
                                   @NotNull TsLintBinFileVersionManager manager,
                                   @NotNull String stdout) throws ExecutionException {
    Assert.assertNotNull(expected);
    SemVer version = manager.parse(stdout);
    Assert.assertEquals(expected, version);
  }

  private static void assertNotParsed(@NotNull TsLintBinFileVersionManager manager, @NotNull String stdout) throws ExecutionException {
    try {
      manager.parse(stdout);
      Assert.fail();
    }
    catch (ExecutionException ignored) {
    }
  }
}
