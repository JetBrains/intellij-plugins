package com.intellij.flex.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
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
 */
@Component(role=RepositoryReplicatorMojo.class)
public class RepositoryReplicatorMojo extends AbstractMojo {
  /**
   * @parameter expression="${project}"
   * @readonly
   * @required
   */
  private MavenProject project;

  /**
   * @parameter expression="${outputDirectory}" expression="${outputDirectory}" default-value="${user.dir}/build/repo"
   * @readonly
   * @required
   */
  private File outputDirectory;

  /**
   * @parameter expression="${localRepository}"
   * @required
   * @readonly
   */
  private ArtifactRepository localRepository;

  private final Set<Artifact> copiedArtifacts = new HashSet<Artifact>();
  private final Set<String> extractedConfigs = new HashSet<String>(3);

  @Requirement
  private MavenPluginManager pluginManager;

  @Requirement
  private LegacySupport legacySupport;

  private static boolean compilerLibsCopied;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String packaging = project.getPackaging();

    String localRepositoryBasedir = localRepository.getBasedir();
    File localRepositoryFile = new File(localRepositoryBasedir);
    int localRepositoryBasedirLength = localRepositoryBasedir.length();

    MavenSession session = legacySupport.getSession();
    if (!compilerLibsCopied && packaging.equals("pom")) {
      compilerLibsCopied = true;
      
      try {
        PluginDescriptor pluginDescriptor = pluginManager.getPluginDescriptor(project.getPlugin("org.sonatype.flexmojos:flexmojos-maven-plugin"), session.getCurrentProject().getRemotePluginRepositories(), session.getRepositorySession());
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
    }

    if (!Utils.isFlashProject(project)) {
      return;
    }

    //noinspection ResultOfMethodCallIgnored
    outputDirectory.mkdirs();

    // skip projects artifacts
    for (MavenProject referenceProject : project.getProjectReferences().values()) {
     copiedArtifacts.add(referenceProject.getArtifact());
    }

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
          //continue;
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
