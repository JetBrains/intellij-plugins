package com.intellij.flex.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.LifecycleExecutionPlanCalculator;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

final class Maven {
  private final HashMap<File, ProjectCacheData> projectsCache = new HashMap<File, ProjectCacheData>();
  private final PlexusContainer plexusContainer;
  private final MavenSession session;

  private final BuildPluginManager pluginManager;
  private final ReentrantLock projectCacheLock = new ReentrantLock();
  private final ProjectBuilder projectBuilder;

  public Maven(PlexusContainer plexusContainer, MavenSession session) throws ComponentLookupException {
    this.plexusContainer = plexusContainer;
    this.session = session;
    pluginManager = plexusContainer.lookup(BuildPluginManager.class);
    projectBuilder = plexusContainer.lookup(ProjectBuilder.class);
  }

  public MavenProject readProject(final File pomFile) throws ComponentLookupException, ProjectBuildingException {
    projectCacheLock.lock();
    ProjectCacheData projectCacheData;
    boolean unlocked = false;
    try {
      projectCacheData = projectsCache.get(pomFile);
      if (projectCacheData != null) {
        projectCacheLock.unlock();
        unlocked = true;
        
        while (projectCacheData.project == null) {
          try {
            Thread.sleep(5);
          }
          catch (InterruptedException e) {
            break;
          }
        }
        return projectCacheData.project;
      }

      projectCacheData = new ProjectCacheData();
      projectsCache.put(pomFile, projectCacheData);
    }
    finally {
      if (!unlocked) {
        projectCacheLock.unlock();
      }
    }

    final ProjectBuildingRequest projectBuildingRequest = session.getRequest().getProjectBuildingRequest();
    projectBuildingRequest.setResolveDependencies(true);

    projectBuildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
    projectBuildingRequest.setRepositorySession(session.getRepositorySession());
    projectCacheData.project = projectBuilder.build(pomFile, projectBuildingRequest).getProject();
    return projectCacheData.project;
  }

  private static final class ProjectCacheData {
    private MavenProject project;
  }

  public MojoExecution createMojoExecution(Plugin plugin, String goal, MavenProject project) throws Exception {
    MojoDescriptor mojoDescriptor = pluginManager.getMojoDescriptor(plugin, goal, project.getRemotePluginRepositories(), session.getRepositorySession());
    List<PluginExecution> executions = plugin.getExecutions();
    MojoExecution mojoExecution = new MojoExecution(mojoDescriptor, executions.isEmpty() ? null : executions.get(executions.size() - 1).getId(), MojoExecution.Source.CLI);
    plexusContainer.lookup(LifecycleExecutionPlanCalculator.class).setupMojoExecution(session, project, mojoExecution);
    return mojoExecution;
  }

  @SuppressWarnings("UnusedParameters")
  public synchronized void releaseMojoExecution(MojoExecution mojoExecution) throws Exception {
  }

  public ClassRealm getPluginRealm(MojoExecution mojoExecution) throws PluginManagerException, PluginResolutionException {
    return pluginManager.getPluginRealm(session, mojoExecution.getMojoDescriptor().getPluginDescriptor());
  }
}
