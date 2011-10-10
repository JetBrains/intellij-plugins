package com.intellij.flex.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class Maven {
  private final ConcurrentHashMap<File, MavenProject> projectsCache = new ConcurrentHashMap<File, MavenProject>();
  private final PlexusContainer plexusContainer;
  private final MavenSession session;

  public Maven(PlexusContainer plexusContainer, MavenSession session) {
    this.plexusContainer = plexusContainer;
    this.session = session;
  }

  public MavenProject readProject(final File pomFile) throws ComponentLookupException, ProjectBuildingException {
    MavenProject project = projectsCache.get(pomFile);
    if (project != null) {
      return project;
    }

    final ProjectBuildingRequest projectBuildingRequest = session.getRequest().getProjectBuildingRequest();
    projectBuildingRequest.setResolveDependencies(true);

    projectBuildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
    projectBuildingRequest.setRepositorySession(session.getRepositorySession());
    project = plexusContainer.lookup(ProjectBuilder.class).build(pomFile, projectBuildingRequest).getProject();

    projectsCache.put(pomFile, project);
    return project;
  }
}
