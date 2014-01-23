package org.jetbrains.jps.osmorc.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.maven.model.JpsMavenExtensionService;
import org.jetbrains.jps.maven.model.impl.MavenModuleResourceConfiguration;
import org.jetbrains.jps.maven.model.impl.MavenProjectConfiguration;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.build.OsmorcBuildSession;

import java.io.File;
import java.util.Properties;

public class MavenIntegrationUtil {

  @NotNull
  public static Properties getMavenProjectProperties(OsmorcBuildSession session) {
    CompileContext context = session.getContext();

    final BuildDataPaths dataPaths = context.getProjectDescriptor().dataManager.getDataPaths();
    final MavenProjectConfiguration projectConfig =
      JpsMavenExtensionService.getInstance().getMavenProjectConfiguration(dataPaths);

    final Properties result = new Properties();

    session.getExtension().processAffectedModules(new Consumer<JpsModule>() {

      @Override
      public void consume(JpsModule module) {
        MavenModuleResourceConfiguration moduleConfig = projectConfig.moduleConfigurations.get(module.getName());
        if (moduleConfig != null) {
          result.putAll(moduleConfig.properties);
        }
      }
    });

    return result;
  }

  @Nullable
  public static File getMavenProjectPath(OsmorcBuildSession session) {
    final BuildDataPaths dataPaths = session.getContext().getProjectDescriptor().dataManager.getDataPaths();
    final MavenProjectConfiguration projectConfig = JpsMavenExtensionService.getInstance().getMavenProjectConfiguration(dataPaths);

    MavenModuleResourceConfiguration moduleConfig = projectConfig.moduleConfigurations.get(session.getModule().getName());
    if (moduleConfig == null) {
      return null;
    }

    return new File(FileUtil.toSystemDependentName(moduleConfig.directory), "pom.xml");
  }
}
