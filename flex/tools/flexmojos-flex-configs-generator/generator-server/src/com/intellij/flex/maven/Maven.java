package com.intellij.flex.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.LifecycleExecutionPlanCalculator;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.version.DefaultPluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class Maven {
  private final ConcurrentMap<File, AtomicNotNullLazyValue<MavenProject>> projectsCache = new ConcurrentHashMap<File, AtomicNotNullLazyValue<MavenProject>>();
  private final PlexusContainer plexusContainer;
  private final MavenSession session;

  private final BuildPluginManager pluginManager;
  private final ProjectBuilder projectBuilder;
  private final Date buildStartDate = new Date();

  public Maven(PlexusContainer plexusContainer, MavenSession session) throws ComponentLookupException {
    this.plexusContainer = plexusContainer;
    this.session = session;
    pluginManager = plexusContainer.lookup(BuildPluginManager.class);
    projectBuilder = plexusContainer.lookup(ProjectBuilder.class);
  }

  /**
   * Copied from com.intellij.openapi.util.*
   */
  private abstract class AtomicNotNullLazyValue<T> {
    private volatile T myValue;

    protected abstract T compute();

    public final T getValue() {
      T value = myValue;
      if (value != null) {
        return value;
      }
      synchronized (this) {
        value = myValue;
        if (value == null) {
          myValue = value = compute();
        }
      }
      return value;
    }
  }

  // @Nullable return null if cannot read
  public MavenProject readProject(final File pomFile, final Logger logger) {
    AtomicNotNullLazyValue<MavenProject> projectRef = projectsCache.get(pomFile);
    if (projectRef == null) {
      AtomicNotNullLazyValue<MavenProject> candidate =
        projectsCache.putIfAbsent(pomFile, projectRef = new AtomicNotNullLazyValue<MavenProject>() {
          @Override
          protected MavenProject compute() {
            ProjectBuildingRequest projectBuildingRequest = session.getRequest().getProjectBuildingRequest();
            projectBuildingRequest.setResolveDependencies(true);

            projectBuildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            projectBuildingRequest.setRepositorySession(session.getRepositorySession());
            projectBuildingRequest.setBuildStartTime(buildStartDate);

            try {
              return projectBuilder.build(pomFile, projectBuildingRequest).getProject();
            }
            catch (Throwable e) {
              logger.error("Cannot read project " + pomFile.getPath(), e);
              return null;
            }
          }
        });
      if (candidate != null) {
        projectRef = candidate;
      }
    }
    return projectRef.getValue();
  }

  public MojoExecution createMojoExecution(Plugin plugin, String goal, MavenProject project) throws Exception {
    if (plugin.getVersion() == null) {
      plugin.setVersion(plexusContainer.lookup(PluginVersionResolver.class).resolve(new DefaultPluginVersionRequest(plugin, session)).getVersion());
    }

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