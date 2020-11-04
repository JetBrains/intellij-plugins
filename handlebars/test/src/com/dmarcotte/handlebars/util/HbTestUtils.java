// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.util;

import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.io.File;

public final class HbTestUtils {
  /**
   * The root of the test data directory
   */
  public static final String BASE_TEST_DATA_PATH = findTestDataPath();

  private static String findTestDataPath() {
    File f = new File("test", "data");
    if (f.exists()) {
      return f.getAbsolutePath();
    }
    return PathManager.getHomePath() + "/contrib/handlebars/test/data";
  }

  @TestOnly
  public static void setOpenHtmlAsHandlebars(final boolean value, @NotNull final Project project, @NotNull Disposable parentDisposable) {
    final boolean oldValue = HbConfig.shouldOpenHtmlAsHandlebars(project);
    if (oldValue == value) return;

    HbConfig.setShouldOpenHtmlAsHandlebars(value, project);
    Disposer.register(parentDisposable, new Disposable() {
      @Override
      public void dispose() {
        HbConfig.setShouldOpenHtmlAsHandlebars(oldValue, project);
      }
    });
  }
}
