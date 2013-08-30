package org.osmorc.util;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.util.Properties;

public class MavenIntegrationUtil {
  private static final boolean MAVEN_LOADED;
  static {
    IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("org.jetbrains.idea.maven"));
    MAVEN_LOADED = plugin != null && plugin.isEnabled();
  }

  @NotNull
  public static Properties getMavenProjectProperties(Project project, Module[] affectedModules) {
    Properties properties = new Properties();

    if (MAVEN_LOADED) {
      // IDEA-71307, add maven project properties to scope, in case the project is maven based.
      MavenProjectsManager projectManager = MavenProjectsManager.getInstance(project);
      if (projectManager.isMavenizedProject()) {
        for (Module affectedModule : affectedModules) {
          MavenProject mavenProject = projectManager.findProject(affectedModule);
          if (mavenProject != null) {
            //noinspection UseOfPropertiesAsHashtable
            properties.putAll(mavenProject.getProperties());
          }
        }
      }
    }

    return properties;
  }

  @Nullable
  public static String getMavenProjectPath(Module module) {
    if (MAVEN_LOADED) {
      MavenProjectsManager projectsManager = MavenProjectsManager.getInstance(module.getProject());
      MavenProject project = projectsManager.findProject(module);
      if (project != null) {
        MavenPlugin plugin = project.findPlugin("org.apache.felix", "maven-bundle-plugin");
        if (plugin != null) {
          return VfsUtilCore.pathToUrl(project.getPath());
        }
      }
    }

    return null;
  }
}
