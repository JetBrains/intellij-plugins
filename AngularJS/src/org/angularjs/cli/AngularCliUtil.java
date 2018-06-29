package org.angularjs.cli;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.javascript.debugger.execution.JavaScriptDebugConfiguration;
import com.intellij.javascript.debugger.execution.JavascriptDebugConfigurationType;
import com.intellij.javascript.karma.execution.KarmaConfigurationType;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.execution.KarmaRunSettings;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.packageJson.PackageJsonGetDependenciesAction;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.javascript.protractor.ProtractorConfigurationType;
import com.intellij.javascript.protractor.ProtractorRunConfiguration;
import com.intellij.javascript.protractor.ProtractorRunSettings;
import com.intellij.javascript.protractor.ProtractorUtil;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmCommand;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmConfigurationType;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfiguration;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunSettings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import icons.AngularJSIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class AngularCliUtil {

  private static final NotificationGroup ANGULAR_CLI_NOTIFICATIONS = new NotificationGroup(
    "Angular CLI", NotificationDisplayType.BALLOON, false, null, AngularJSIcons.Angular2);

  private static final List<String> ANGULAR_JSON_NAMES = ContainerUtil.newArrayList(
    "angular.json", ".angular-cli.json", "angular-cli.json");


  @Nullable
  public static VirtualFile findCliJson(@Nullable VirtualFile dir) {
    if (dir == null) return null;
    for (String name: ANGULAR_JSON_NAMES) {
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
        RunManager runManager = RunManager.getInstance(project);

        String packageJsonPath = getPackageJson(baseDir);
        if (packageJsonPath == null) {
          return;
        }

        String nameSuffix = ModuleManager.getInstance(project).getModules().length > 1
                            ? " (" + baseDir.getName() + ")" : "";

        createJSDebugConfiguration(runManager, "Angular Application" + nameSuffix, "http://localhost:4200");
        createKarmaConfiguration(project, baseDir, runManager, "Tests" + nameSuffix);
        createProtractorConfiguration(project, baseDir, runManager, "E2E Tests" + nameSuffix);
        runManager.setSelectedConfiguration(
          createNpmConfiguration(packageJsonPath, runManager, "Angular CLI Server" + nameSuffix, "start"));
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

  private static void createJSDebugConfiguration(@NotNull RunManager runManager, @NotNull String label, @NotNull String url) {
    createRunConfig(runManager, label, JavascriptDebugConfigurationType.getTypeInstance(),
                    (JavaScriptDebugConfiguration config) -> url.equals(config.getUri()),
                    (JavaScriptDebugConfiguration config) -> config.setUri(url));
  }

  @NotNull
  private static RunnerAndConfigurationSettings createNpmConfiguration(@NotNull String packageJsonPath,
                                                                       @NotNull RunManager runManager,
                                                                       @NotNull String label,
                                                                       @NotNull String scriptName) {
    NpmRunSettings runSettings = NpmRunSettings
      .builder()
      .setCommand(NpmCommand.RUN_SCRIPT)
      .setScriptNames(Collections.singletonList(scriptName))
      .setPackageJsonPath(packageJsonPath)
      .build();
    return createRunConfig(runManager, label, NpmConfigurationType.getInstance(),
                           (NpmRunConfiguration config) -> similar(config.getRunSettings(), runSettings),
                           (NpmRunConfiguration config) -> config.setRunSettings(runSettings));
  }

  private static void createKarmaConfiguration(@NotNull Project project,
                                               @NotNull VirtualFile baseDir,
                                               @NotNull RunManager runManager,
                                               @NotNull String label) {
    KarmaRunSettings runSettings = new KarmaRunSettings.Builder()
      .setKarmaPackage(findPackage(project, baseDir, KarmaUtil.ANGULAR_CLI__PACKAGE_NAME))
      .setConfigPath(findConfigFile(project, baseDir, KarmaUtil::listPossibleConfigFilesInProject))
      .setWorkingDirectory(baseDir.getPath())
      .build();
    createRunConfig(runManager, label, KarmaConfigurationType.getInstance(),
                    (KarmaRunConfiguration config) -> similar(config.getRunSettings(), runSettings),
                    (KarmaRunConfiguration config) -> config.setRunSettings(runSettings)
    );
  }

  private static void createProtractorConfiguration(@NotNull Project project,
                                                    @NotNull VirtualFile baseDir,
                                                    @NotNull RunManager runManager,
                                                    @NotNull String label) {
    ProtractorRunSettings runSettings = new ProtractorRunSettings.Builder()
      .setConfigFilePath(StringUtil.defaultIfEmpty(
        findConfigFile(project, baseDir, ProtractorUtil::listPossibleConfigFilesInProject), ""))
      .build();
    createRunConfig(runManager, label, ProtractorConfigurationType.getInstance(),
                    (ProtractorRunConfiguration config) -> similar(config.getRunSettings(), runSettings),
                    (ProtractorRunConfiguration config) -> config.setRunSettings(runSettings)
    );
    Optional.ofNullable(findPackage(project, baseDir, ProtractorUtil.PACKAGE_NAME))
            .ifPresent(pkg -> ProtractorUtil.setProtractorPackage(project, pkg));
  }

  private static boolean similar(@NotNull ProtractorRunSettings s1, @NotNull ProtractorRunSettings s2) {
    return s1.getConfigFileSystemDependentPath().equals(s2.getConfigFileSystemDependentPath());
  }

  private static boolean similar(@NotNull KarmaRunSettings s1, @NotNull KarmaRunSettings s2) {
    return s1.getConfigPathSystemDependent().equals(s2.getConfigPathSystemDependent())
           && Objects.equals(s1.getKarmaPackage(), s2.getKarmaPackage());
  }

  private static boolean similar(@NotNull NpmRunSettings s1, @NotNull NpmRunSettings s2) {
    return s1.getCommand().equals(s2.getCommand())
           && s1.getScriptNames().equals(s2.getScriptNames())
           && s1.getPackageJsonSystemDependentPath().equals(s2.getPackageJsonSystemDependentPath());
  }

  @SuppressWarnings("unchecked")
  @NotNull
  private static <T> RunnerAndConfigurationSettings createRunConfig(@NotNull RunManager runManager,
                                                                    @NotNull String label,
                                                                    @NotNull ConfigurationType configurationType,
                                                                    @NotNull Predicate<T> isSimilar,
                                                                    @NotNull Consumer<T> configurator) {
    return runManager
      .getConfigurationSettingsList(configurationType)
      .stream()
      .filter(settings -> isSimilar.test((T)settings.getConfiguration()))
      .findFirst()
      .orElseGet(() -> {
        RunnerAndConfigurationSettings settings =
          runManager.createConfiguration(label, configurationType.getConfigurationFactories()[0]);

        configurator.consume((T)settings.getConfiguration());

        runManager.addConfiguration(settings);
        return settings;
      });
  }

  @Nullable
  private static NodePackage findPackage(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull String packageName) {
    NodeJsInterpreter interpreter = NodeJsInterpreterRef.createProjectRef().resolve(project);
    NodePackageDescriptor descr = new NodePackageDescriptor(packageName);
    return descr.listAvailable(project, interpreter, baseDir, true)
                .stream()
                .filter(p -> p.getSystemIndependentPath().startsWith(baseDir.getPath())).findFirst().orElse(null);
  }

  @Nullable
  private static String findConfigFile(@NotNull Project project,
                                       @NotNull VirtualFile baseDir,
                                       Function<Project, List<VirtualFile>> listProvider) {
    return listProvider.apply(project)
                       .stream()
                       .filter(f -> f.getPath().startsWith(baseDir.getPath()))
                       .findFirst()
                       .map(f -> f.getPath())
                       .orElse(null);
  }
}
