package com.intellij.flex.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
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

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    String packaging = project.getPackaging();
    if (!(packaging.equals("swc") || packaging.equals("swf"))) {
      return;
    }

    String localRepositoryBasedir = localRepository.getBasedir();
    File localRepositoryFile = new File(localRepositoryBasedir);
    int localRepositoryBasedirLength = localRepositoryBasedir.length();

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
          continue;
        }

        //noinspection ResultOfMethodCallIgnored
        outFile.getParentFile().mkdirs();
        
        copyFile(artifactFile, outFile);

        if ("configs".equals(artifact.getClassifier())) {
          FileUtils.copyDirectory(new File(artifactFile.getParentFile(), "configs_zip"), new File(outputDirectory, artifactFile.getParent().substring(localRepositoryBasedirLength) + "/configs_zip"));
        }
        else if (artifact.getArtifactId().equals("playerglobal") || artifact.getArtifactId().equals("airglobal")) {
          copyFile(artifactFile, new File(outputDirectory, artifactFile.getParent().substring(localRepositoryBasedirLength) + "/" + artifact.getArtifactId() + ".swc"));
        }
        else if (!artifact.getType().equals("pom")) {
          final String pomFilename = localPath.substring(0, localPath.length() - artifact.getType().length()) + "pom";
          File pom = new File(localRepositoryFile, pomFilename);
          if (pom.exists()) {
            copyFile(pom, new File(outputDirectory, pomFilename));
          }
        }
      }
      catch (IOException e) {
        throw new MojoExecutionException("Cannot copy", e);
      }
    }
  }

  private void copyFile(File fromFile, File toFile) throws MojoExecutionException, IOException {
    final FileChannel fromChannel = new FileInputStream(fromFile).getChannel();
    final FileChannel toChannel = new FileOutputStream(toFile).getChannel();
    try {
      fromChannel.transferTo(0, fromFile.length(), toChannel);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Cannot copy", e);
    }
    finally {
      fromChannel.close();
      toChannel.close();
    }
  }
}
