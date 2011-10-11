package com.intellij.flex.maven;

import org.apache.maven.DefaultMaven;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.*;
import org.apache.maven.lifecycle.internal.ThreadConfigurationService;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
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
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class GeneratorServer {
  private final PlexusContainer plexusContainer;
  private final MavenSession session;

  private final DataInputStream in;
  private final MavenPluginManager mavenPluginManager;
  
  private final File generatorOutputDirectory;
  private final Logger logger;
  
  private final Maven maven;

  public static void main(String[] args) throws Exception {
    final long start = System.currentTimeMillis();
    new GeneratorServer(args);
    final long duration = System.currentTimeMillis() - start;
    System.out.print("\n[fcg] generating took " + duration + " ms: " + duration / 60000 + " min " + (duration % 60000) / 1000 + "sec");
  }

  public Logger getLogger() {
    return logger;
  }

  public GeneratorServer(String[] args)
    throws ComponentLookupException, IOException, MavenExecutionRequestPopulationException, SettingsBuildingException,
           PlexusContainerException, InterruptedException {
    generatorOutputDirectory = new File(args[4]);
    //noinspection ResultOfMethodCallIgnored
    generatorOutputDirectory.mkdirs();

    in = new DataInputStream(new BufferedInputStream(System.in));

    LoggerManager loggerManager = new ConsoleLoggerManager();
    logger = loggerManager.getLoggerForComponent(null);
    plexusContainer = createPlexusContainer(loggerManager);

    mavenPluginManager = plexusContainer.lookup(MavenPluginManager.class);

    session = createSession(createExecutionRequest(args));
    maven = new Maven(plexusContainer, session);
    
    final List<String> generators = new ArrayList<String>(2);
    final URL generatorJarPath = new URL("file://" + in.readUTF());
    generators.add(in.readUTF());

    final int projectsCount = in.readUnsignedShort();
    final ExecutorService executorService = plexusContainer.lookup(ThreadConfigurationService.class)
      .getExecutorService("1", true, projectsCount);
    try {
      for (int i = 0; i < projectsCount; i++) {
        final String pathname = in.readUTF();
        executorService.submit(new Runnable() {
          @Override
          public void run() {
            try {
              generate(pathname, generators, generatorJarPath);
              synchronized (System.out) {
                System.out.append("\n[fcg] generated: ").append(pathname).append("[/fcg]").flush();
              }
            }
            catch (Throwable e) {
              getLogger().error("Cannnot generate flex config for " + pathname, e);
            }
          }
        });
      }
    }
    finally {
      executorService.shutdown();
      executorService.awaitTermination(10, TimeUnit.MINUTES);
    }
  }

  private void generate(final String pathname, final List<String> generators, final URL generatorJarPath) throws Exception {
    final MavenProject project = maven.readProject(new File(pathname));
    session.setCurrentProject(project);

    MojoExecution flexmojosMojoExecution = null;
    MojoExecution flexmojosGeneratorMojoExecution = null;
    final String compileGoal = "compile-" + project.getPackaging();
    try {
      for (Plugin plugin : project.getBuildPlugins()) {
        if (plugin.getGroupId().equals("org.sonatype.flexmojos")) {
          if (flexmojosMojoExecution == null && plugin.getArtifactId().equals("flexmojos-maven-plugin")) {
            flexmojosMojoExecution = maven.createMojoExecution(plugin, compileGoal, project);
          }
          else if (flexmojosGeneratorMojoExecution == null && plugin.getArtifactId().equals("flexmojos-generator-mojo")) {
            flexmojosGeneratorMojoExecution = maven.createMojoExecution(plugin, "generate", project);
          }

          if (flexmojosMojoExecution != null && flexmojosGeneratorMojoExecution != null) {
            break;
          }
        }
      }

      assert flexmojosMojoExecution != null;
      final ClassRealm flexmojosPluginRealm = maven.getPluginRealm(flexmojosMojoExecution);
      flexmojosPluginRealm.addURL(generatorJarPath);

      final Mojo mojo = mavenPluginManager.getConfiguredMojo(Mojo.class, session, flexmojosMojoExecution);
      try {
        for (String configuratorClassName : generators) {
          Class configuratorClass = flexmojosPluginRealm.loadClass(configuratorClassName);
          FlexConfigGenerator configurator = (FlexConfigGenerator)configuratorClass.getConstructor(MavenSession.class, File.class)
                                                                                   .newInstance(session, generatorOutputDirectory);
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
        plexusContainer.release(mojo);
      }
    }
    finally {
      session.setCurrentProject(null);
      if (flexmojosMojoExecution != null) {
        maven.releaseMojoExecution(compileGoal, flexmojosMojoExecution);
      }
      if (flexmojosGeneratorMojoExecution != null) {
        maven.releaseMojoExecution("generate", flexmojosGeneratorMojoExecution);
      }
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

  
  public File getOutputFile(File pomFile) throws Exception {
    final MavenProject project = maven.readProject(pomFile);
    final MavenProject oldProject = session.getCurrentProject();
    final String compileGoal = "compile-" + project.getPackaging();
    MojoExecution flexmojosMojoExecution = null;
    try {
      session.setCurrentProject(project);
      for (Plugin plugin : project.getBuildPlugins()) {
        if (plugin.getGroupId().equals("org.sonatype.flexmojos") && plugin.getArtifactId().equals("flexmojos-maven-plugin")) {
          flexmojosMojoExecution = maven.createMojoExecution(plugin, compileGoal, project);
          break;
        }
      }

      if (flexmojosMojoExecution == null) {
        return null;
      }

      // getPluginRealm creates plugin realm and populates pluginDescriptor.classRealm field
      maven.getPluginRealm(flexmojosMojoExecution);

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
      if (flexmojosMojoExecution != null) {
        maven.releaseMojoExecution(compileGoal, flexmojosMojoExecution);
      }
    }
  }

  private MavenSession createSession(MavenExecutionRequest request) throws ComponentLookupException {
    return new ThreadSafeMavenSession(plexusContainer, createRepositorySession(request), request,
                                                       new DefaultMavenExecutionResult());
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

  private DefaultPlexusContainer createPlexusContainer(LoggerManager loggerManager) throws PlexusContainerException {
    ContainerConfiguration mavenCoreCC = new DefaultContainerConfiguration().setClassWorld(new ClassWorld("plexus.core", ClassWorld.class.getClassLoader())).setName("mavenCore");
    mavenCoreCC.setAutoWiring(true);
    final DefaultPlexusContainer container = new DefaultPlexusContainer(mavenCoreCC);
    container.setLoggerManager(loggerManager);
    return container;
  }
}
