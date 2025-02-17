// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.watcher;

import com.intellij.plugins.watcher.DependenciesTestBase;
import com.intellij.plugins.watcher.model.TaskOptions;
import com.jetbrains.plugins.jade.JadeHighlightingTest;
import com.jetbrains.plugins.jade.psi.JadeFileType;
import org.jetbrains.annotations.NotNull;

public class JadeFileDependenciesTest extends DependenciesTestBase {

  private static final String TEST_DATA_PATH = JadeHighlightingTest.RELATIVE_TEST_DATA_PATH + "/watcher";

  public JadeFileDependenciesTest() {
    super(JadeFileType.INSTANCE.getDefaultExtension());
  }

  @NotNull
  @Override
  protected String getBasePath() {
    return TEST_DATA_PATH;
  }

  public void testIncludeHead() {
    doTest("include", "head.jade", new TaskOptions(), "main.jade");
  }

  public void testIncludeScripts() {
    doTest("include", "scripts.jade", new TaskOptions(), "main.jade");
  }

  public void testExtends() {
    doTest("extends", "layout.jade", new TaskOptions(), "index.jade");
  }

}
