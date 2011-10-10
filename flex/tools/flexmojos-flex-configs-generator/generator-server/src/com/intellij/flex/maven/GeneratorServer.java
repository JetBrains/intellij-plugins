package com.intellij.flex.maven;

import org.apache.maven.DefaultMaven;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.*;
import org.apache.maven.lifecycle.internal.LifecycleExecutionPlanCalculator;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.codehaus.plexus.*;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GeneratorServer {
  private final PlexusContainer plexusContainer;
  private final MavenSession session;

  private final DataInputStream in;
  private final BuildPluginManager pluginManager;
  private final MavenPluginManager mavenPluginManager;
  
  private final File generatorOutputDirectory;
  private final LoggerManagerImpl loggerManager;
  
  private final Maven maven;

  public static void main(String[] args) throws Exception {
    new GeneratorServer(args);
  }

  public Logger getLogger() {
    return loggerManager.logger;
  }

  public GeneratorServer(String[] args)
    throws ComponentLookupException, IOException, MavenExecutionRequestPopulationException, SettingsBuildingException,
           PlexusContainerException {
    generatorOutputDirectory = new File(args[4]);
    in = new DataInputStream(new BufferedInputStream(System.in));

    loggerManager = new LoggerManagerImpl(null);
    plexusContainer = createPlexusContainer();

    pluginManager = plexusContainer.lookup(BuildPluginManager.class);
    mavenPluginManager = plexusContainer.lookup(MavenPluginManager.class);

    session = createSession(createExecutionRequest(args));
    maven = new Maven(plexusContainer, session);

    int projectsCount = in.readUnsignedShort();
    for (int i = 0; i < projectsCount; i++) {
      final String pathname = in.readUTF();
      try {
        generate(pathname);
        System.out.append("\n[fcg] generated: ").append(pathname).append("[/fcg]").flush();
      }
      catch (Exception e) {
        getLogger().error("Cannnot generate flex config for " + pathname, e);
      }
    }
  }

  private void generate(String pathname) throws Exception {
    final MavenProject project = maven.readProject(new File(pathname));
    session.setCurrentProject(project);

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

    final List<String> configurators = new ArrayList<String>(2);
    //if (generateNonShareable) {
    configurators.add("com.intellij.flex.maven.IdeaConfigurator");
    //}
    //if (generateShareable) {
    //  configurators.add("com.intellij.flex.maven.ShareableFlexConfigGenerator");
    //}

    assert flexmojosMojoExecution != null;
    ClassRealm flexmojosPluginRealm = pluginManager.getPluginRealm(session,
                                                                      flexmojosMojoExecution.getMojoDescriptor().getPluginDescriptor());
    flexmojosPluginRealm.addURL(new URL("file:///Users/develar/Documents/flexmojos-idea-configurator/out/artifacts/idea-configurator.jar"));
    
    Mojo mojo = null;
    try {
      mojo = mavenPluginManager.getConfiguredMojo(Mojo.class, session, flexmojosMojoExecution);
      for (String configuratorClassName : configurators) {
        Class configuratorClass = flexmojosPluginRealm.loadClass(configuratorClassName);
        FlexConfigGenerator configurator = (FlexConfigGenerator)configuratorClass.getConstructor(MavenSession.class, File.class).newInstance(session, generatorOutputDirectory);
        configurator.preGenerate(project, getClassifier(mojo), flexmojosGeneratorMojoExecution);
        try {
          if ("swc".equals(project.getPackaging())) {
            configurator.generate(mojo);
          }
          else {
            configurator.generate(mojo, getSourceFileForSwf(mojo));
          }
        }
        finally {
          configurator.postGenerate();
        }
      }
    }
    finally {
      plexusContainer.release(mojo);
    }
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

  private MojoExecution createMojoExecution(Plugin plugin, String goal, MavenProject project) throws Exception {
    MojoDescriptor mojoDescriptor = pluginManager.getMojoDescriptor(plugin, goal, project
      .getRemotePluginRepositories(), session.getRepositorySession());
    MojoExecution mojoExecution = new MojoExecution(mojoDescriptor, "default-cli", MojoExecution.Source.CLI);
    plexusContainer.lookup(LifecycleExecutionPlanCalculator.class).setupMojoExecution(session, project, mojoExecution);
    return mojoExecution;
  }
  
  public File getOutputFile(File pomFile) throws Exception {
    final MavenProject project = maven.readProject(pomFile);
    final MavenProject oldProject = session.getCurrentProject();
    try {
      session.setCurrentProject(project);
      MojoExecution flexmojosMojoExecution = null;
      for (Plugin plugin : project.getBuildPlugins()) {
        if (plugin.getGroupId().equals("org.sonatype.flexmojos") && plugin.getArtifactId().equals("flexmojos-maven-plugin")) {
          flexmojosMojoExecution = createMojoExecution(plugin, "compile-" + project.getPackaging(), project);
          break;
        }
      }

      if (flexmojosMojoExecution == null) {
        return null;
      }


      // getPluginRealm creates plugin realm and populates pluginDescriptor.classRealm field
      pluginManager.getPluginRealm(session, flexmojosMojoExecution.getMojoDescriptor().getPluginDescriptor());

      final Mojo mojo = mavenPluginManager.getConfiguredMojo(Mojo.class, session, flexmojosMojoExecution);
      try {
        return new File((String)mojo.getClass().getMethod("getOutput").invoke(mojo));
      }
      finally {
        plexusContainer.release(mojo);
      }
    }
    finally {
      session.setCurrentProject(oldProject);
    }
  }

  private MavenSession createSession(MavenExecutionRequest request) throws ComponentLookupException {
    return new MavenSession(plexusContainer, createRepositorySession(request), request, new DefaultMavenExecutionResult());
  }

  private RepositorySystemSession createRepositorySession(MavenExecutionRequest request) throws ComponentLookupException {
    DefaultRepositorySystemSession session = (DefaultRepositorySystemSession)((DefaultMaven)plexusContainer.lookup(org.apache.maven.Maven.class)).newRepositorySession(request);
    if (!request.isUpdateSnapshots()) {
      session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_NEVER);
    }
    return session;
  }

  private MavenExecutionRequest createExecutionRequest(String[] args)
    throws ComponentLookupException, SettingsBuildingException, MavenExecutionRequestPopulationException, IOException {
    MavenExecutionRequest request = new DefaultMavenExecutionRequest();
    request.setGlobalSettingsFile(new File(args[0]));
    if (!args[1].equals(" ")) {
      request.setUserSettingsFile(new File(args[1]));
    }
    request.setLocalRepository(createLocalRepository(args[2]));
    request.setSystemProperties(System.getProperties());

    request.setOffline(args[3].equals("t")).setUpdateSnapshots(false).setCacheNotFound(true).setCacheTransferError(true);

    int profilesLength = in.readShort();
    if (profilesLength > 0) {
      List<String> activeProfiles = new ArrayList<String>(profilesLength);
      while (profilesLength-- > 0) {
        activeProfiles.add(in.readUTF());
      }
      request.setActiveProfiles(activeProfiles);
    }

    plexusContainer.lookup(MavenExecutionRequestPopulator.class).populateFromSettings(request, createSettings(request));

    request.setWorkspaceReader(new WorkspaceReaderImpl(in, this));
    return request;
  }

  public ArtifactRepository createLocalRepository(String localRepositoryPath) throws ComponentLookupException {
    try {
      return plexusContainer.lookup(RepositorySystem.class).createLocalRepository(new File(localRepositoryPath));
    }
    catch (InvalidRepositoryException ex) {
      // can't happen
      throw new IllegalStateException(ex);
    }
  }

  private Settings createSettings(MavenExecutionRequest mavenExecutionRequest) throws ComponentLookupException, SettingsBuildingException {
    SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
    request.setSystemProperties(request.getSystemProperties());
    request.setGlobalSettingsFile(mavenExecutionRequest.getGlobalSettingsFile());
    request.setUserSettingsFile(mavenExecutionRequest.getUserSettingsFile());
    return plexusContainer.lookup(SettingsBuilder.class).build(request).getEffectiveSettings();
  }

  private DefaultPlexusContainer createPlexusContainer() throws PlexusContainerException {
    ContainerConfiguration mavenCoreCC = new DefaultContainerConfiguration().setClassWorld(new ClassWorld("plexus.core", ClassWorld.class.getClassLoader())).setName("mavenCore");
    mavenCoreCC.setAutoWiring(true);
    final DefaultPlexusContainer container = new DefaultPlexusContainer(mavenCoreCC);
    container.setLoggerManager(loggerManager);
    return container;
  }
}
