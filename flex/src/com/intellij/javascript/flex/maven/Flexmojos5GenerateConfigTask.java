package com.intellij.javascript.flex.maven;

import org.jetbrains.idea.maven.project.MavenProjectsTree;

public class Flexmojos5GenerateConfigTask extends Flexmojos4GenerateConfigTask {
  public Flexmojos5GenerateConfigTask(MavenProjectsTree tree) {
    super(tree);
  }

  protected String getIdeaConfiguratorClassName() {
    return "com.intellij.flex.maven.IdeaConfiguratorFlexmojos5";
  }
}
