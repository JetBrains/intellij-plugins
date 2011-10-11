package com.intellij.flex.maven;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.sonatype.aether.RepositorySystemSession;

class ThreadSafeMavenSession extends MavenSession {
  private final ThreadLocal<MavenProject> threadLocalCurrentProject = new ThreadLocal<MavenProject>();

  public ThreadSafeMavenSession(PlexusContainer container, RepositorySystemSession repositorySession, MavenExecutionRequest request,
                                MavenExecutionResult result) {
    super(container, repositorySession, request, result);
    setParallel(true);
  }

  @Override
  public void setCurrentProject(MavenProject currentProject) {
    threadLocalCurrentProject.set(currentProject);
  }

  @Override
  public MavenProject getCurrentProject() {
    return threadLocalCurrentProject.get();
  }
}
