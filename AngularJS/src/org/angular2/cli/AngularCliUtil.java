// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.javascript.JSRunConfigurationBuilder;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.packageJson.PackageJsonGetDependenciesAction;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.intellij.openapi.util.Pair.pair;

public class AngularCliUtil {

  private static final NotificationGroup ANGULAR_CLI_NOTIFICATIONS = new NotificationGroup(
    "Angular CLI", NotificationDisplayType.BALLOON, false, null, AngularJSIcons.Angular2);

  private static final List<String> ANGULAR_JSON_NAMES = ContainerUtil.newArrayList(
    "angular.json", ".angular-cli.json", "angular-cli.json");


  @Nullable
  public static VirtualFile findCliJson(@Nullable VirtualFile dir) {
    if (dir == null) return null;
    for (String name : ANGULAR_JSON_NAMES) {
      VirtualFile cliJson = dir.findChild(name);
      if (cliJson != null) {
        return cliJson;
      }
    }
    return null;
  }

  @Nullable
  public static VirtualFile findAngularCliFolder(@NotNull Project project, @Nullable VirtualFile file) {
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
    NodeJsInterpreter interpreter = NodeJsInterpreterManager.getInstance(project).getInterpreter();
    NodeJsLocalInterpreter node = NodeJsLocalInterpreter.tryCast(interpreter);
    if (node == null) {
      return false;
    }
    List<CompletionModuleInfo> modules = new ArrayList<>();
    NodeModuleSearchUtil.findModulesWithName(modules, AngularCLIProjectGenerator.PACKAGE_NAME, cli,
                                             false, node);
    return !modules.isEmpty() && modules.get(0).getVirtualFile() != null;
  }

  public static boolean isAngularJsonFile(@NotNull String fileName) {
    return ANGULAR_JSON_NAMES.contains(fileName);
  }

  public static void notifyAngularCliNotInstalled(@NotNull Project project, @NotNull VirtualFile cliFolder, @NotNull String message) {
    VirtualFile packageJson = PackageJsonUtil.findChildPackageJsonFile(cliFolder);
    Notification notification = ANGULAR_CLI_NOTIFICATIONS.createNotification(
      message,
      "Required package '@angular/cli' is not installed.",
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
        if (packageJsonPath == null
            || !AngularCliConfigLoader.load(project, baseDir).exists()) {
          return;
        }

        String nameSuffix = ModuleManager.getInstance(project).getModules().length > 1
                            ? " (" + baseDir.getName() + ")" : "";

        createJSDebugConfiguration(project, "Angular Application" + nameSuffix, "http://localhost:4200");
        createKarmaConfiguration(project, baseDir, "Tests" + nameSuffix);
        createProtractorConfiguration(project, baseDir, "E2E Tests" + nameSuffix);
        RunManager.getInstance(project).setSelectedConfiguration(
          createNpmConfiguration(project, packageJsonPath, "Angular CLI Server" + nameSuffix, "start"));
      }));
  }

  @Nullable
  private static String getPackageJson(@NotNull VirtualFile baseDir) {
    VirtualFile pkg = PackageJsonUtil.findChildPackageJsonFile(baseDir);
    if (pkg != null) {
      return pkg.getPath();
    }
    return null;
  }

  private static void createJSDebugConfiguration(@NotNull Project project, @NotNull String label, @NotNull String url) {
    createIfNoSimilar("jsdebug", project, label, null, null, ContainerUtil.newHashMap(
      pair("uri", url)
    ));
  }

  @Nullable
  private static RunnerAndConfigurationSettings createNpmConfiguration(@NotNull Project project,
                                                                       @NotNull String packageJsonPath,
                                                                       @NotNull String label,
                                                                       @NotNull String scriptName) {
    return createIfNoSimilar("npm", project, label, null, packageJsonPath, ContainerUtil.newHashMap(pair("run-script", scriptName)));
  }

  private static void createKarmaConfiguration(@NotNull Project project,
                                               @NotNull VirtualFile baseDir,
                                               @NotNull String label) {
    ObjectUtils.doIfNotNull(
      AngularCliConfigLoader.load(project, baseDir).getKarmaConfigFile(),
      file -> createIfNoSimilar("karma", project, label, baseDir, file.getPath(), ContainerUtil.newHashMap(
        pair("package", findPackage(project, baseDir, "@angular/cli"))
      ))
    );
  }

  private static void createProtractorConfiguration(@NotNull Project project,
                                                    @NotNull VirtualFile baseDir,
                                                    @NotNull String label) {
    ObjectUtils.doIfNotNull(
      AngularCliConfigLoader.load(project, baseDir).getProtractorConfigFile(),
      file -> createIfNoSimilar("protractor", project, label, null, file.getPath(), ContainerUtil.newHashMap(
        pair("global-package", findPackage(project, baseDir, "protractor"))
      ))
    );
  }

  @Nullable
  private static RunnerAndConfigurationSettings createIfNoSimilar(@NotNull String rcType,
                                                                  @NotNull Project project,
                                                                  @NotNull String label,
                                                                  VirtualFile baseDir,
                                                                  String configPath,
                                                                  @NotNull Map<String, Object> options) {
    return ObjectUtils.doIfNotNull(
      JSRunConfigurationBuilder.getForName(rcType, project),
      builder -> ObjectUtils.notNull(
        builder.findSimilarRunConfiguration(baseDir, configPath, options),
        () -> builder.createRunConfiguration(label, baseDir, configPath, options))
    );
  }

  @Nullable
  private static NodePackage findPackage(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull String packageName) {
    NodeJsInterpreter interpreter = NodeJsInterpreterRef.createProjectRef().resolve(project);
    NodePackageDescriptor descr = new NodePackageDescriptor(packageName);
    return descr.listAvailable(project, interpreter, baseDir, true)
                .stream()
                .filter(p -> p.getSystemIndependentPath().startsWith(baseDir.getPath())).findFirst().orElse(null);
  }
}
