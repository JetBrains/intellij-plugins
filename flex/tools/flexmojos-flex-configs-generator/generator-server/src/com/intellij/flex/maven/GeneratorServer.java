package com.intellij.flex.maven;

import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.*;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.codehaus.plexus.*;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.util.FilterRepositorySystemSession;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class GeneratorServer {
  private static final byte UNLOCKED = 0;
  private static final byte LOCKED = 1;

  private static final byte SERVER_MUST_READ = 2;
  private static final byte CLIENT_MUST_READ = 3;

  private static final byte READ_PROJECT = 1;
  private static final byte EXIT = 2;

  private final PlexusContainer plexusContainer;
  private final MavenSession session;
  private final MappedByteBuffer map;

  private final byte[] BYTE_BUFFER = new byte[8192];

  private static final Charset UTF8 = Charset.forName("utf-8");

  public static void main(String[] args) throws IOException, PlexusContainerException, ComponentLookupException, SettingsBuildingException,
                                                MavenExecutionRequestPopulationException, InterruptedException, ProjectBuildingException {
    new GeneratorServer(args, createSharedMemory());
  }

  private static MappedByteBuffer createSharedMemory() throws IOException {
    RandomAccessFile file = null;
    FileChannel channel = null;
    try {
      file = new RandomAccessFile(new File(System.getProperty("java.io.tmpdir"), "com.intellij.flex.maven.FlexMojos4FacetImporter"), "rw");
      channel = file.getChannel();
      return channel.map(MapMode.READ_WRITE, 0, 8192);
    }
    finally {
      if (channel != null) {
        channel.close();
      }
      if (file != null) {
        file.close();
      }
    }
  }

  public GeneratorServer(String[] args, MappedByteBuffer mem)
    throws PlexusContainerException, ComponentLookupException, SettingsBuildingException, MavenExecutionRequestPopulationException,
           InterruptedException, IOException, ProjectBuildingException {
    this.map = mem;
    plexusContainer = createPlexusContainer();

    createExecutionRequest(args);
    session = createSession(createExecutionRequest(args));

    while (true) {
      while (mem.get(0) != SERVER_MUST_READ) {
        Thread.sleep(5);
      }

      try {
        final byte method = mem.get(1);
        mem.position(2);
        switch (method) {
          case READ_PROJECT:
            generate();
            break;

          case EXIT:
            return;

          default:
            throw new IllegalStateException("Unknown method: " + method);
        }

        assert !mem.hasRemaining();
      }
      finally {
        mem.put(UNLOCKED);
      }
    }
  }

  private void generate() throws ProjectBuildingException, ComponentLookupException {
    int length = map.getShort();
    map.get(BYTE_BUFFER, 0, length);
    final String s = new String(BYTE_BUFFER, 0, length, UTF8);
    final File pomFile = new File(s);
    final MavenProject project = readProject(pomFile);
  }

  private MavenProject readProject(File pomFile) throws ComponentLookupException, ProjectBuildingException {
    ProjectBuildingRequest configuration = session.getRequest().getProjectBuildingRequest();
    configuration.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
    configuration.setRepositorySession(session.getRepositorySession());
    return plexusContainer.lookup(ProjectBuilder.class).build(pomFile, configuration).getProject();
  }

  private MavenSession createSession(MavenExecutionRequest request) throws ComponentLookupException {
    return new MavenSession(plexusContainer, createRepositorySession(request), request, new DefaultMavenExecutionResult());
  }

  private RepositorySystemSession createRepositorySession(MavenExecutionRequest request) throws ComponentLookupException {
      RepositorySystemSession session = ((DefaultMaven)plexusContainer.lookup(Maven.class)).newRepositorySession(request);
      if (!request.isUpdateSnapshots()) {
        session = new FilterRepositorySystemSession(session) {
          public String getUpdatePolicy() {
            return RepositoryPolicy.UPDATE_POLICY_NEVER;
          }
        };
      }
      return session;
  }

  private MavenExecutionRequest createExecutionRequest(String[] args)
    throws ComponentLookupException, SettingsBuildingException, MavenExecutionRequestPopulationException {
    MavenExecutionRequest request = new DefaultMavenExecutionRequest();
    request.setGlobalSettingsFile(new File(args[0]));
    if (!args[1].equals(" ")) {
      request.setUserSettingsFile(new File(args[1]));
    }
    request.setLocalRepository(createLocalRepository(args[2]));
    request.setSystemProperties(System.getProperties());

    request.setOffline(args[3].equals("t"));
    int profilesLength = Integer.parseInt(args[4]);
    if (profilesLength > 0) {
      List<String> activeProfiles = new ArrayList<String>(profilesLength);
      for (int i = 5, n = profilesLength + 5; i < n; i++) {
        activeProfiles.add(args[i]);
      }
      request.setActiveProfiles(activeProfiles);
    }

    plexusContainer.lookup(MavenExecutionRequestPopulator.class).populateFromSettings(request, createSettings(request));
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

  private static DefaultPlexusContainer createPlexusContainer() throws PlexusContainerException {
    ContainerConfiguration mavenCoreCC = new DefaultContainerConfiguration().setClassWorld(new ClassWorld("plexus.core", ClassWorld.class.getClassLoader())).setName("mavenCore");
    mavenCoreCC.setAutoWiring(true);
    return new DefaultPlexusContainer(mavenCoreCC);
  }
}
