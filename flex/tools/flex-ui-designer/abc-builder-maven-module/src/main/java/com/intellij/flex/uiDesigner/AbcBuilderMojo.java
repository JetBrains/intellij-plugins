package com.intellij.flex.uiDesigner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.lang.RuntimeException;
import java.lang.String;

/**
 * @goal generate
 * @phase compile
 */
public class AbcBuilderMojo extends AbstractMojo {
  /**
   * The maven project.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  @SuppressWarnings({"UnusedDeclaration"})
  private MavenProject project;

  /**
   * @parameter
   */
  private String flexVersion;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      String rootPath = project.getBuild().getDirectory();
      ComplementSwfBuilder.build(rootPath, flexVersion);
    }
    catch (IOException e) {
      throw new MojoFailureException("", e);
    }
  }
}
