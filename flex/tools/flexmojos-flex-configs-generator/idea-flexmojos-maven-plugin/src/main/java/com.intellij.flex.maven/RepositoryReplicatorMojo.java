package com.intellij.flex.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @goal replicate-repo
 * @requiresDependencyResolution compile
 * @threadSafe
 * @aggregator
 */
@Component(role=RepositoryReplicatorMojo.class)
public class RepositoryReplicatorMojo extends AbstractMojo {
  /**
   * @parameter expression="${session}"
   * @required
   * @readonly
   */
  @SuppressWarnings({"UnusedDeclaration"})
  private MavenSession session;

  /**
   * @parameter expression="${outputDirectory}" expression="${outputDirectory}" default-value="lib"
   * @readonly
   * @required
   */
  @SuppressWarnings({"UnusedDeclaration"})
  private File outputDirectory;

  private final Set<Artifact> copiedArtifacts = new HashSet<Artifact>(64);
  private final Set<String> extractedConfigs = new HashSet<String>();

  @Requirement
  private MavenPluginManager pluginManager;

  /**
   * List of remote repositories to be used by the plugin to resolve dependencies.
   *
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   */
  private List<ArtifactRepository> remoteRepositories;

  @Requirement
  private RepositorySystem repositorySystem;
  private int localRepositoryBasedirLength;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final String localRepositoryBasedir = session.getLocalRepository().getBasedir();
    File localRepositoryFile = new File(localRepositoryBasedir);
    localRepositoryBasedirLength = localRepositoryBasedir.length();

    try {
      final PluginDescriptor pluginDescriptor = pluginManager.getPluginDescriptor(session.getTopLevelProject().getPlugin("org.sonatype.flexmojos:flexmojos-maven-plugin"), session.getCurrentProject().getRemotePluginRepositories(), session.getRepositorySession());
      final File compilerLibsDirectory = new File(outputDirectory, "../build-gant/compiler-libs");
      //noinspection ResultOfMethodCallIgnored
      compilerLibsDirectory.mkdirs();
      for (ComponentDependency dependency : pluginDescriptor.getDependencies()) {
        if (dependency.getGroupId().equals("com.adobe.flex.compiler") && dependency.getType().equals("jar")) {
          final String artifactId = dependency.getArtifactId();
          if (artifactId.equals("adt") || artifactId.equals("asdoc") || artifactId.equals("digest") || artifactId.equals("fcsh") || artifactId.equals("fdb") || artifactId.equals("optimizer") || artifactId.equals("swcdepends")) {
            continue;
          }

          copyIfLastModifiedNotEquals(new File(localRepositoryFile, "com/adobe/flex/compiler/" + artifactId + "/" + dependency.getVersion() + "/" + artifactId + "-" + dependency.getVersion() + ".jar"), new File(compilerLibsDirectory, artifactId + ".jar"));
        }
      }
    }
    catch (Exception e) {
      throw new MojoExecutionException("Cannot find flemxojos maven plugin", e);
    }

    if (outputDirectory.exists()) {
      try {
        FileUtils.deleteDirectory(outputDirectory);
      }
      catch (IOException e) {
        throw new MojoExecutionException("", e);
      }
    }

    //noinspection ResultOfMethodCallIgnored
    outputDirectory.mkdirs();

    for (MavenProject project : session.getProjects()) {
      // skip projects artifacts
      copiedArtifacts.add(project.getArtifact());
      try {
        copyProjectArtifacts(localRepositoryFile, project);
      }
      catch (IOException e) {
        throw new MojoExecutionException("", e);
      }

      for (Plugin plugin : project.getBuildPlugins()) {
        if (plugin.getGroupId().startsWith("org.apache.maven")) {
          continue;
        }

        try {
          resolveAndCopyArtifact(repositorySystem.createPluginArtifact(plugin));
        }
        catch (IOException e) {
          throw new MojoExecutionException("", e);
        }
      }
    }

