// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.watcher;

import com.intellij.plugins.watcher.DependenciesTestBase;
import com.intellij.plugins.watcher.model.TaskOptions;
import com.jetbrains.plugins.jade.JadeTestUtil;
import com.jetbrains.plugins.jade.psi.JadeFileType;

public class JadeFileDependenciesTest extends DependenciesTestBase {

  public JadeFileDependenciesTest() {
    super(JadeFileType.INSTANCE.getDefaultExtension());
  }

  @Override
  public String getTestDataPath() {
    return JadeTestUtil.getBaseTestDataPath() + "/watcher";
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
