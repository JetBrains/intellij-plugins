package com.intellij.flex.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.*;
import org.codehaus.plexus.component.annotations.Component;

@Component(role=BuildPluginManager.class)
public class InvestigatorBuildPluginManager extends DefaultBuildPluginManager implements BuildPluginManager {
  @Override
  public void executeMojo(MavenSession session, MojoExecution mojoExecution)
    throws MojoFailureException, MojoExecutionException, PluginConfigurationException, PluginManagerException {
    System.out.print("fffffAAAAAAAAAAAAAAA \n\n\n\n\nFFFFFFFFFFFFFFFFFF BOOOOOOOOOOOO!!!!!!!!11111");
    super.executeMojo(session, mojoExecution);
  }
}

//@Component(role=BuildPluginManager.class)
//public class InvestigatorBuildPluginManager extends DefaultBuildPluginManager implements BuildPluginManager {
//  @Override
//  public void executeMojo(MavenSession session, MojoExecution mojoExecution)
//    throws MojoFailureException, MojoExecutionException, PluginConfigurationException, PluginManagerException {
//    System.out.print("fffffAAAAAAAAAAAAAAA \n\n\n\n\nFFFFFFFFFFFFFFFFFF BOOOOOOOOOOOO!!!!!!!!11111");
//    super.executeMojo(session, mojoExecution);
//  }
//}