    for (MavenProject project : session.getProjects()) {
      try {
        copyParentPom(project.getParent());
      }
      catch (IOException e) {
        throw new MojoExecutionException("", e);
      }
    }
  }

  private boolean resolveAndCopyArtifact(Artifact artifact) throws MojoExecutionException, IOException {
    if (copiedArtifacts.contains(artifact)) {
      return true;
    }

    copiedArtifacts.add(artifact);

    File artifactFile = artifact.getFile();
    if (artifactFile == null && !artifact.isResolved()) {
      ArtifactResolutionRequest request = new ArtifactResolutionRequest();
      request.setArtifact(artifact);
      request.setLocalRepository(session.getLocalRepository());
      request.setRemoteRepositories(remoteRepositories);
      ArtifactResolutionResult result = repositorySystem.resolve(request);
      artifactFile = artifact.getFile();
      if (!result.isSuccess() && artifactFile == null) {
        if (getLog().isDebugEnabled()) {
          for (Exception e : result.getExceptions()) {
            getLog().error(e);
          }
        }
        throw new MojoExecutionException("Failed to resolve artifact " + artifact);
      }
    }

    return copyArtifact(artifactFile, artifactFile.getPath().substring(localRepositoryBasedirLength));
  }

  //private void copyFile(String groupId, String artifactId, String version) throws IOException {
  //  String localPath = groupId.replace('.', File.separatorChar) + artifactId + File.separatorChar + version + File.separatorChar + artifactId + '-' + version + ".jar";
  //  copyIfLastModifiedNotEquals(new File(localRepositoryFile, localPath), new File(outputDirectory, localPath));
  //}

  private void copyParentPom(MavenProject project) throws IOException, MojoExecutionException {
    if (project == null) {
      return;
    }

    Artifact artifact = project.getArtifact();
    resolveAndCopyArtifact(artifact);
    copyParentPom(project.getParent());
  }

  private static void copyIfLastModifiedNotEquals(File from, File to) throws IOException {
    if (from.lastModified() != to.lastModified()) {
      Utils.copyFile(from, to);
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void copyProjectArtifacts(File localRepositoryFile, MavenProject project)
    throws MojoExecutionException, IOException {
    for (Artifact artifact : project.getArtifacts()) {
      if (resolveAndCopyArtifact(artifact)) {
        continue;
      }

      final File artifactFile = artifact.getFile();
      final String localPath = artifactFile.getPath().substring(localRepositoryBasedirLength);
      try {
        if (("configs".equals(artifact.getClassifier()) || (artifact.getClassifier() == null && "framework".equals(artifact.getArtifactId()) && artifact.getType().equals("swc"))) && !extractedConfigs.contains(artifact.getVersion())) {
          extractedConfigs.add(artifact.getVersion());
          final File sourceDirectory = new File(artifactFile.getParentFile(), "configs_zip");
          final File destinationDirectory = new File(outputDirectory, artifactFile.getParent().substring(localRepositoryBasedirLength) + "/configs_zip");
          destinationDirectory.mkdirs();
          destinationDirectory.setLastModified(artifactFile.lastModified());

          for (String from : sourceDirectory.list()) {
            // build.xml — published flex sdk contains unneeded ant file
            if (from.charAt(0) != '.' && !from.equals("build.xml")) {
              File fromFile = new File(sourceDirectory, from);
              File toFile = new File(destinationDirectory, from);
              if (fromFile.isDirectory()) {
                toFile.mkdir();
                copyDirectory(fromFile, toFile);
              }
              else {
                Utils.copyFile(fromFile, toFile);
              }
            }
          }

          Utils.copyFile(new File(sourceDirectory, "macFonts.ser"), new File(outputDirectory, "fonts.ser"));
        }
        else if (artifact.getArtifactId().equals("playerglobal") || artifact.getArtifactId().equals("airglobal")) {
          Utils.copyFile(artifactFile, new File(outputDirectory, artifactFile.getParent().substring(localRepositoryBasedirLength) + "/" + artifact.getArtifactId() + ".swc"));
        }
        else if (artifact.getType().equals("rb.swc")) {
          if (artifact.getClassifier() == null) {
           Utils.copyFile(artifactFile, new File(outputDirectory, artifactFile.getPath().substring(localRepositoryBasedirLength, artifactFile.getPath().length() - ".rb.swc".length()) + "-en_US.rb.swc"));
          }
        }
        else if (!artifact.getType().equals("pom")) {
          final String pomFilename = localPath.substring(0, localPath.length() - artifact.getType().length()) + "pom";
          File pom = new File(localRepositoryFile, pomFilename);
          if (pom.exists()) {
            Utils.copyFile(pom, new File(outputDirectory, pomFilename));
          }
        }
      }
      catch (IOException e) {
        throw new MojoExecutionException("Cannot copy", e);
      }
    }
  }

  private boolean copyArtifact(File artifactFile, String localPath) throws IOException {
    File outFile = new File(outputDirectory, localPath);
    if (outFile.lastModified() == artifactFile.lastModified()) {
      return true;
    }

    //noinspection ResultOfMethodCallIgnored
    outFile.getParentFile().mkdirs();
    Utils.copyFile(artifactFile, outFile);
    return false;
  }

  private static void copyDirectory(File sourceDirectory, File destinationDirectory) throws IOException {
    //noinspection ResultOfMethodCallIgnored
    destinationDirectory.setLastModified(sourceDirectory.lastModified());

    for (String from : sourceDirectory.list()) {
      if (from.charAt(0) != '.') {
        final File fromFile = new File(sourceDirectory, from);
        final File toFile = new File(destinationDirectory, from);
        if (fromFile.isDirectory()) {
          //noinspection ResultOfMethodCallIgnored
          toFile.mkdir();
          copyDirectory(fromFile, toFile);
        }
        else {
          Utils.copyFile(fromFile, toFile);
        }
      }
    }
  }
}
