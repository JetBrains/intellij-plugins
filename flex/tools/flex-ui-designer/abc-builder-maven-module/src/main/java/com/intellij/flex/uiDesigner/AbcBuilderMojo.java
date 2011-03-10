package com.intellij.flex.uiDesigner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

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
      ComplementSwfBuilder.build(new File(rootPath + "/flex-injection-" + flexVersion + "-1.0-SNAPSHOT.swf"), null, rootPath, flexVersion);
    }
    catch (IOException e) {
      throw new MojoFailureException("", e);
    }
  }
}
