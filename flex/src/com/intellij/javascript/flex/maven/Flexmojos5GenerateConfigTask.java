// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.maven;

import org.jetbrains.idea.maven.project.MavenProjectsTree;

final class Flexmojos5GenerateConfigTask extends Flexmojos4GenerateConfigTask {
  Flexmojos5GenerateConfigTask(MavenProjectsTree tree) {
    super(tree);
  }

  @Override
  protected String getIdeaConfiguratorClassName() {
    return "com.intellij.flex.maven.IdeaConfiguratorFlexmojos5";
  }
}
