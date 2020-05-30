// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.javascript.JSRunConfigurationBuilder;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.packageJson.notification.PackageJsonGetDependenciesAction;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import icons.AngularJSIcons;
import one.util.streamex.StreamEx;
import org.angular2.cli.config.AngularConfig;
import org.angular2.cli.config.AngularConfigProvider;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static org.angular2.lang.Angular2LangUtil.ANGULAR_CLI_PACKAGE;

public class AngularCliUtil {

  private static final NotificationGroup ANGULAR_CLI_NOTIFICATIONS =
    new NotificationGroup("Angular CLI", NotificationDisplayType.BALLOON, false, null, AngularJSIcons.Angular2,
                          Angular2Bundle.message("angular.description.angular-cli"), null);

  @NonNls private static final List<String> ANGULAR_JSON_NAMES = ContainerUtil.newArrayList(
    "angular.json", ".angular-cli.json", "angular-cli.json");
  @NonNls private static final String NG_CLI_DEFAULT_ADDRESS = "http://localhost:4200";


  public static @Nullable VirtualFile findCliJson(@Nullable VirtualFile dir) {
    if (dir == null || !dir.isValid()) return null;
    for (String name : ANGULAR_JSON_NAMES) {
      VirtualFile cliJson = dir.findChild(name);
      if (cliJson != null) {
        return cliJson;
      }
    }
    return null;
  }

  public static @Nullable VirtualFile findAngularCliFolder(@NotNull Project project, @Nullable VirtualFile file) {
    VirtualFile current = file;
    while (current != null) {
      if (current.isDirectory() && findCliJson(current) != null) return current;
      current = current.getParent();
    }
    if (findCliJson(project.getBaseDir()) != null) {
      return project.getBaseDir();
    }
    return null;
  }

  public static boolean hasAngularCLIPackageInstalled(@NotNull Project project, @NotNull VirtualFile cli) {
    List<CompletionModuleInfo> modules = new ArrayList<>();
    NodeModuleSearchUtil.findModulesWithName(modules, ANGULAR_CLI_PACKAGE, cli, null);
    return !modules.isEmpty() && modules.get(0).getVirtualFile() != null;
  }

  public static boolean isAngularJsonFile(@NotNull String fileName) {
    return ANGULAR_JSON_NAMES.contains(fileName);
  }

  public static void notifyAngularCliNotInstalled(@NotNull Project project, @NotNull VirtualFile cliFolder, @NotNull String message) {
    VirtualFile packageJson = PackageJsonUtil.findChildPackageJsonFile(cliFolder);
    Notification notification = ANGULAR_CLI_NOTIFICATIONS.createNotification(
      message,
      Angular2Bundle.message("angular.notify.cli.required-package-not-installed"),
      NotificationType.WARNING, null);
    if (packageJson != null) {
      notification.addAction(new PackageJsonGetDependenciesAction(project, packageJson, notification));
    }
    notification.notify(project);
  }

  public static void createRunConfigurations(@NotNull Project project, @NotNull VirtualFile baseDir) {
    ApplicationManager.getApplication().executeOnPooledThread(
      () -> DumbService.getInstance(project).runReadActionInSmartMode(() -> {
        if (project.isDisposed()) {
          return;
        }

        String packageJsonPath = getPackageJson(baseDir);
        AngularConfig config;
        if (packageJsonPath == null
            || (config = AngularConfigProvider.getAngularConfig(project, baseDir)) == null) {
          return;
        }

        createKarmaConfigurations(project, config);
        createProtractorConfigurations(project, config);

        String nameSuffix = ModuleManager.getInstance(project).getModules().length > 1
                            ? " (" + baseDir.getName() + ")" : "";

        createJSDebugConfiguration(project, "Angular Application" + nameSuffix, NG_CLI_DEFAULT_ADDRESS);
        RunManager.getInstance(project).setSelectedConfiguration(
          createNpmConfiguration(project, packageJsonPath, "Angular CLI Server" + nameSuffix, "start"));
      }));
  }

  private static @Nullable String getPackageJson(@NotNull VirtualFile baseDir) {
    VirtualFile pkg = PackageJsonUtil.findChildPackageJsonFile(baseDir);
    if (pkg != null) {
      return pkg.getPath();
    }
    return null;
  }

  private static void createJSDebugConfiguration(@NotNull Project project, @NotNull @NonNls String label, @NotNull String url) {
    createIfNoSimilar("jsdebug", project, label, null, null, ContainerUtil.newHashMap(
      pair("uri", url)
    ));
  }

  private static @Nullable RunnerAndConfigurationSettings createNpmConfiguration(@NotNull Project project,
                                                                                 @NotNull String packageJsonPath,
                                                                                 @NotNull @NonNls String label,
                                                                                 @NotNull String scriptName) {
    //noinspection HardCodedStringLiteral
    return createIfNoSimilar("npm", project, label, null, packageJsonPath,
                             ContainerUtil.newHashMap(pair("run-script", scriptName)));
  }

  private static void createKarmaConfigurations(@NotNull Project project,
                                                @NotNull AngularConfig config) {
    StreamEx.of(config.getProjects())
      .filter(ngProject -> ngProject.getKarmaConfigFile() != null
                           && ngProject.getRootDir() != null)
      .forEach(ngProject -> createIfNoSimilar("karma", project, "Tests (" + ngProject.getName() + ")",
                                              ngProject.getRootDir(), ngProject.getKarmaConfigFile().getPath(),
                                              Collections.emptyMap())
      );
  }

  private static void createProtractorConfigurations(@NotNull Project project,
                                                     @NotNull AngularConfig config) {
    StreamEx.of(config.getProjects())
      .filter(ngProject -> ngProject.getProtractorConfigFile() != null
                           && ngProject.getRootDir() != null)
      .forEach(ngProject -> createIfNoSimilar("protractor", project, "E2E Tests (" + ngProject.getName() + ")",
                                              ngProject.getRootDir(), ngProject.getProtractorConfigFile().getPath(),
                                              Collections.emptyMap()));
  }

  private static @Nullable RunnerAndConfigurationSettings createIfNoSimilar(@NotNull @NonNls String rcType,
                                                                            @NotNull Project project,
                                                                            @NonNls @NotNull String label,
                                                                            VirtualFile baseDir,
                                                                            String configPath,
                                                                            @NotNull Map<String, Object> options) {
    return doIfNotNull(
      JSRunConfigurationBuilder.getForName(rcType, project),
      builder -> ObjectUtils.notNull(
        builder.findSimilarRunConfiguration(baseDir, configPath, options),
        () -> builder.createRunConfiguration(label, baseDir, configPath, options))
    );
  }
}
