package com.intellij.flex.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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
   * @parameter expression="${outputDirectory}" expression="${outputDirectory}" default-value="build/repo"
   * @readonly
   * @required
   */
  @SuppressWarnings({"UnusedDeclaration"})
  private File outputDirectory;

  private final Set<Artifact> copiedArtifacts = new HashSet<Artifact>(64);
  private final Set<String> extractedConfigs = new HashSet<String>();

  @Requirement
  private MavenPluginManager pluginManager;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String localRepositoryBasedir = session.getLocalRepository().getBasedir();
    File localRepositoryFile = new File(localRepositoryBasedir);
    int localRepositoryBasedirLength = localRepositoryBasedir.length();

    try {
      PluginDescriptor pluginDescriptor = pluginManager.getPluginDescriptor(session.getTopLevelProject().getPlugin("org.sonatype.flexmojos:flexmojos-maven-plugin"), session.getCurrentProject().getRemotePluginRepositories(), session.getRepositorySession());
      final File compilerLibsDirectory = new File(outputDirectory, "../../build-gant/compiler-libs");
      //noinspection ResultOfMethodCallIgnored
      compilerLibsDirectory.mkdirs();
      for (ComponentDependency dependency : pluginDescriptor.getDependencies()) {
        if (dependency.getGroupId().equals("com.adobe.flex.compiler") && dependency.getType().equals("jar")) {
          final String artifactId = dependency.getArtifactId();
          if (artifactId.equals("adt") || artifactId.equals("asdoc") || artifactId.equals("digest") || artifactId.equals("fcsh") || artifactId.equals("fdb") || artifactId.equals("optimizer") || artifactId.equals("swcdepends")) {
            continue;
          }

          Utils.copyFile(new File(localRepositoryFile, "com/adobe/flex/compiler/" + artifactId + "/" + dependency.getVersion() + "/" + artifactId + "-" + dependency.getVersion() + ".jar"), new File(compilerLibsDirectory, artifactId + ".jar"));
        }
      }
    }
    catch (Exception e) {
      throw new MojoExecutionException("Cannot find flemxojos maven plugin", e);
    }

    //noinspection ResultOfMethodCallIgnored
    outputDirectory.mkdirs();

    for (MavenProject project : session.getProjects()) {
      if (!(project.getPackaging().equals("swf") || project.getPackaging().equals("swc"))) {
        continue;
      }

      // skip projects artifacts
      copiedArtifacts.add(project.getArtifact());
      copyProjectArtifacts(localRepositoryFile, localRepositoryBasedirLength, project);
    }
  }

  private void copyProjectArtifacts(File localRepositoryFile, int localRepositoryBasedirLength, MavenProject project) throws MojoExecutionException {
    for (Artifact artifact : project.getArtifacts()) {
      if (copiedArtifacts.contains(artifact)) {
        continue;
      }

      copiedArtifacts.add(artifact);

      final File artifactFile = artifact.getFile();
      final String localPath = artifactFile.getPath().substring(localRepositoryBasedirLength);
      try {
        File outFile = new File(outputDirectory, localPath);
        if (outFile.lastModified() == artifactFile.lastModified()) {
          continue;
        }

        //noinspection ResultOfMethodCallIgnored
        outFile.getParentFile().mkdirs();

        Utils.copyFile(artifactFile, outFile);

        if (("configs".equals(artifact.getClassifier()) || (artifact.getClassifier() == null && "framework".equals(artifact.getArtifactId()) && artifact.getType().equals("swc"))) && !extractedConfigs.contains(artifact.getVersion())) {
          extractedConfigs.add(artifact.getVersion());
          final File in = new File(artifactFile.getParentFile(), "configs_zip");
          FileUtils.copyDirectory(in, new File(outputDirectory, artifactFile.getParent().substring(localRepositoryBasedirLength) + "/configs_zip"));
          Utils.copyFile(new File(in, "macFonts.ser"), new File(outputDirectory, "fonts.ser"));
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
}
