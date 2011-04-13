package com.intellij.flex.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.plugin.compiler.MxmlcMojo;

/**
 * @goal generate-config-swf
 * @requiresDependencyResolution compile
 * @threadSafe
 */
public class ApplicationConfiguratorMojo extends MxmlcMojo {
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    IdeaConfigurator configurator = new IdeaConfigurator();
    try {
      configurator.init(project, classifier);
      configurator.buildConfiguration(this, getSourceFile());
    }
    catch (Exception e) {
      throw new MojoExecutionException("Failed to execute configurator: " + e.getMessage(), e);
    }
  }
}
