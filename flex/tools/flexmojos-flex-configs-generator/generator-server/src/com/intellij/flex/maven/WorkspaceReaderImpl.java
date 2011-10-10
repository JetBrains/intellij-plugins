package com.intellij.flex.maven;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

import java.io.File;
import java.util.List;

class WorkspaceReaderImpl implements WorkspaceReader {
  private final WorkspaceRepository repository = new WorkspaceRepository("ide", WorkspaceReaderImpl.class);

  @Override
  public WorkspaceRepository getRepository() {
    return repository;
  }

  @Override
  public File findArtifact(Artifact artifact) {
    return null;
  }

  @Override
  public List<String> findVersions(Artifact artifact) {
    return null;
  }
}
