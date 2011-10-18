package com.intellij.flex.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.LifecycleExecutionPlanCalculator;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @goal generate
 * @requiresDependencyResolution compile
 * @threadSafe
 * @phase compile
 * @aggregator
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
   * @parameter expression="${generateShareable}"
   * @readonly
   */
  @SuppressWarnings({"UnusedDeclaration"})
  private boolean generateShareable;

  /**
   * @parameter expression="${generateNonShareable}" default-value="true"
   * @readonly
   */
  @SuppressWarnings({"UnusedDeclaration"})
  private boolean generateNonShareable;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    List<String> configurators = new ArrayList<String>(2);
    if (generateNonShareable) {
      configurators.add("com.intellij.flex.maven.IdeaConfigurator");
    }
    if (generateShareable) {
      configurators.add("com.intellij.flex.maven.ShareableFlexConfigGenerator");
    }

    // for some unknown reasons (WTF?), project artifact is not equals is not the same instance as in dependentProject.getArtifacts(),
    // so, without this hack, we have two problems:
    // 1) if artifact located in local repo, but not in project dir, output file will be from local repo (but must be from project dir)
    // 2) if artifact located nor in local repo, nor in project dir, will be build failure due to "Unable to handle unresolved artifact"
    final HashMap<Artifact, MavenProject> ourProjects = new HashMap<Artifact, MavenProject>(session.getProjects().size());
    final String rootProjectDirPath = session.getTopLevelProject().getBasedir().getPath();
    for (MavenProject project : session.getProjects()) {
      if (!(project.getPackaging().equals("swc") || project.getPackaging().equals("swf"))) {
        continue;
      }

      for (Artifact artifact : project.getArtifacts()) {
        final MavenProject reactorProject = ourProjects.get(artifact);
        if (reactorProject != null) {
          // case 2, see comment above
          if (!artifact.isResolved()) {
            artifact.setResolved(true);
          }
          // case 1, see comment above
          else if (artifact.getFile().getPath().startsWith(rootProjectDirPath)) {
            continue;
          }

          artifact.setFile(reactorProject.getArtifact().getFile());
        }
      }

      ourProjects.put(project.getArtifact(), project);

      try {
        // requires for flexmojos
        session.setCurrentProject(project);
        generateForProject(project, configurators);
      }
      catch (Exception e) {
        throw new MojoExecutionException("Cannot generate flex config: " + e.getMessage(), e);
      }
      finally {
        session.setCurrentProject(session.getTopLevelProject());
      }
    }
  }

  private void generateForProject(MavenProject project, List<String> configurators) throws Exception {
    MojoExecution flexmojosMojoExecution = null;
    MojoExecution flexmojosGeneratorMojoExecution = null;
    for (Plugin plugin : project.getBuildPlugins()) {
      if (plugin.getGroupId().equals("org.sonatype.flexmojos")) {
        if (flexmojosMojoExecution == null && plugin.getArtifactId().equals("flexmojos-maven-plugin")) {
          flexmojosMojoExecution = createMojoExecution(plugin, "compile-" + project.getPackaging(), project);
        }
        else if (flexmojosGeneratorMojoExecution == null && plugin.getArtifactId().equals("flexmojos-generator-mojo")) {
          flexmojosGeneratorMojoExecution = createMojoExecution(plugin, "generate", project);
        }

        if (flexmojosMojoExecution != null && flexmojosGeneratorMojoExecution != null) {
          break;
        }
      }
    }

    if (flexmojosMojoExecution == null) {
      return;
    }

    ClassRealm flexmojosPluginRealm = pluginManager.getPluginRealm(session,
                                                                   flexmojosMojoExecution.getMojoDescriptor().getPluginDescriptor());

    //flexmojosPluginRealm.addURL(new URL("file://" + session.getLocalRepository().getUrl() Users/develar/Documents/flexmojos-idea-configurator/idea-configurator/target/classes/"));
    flexmojosPluginRealm.addURL(new URL("file:///Users/develar/Documents/idea/flex/lib/flexmojos-idea-configurator.jar"));
    flexmojosPluginRealm.importFrom(getClass().getClassLoader(), "com.intellij.flex.maven.FlexConfigGenerator");
    flexmojosPluginRealm.importFrom(getClass().getClassLoader(), "com.intellij.flex.maven.Utils");
    Mojo mojo = null;
    try {
      mojo = mavenPluginManager.getConfiguredMojo(Mojo.class, session, flexmojosMojoExecution);

      for (String configuratorClassName : configurators) {
        Class configuratorClass = flexmojosPluginRealm.loadClass(configuratorClassName);
        FlexConfigGenerator configurator = (FlexConfigGenerator)configuratorClass.getConstructor(MavenSession.class, File.class).newInstance(session, null);
        configurator.preGenerate(project, getClassifier(mojo), flexmojosGeneratorMojoExecution);
        if ("swc".equals(project.getPackaging())) {
          configurator.generate(mojo);
        }
        else {
          configurator.generate(mojo, getSourceFileForSwf(mojo));
        }
        configurator.postGenerate(project);
      }
    }
    finally {
      mavenPluginManager.releaseMojo(mojo, mojoExecution);
    }
  }

  private MojoExecution createMojoExecution(Plugin plugin, String goal, MavenProject project) throws Exception {
    MojoDescriptor mojoDescriptor = pluginManager.getMojoDescriptor(plugin, goal, project.getRemotePluginRepositories(),
                                                                    session.getRepositorySession());
    MojoExecution mojoExecution = new MojoExecution(mojoDescriptor, "default-cli", MojoExecution.Source.CLI);
    lifeCycleExecutionPlanCalculator.setupMojoExecution(session, project, mojoExecution);
    return mojoExecution;
  }


  private File getSourceFileForSwf(Mojo mojo)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
    Method getSourceFileMethod = mojo.getClass().getDeclaredMethod("getSourceFile");
    getSourceFileMethod.setAccessible(true);
    return (File)getSourceFileMethod.invoke(mojo);
  }

  private String getClassifier(Mojo mojo)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
    Method getSourceFileMethod = mojo.getClass().getMethod("getClassifier");
    getSourceFileMethod.setAccessible(true);
    return (String)getSourceFileMethod.invoke(mojo);
  }
}
