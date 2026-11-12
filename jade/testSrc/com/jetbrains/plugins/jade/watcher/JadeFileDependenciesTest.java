// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.watcher;

import com.intellij.openapi.project.Project;
import com.intellij.plugins.watcher.DependenciesTestFixture;
import com.intellij.plugins.watcher.model.TaskOptions;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.plugins.jade.psi.JadeFileType;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static com.intellij.plugins.watcher.DependenciesTestFixtureKt.dependenciesTestFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/jade/testData/watcher")
public class JadeFileDependenciesTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<DependenciesTestFixture> dependencies =
    dependenciesTestFixture(projectFixture, JadeFileType.INSTANCE.getDefaultExtension());

  @Test
  public void testIncludeHead() {
    dependencies.get().doTest("include", "head.jade", new TaskOptions(), "main.jade");
  }

  @Test
  public void testIncludeScripts() {
    dependencies.get().doTest("include", "scripts.jade", new TaskOptions(), "main.jade");
  }

  @Test
  public void testExtends() {
    dependencies.get().doTest("extends", "layout.jade", new TaskOptions(), "index.jade");
  }

}
