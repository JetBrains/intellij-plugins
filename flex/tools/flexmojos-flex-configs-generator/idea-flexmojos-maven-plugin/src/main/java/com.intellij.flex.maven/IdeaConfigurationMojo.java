package com.intellij.flex.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.LifecycleExecutionPlanCalculator;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.strategy.AbstractStrategy;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @goal generate
 * @requiresDependencyResolution compile
 * @threadSafe
 * @phase compile
 */
@Component(role=IdeaConfigurationMojo.class)
public class IdeaConfigurationMojo extends AbstractMojo {
  @Requirement
  private MavenPluginManager mavenPluginManager;

  /**
   * @parameter expression="${session}"
   * @required
   * @readonly
   */
  @SuppressWarnings({"UnusedDeclaration"}) private MavenSession session;

  /**
   * @parameter expression="${mojoExecution}"
   * @required
   * @readonly
   */
  @SuppressWarnings({"UnusedDeclaration"}) private MojoExecution mojoExecution;

  @Requirement
  private BuildPluginManager pluginManager;

  @Requirement
  private LifecycleExecutionPlanCalculator lifeCycleExecutionPlanCalculator;

  /**
   * @parameter expression="${generateAlsoShareable}"
   * @readonly
   */
  @SuppressWarnings({"UnusedDeclaration"})
  private boolean generateAlsoShareable;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    MavenProject project = session.getCurrentProject();
    String packaging = project.getPackaging();
    if (!Utils.isFlashProject(project)) {
      return;
    }

    Plugin flexmojosPlugin = null;
    for (Plugin plugin : project.getBuildPlugins()) {
      if (plugin.getGroupId().equals("org.sonatype.flexmojos") && plugin.getArtifactId().equals("flexmojos-maven-plugin")) {
        flexmojosPlugin = plugin;
      }
    }

    if (flexmojosPlugin == null) {
      return;
    }

    final ClassRealm flexmojosPluginRealm;
    MojoExecution flexmojosMojoExecution;
    try {
      MojoDescriptor flexmojosMojoDescriptor = pluginManager.getMojoDescriptor(flexmojosPlugin, "compile-" + packaging, project.getRemotePluginRepositories(), session.getRepositorySession());
      flexmojosMojoExecution = new MojoExecution(flexmojosMojoDescriptor, "default-cli", MojoExecution.Source.CLI);
      flexmojosPluginRealm = pluginManager.getPluginRealm(session, flexmojosMojoDescriptor.getPluginDescriptor());
      lifeCycleExecutionPlanCalculator.setupMojoExecution(session, project, flexmojosMojoExecution);
    }
    catch (Exception e) {
      throw new MojoExecutionException("Cannot generate flex-config", e);
    }

    Mojo mojo = null;
    try {
      mojo = mavenPluginManager.getConfiguredMojo(Mojo.class, session, flexmojosMojoExecution);
    }
    catch (Exception e) {
      throw new MojoExecutionException("Cannot generate flex-config", e);
    }
    finally {
      mavenPluginManager.releaseMojo(mojo, mojoExecution);
    }

    IdeaConfigurator configurator = generateAlsoShareable ? new ShareableFlexConfigGenerator() : new IdeaConfigurator();
    try {
      modifyOurClassRealm(flexmojosPluginRealm);
      configurator.init(session, project, getClassifier(mojo, flexmojosPluginRealm));
      if ("swc".equals(packaging)) {
        configurator.buildConfiguration(mojo, flexmojosPluginRealm.loadClass("org.sonatype.flexmojos.compiler.ICompcConfiguration"));
      }
      else {
        configurator.buildConfiguration(mojo, getSourceFileForSwf(mojo, flexmojosPluginRealm), flexmojosPluginRealm.loadClass("org.sonatype.flexmojos.compiler.ICommandLineConfiguration"));
      }
    }
    catch (Exception e) {
      throw new MojoExecutionException("Failed to execute configurator: " + e.getMessage(), e);
    }
  }

  private void modifyOurClassRealm(ClassRealm flexmojosPluginRealm) throws NoSuchFieldException, IllegalAccessException {
    final Field realm = AbstractStrategy.class.getDeclaredField("realm");
    realm.setAccessible(true);
    realm.set(mojoExecution.getMojoDescriptor().getPluginDescriptor().getClassRealm().getStrategy(), flexmojosPluginRealm);
  }

  private File getSourceFileForSwf(Mojo mojo, ClassRealm flexmojosPluginRealm)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
    Method getSourceFileMethod = flexmojosPluginRealm.loadClass("org.sonatype.flexmojos.plugin.compiler.MxmlcMojo").getDeclaredMethod(
      "getSourceFile");
    getSourceFileMethod.setAccessible(true);
    return (File)getSourceFileMethod.invoke(mojo);
  }

  private String getClassifier(Mojo mojo, ClassRealm flexmojosPluginRealm)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
    Method getSourceFileMethod = flexmojosPluginRealm.loadClass("org.sonatype.flexmojos.plugin.compiler.AbstractFlexCompilerMojo").getDeclaredMethod(
      "getClassifier");
    getSourceFileMethod.setAccessible(true);
    return (String)getSourceFileMethod.invoke(mojo);
  }
}
