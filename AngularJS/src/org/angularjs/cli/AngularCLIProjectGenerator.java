package org.angularjs.cli;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.filters.Filter;
import com.intellij.ide.util.projectWizard.ModuleNameLocationSettings;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.javascript.debugger.execution.JavaScriptDebugConfiguration;
import com.intellij.javascript.debugger.execution.JavascriptDebugConfigurationType;
import com.intellij.javascript.karma.execution.KarmaConfigurationType;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.execution.KarmaRunSettings;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.javascript.protractor.ProtractorConfigurationType;
import com.intellij.javascript.protractor.ProtractorRunConfiguration;
import com.intellij.javascript.protractor.ProtractorRunSettings;
import com.intellij.javascript.protractor.ProtractorUtil;
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator;
import com.intellij.lang.javascript.boilerplate.NpxPackageDescriptor;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmCommand;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmConfigurationType;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfiguration;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.TextAccessor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import com.intellij.xml.util.XmlStringUtil;
import icons.AngularJSIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author Dennis.Ushakov
 */
public class AngularCLIProjectGenerator extends NpmPackageProjectGenerator {

  public static final String PACKAGE_NAME = "@angular/cli";
  private static final Pattern VALID_NG_APP_NAME = Pattern.compile("[a-zA-Z][0-9a-zA-Z]*(-[a-zA-Z][0-9a-zA-Z]*)*");

  @Nls
  @NotNull
  @Override
  public String getName() {
    return "Angular CLI";
  }

  @Override
  @NotNull
  public String getDescription() {
    return "The Angular CLI makes it easy to create an application that already works, right out of the box. It already follows our best practices!";
  }

  @Override
  @NotNull
  public Icon getIcon() {
    return AngularJSIcons.Angular2;
  }

  @Override
  protected void customizeModule(@NotNull VirtualFile baseDir, ContentEntry entry) {
    if (entry != null) {
      AngularJSProjectConfigurator.excludeDefault(baseDir, entry);
    }
  }

  @Override
  @NotNull
  protected String[] generatorArgs(@NotNull Project project, @NotNull VirtualFile baseDir) {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @Override
  @NotNull
  protected String[] generatorArgs(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Settings settings) {
    return new String[]{"new", baseDir.getName()};
  }

  @NotNull
  @Override
  protected Filter[] filters(@NotNull Project project, @NotNull VirtualFile baseDir) {
    return new Filter[]{new AngularCLIFilter(project, baseDir.getParent().getPath())};
  }

  @NotNull
  @Override
  protected String executable(@NotNull NodePackage pkg) {
    return ng(pkg.getSystemDependentPath());
  }

  @NotNull
  public static String ng(String path) {
    return path + File.separator + "bin" + File.separator + "ng";
  }

  @Override
  @NotNull
  protected String packageName() {
    return PACKAGE_NAME;
  }

  @Override
  @NotNull
  protected String presentablePackageName() {
    return "Angular &CLI:";
  }

  @NotNull
  @Override
  protected List<NpxPackageDescriptor.NpxCommand> getNpxCommands() {
    return Collections.singletonList(new NpxPackageDescriptor.NpxCommand(PACKAGE_NAME, "ng"));
  }

  @Override
  protected String validateProjectPath(@NotNull String path) {
    return Optional.ofNullable(validateFolderName(path, "Project"))
                   .orElseGet(() -> super.validateProjectPath(path));
  }

  @SuppressWarnings("deprecation")
  @NotNull
  @Override
  public GeneratorPeer<Settings> createPeer() {
    return new AngularCLIProjectGeneratorPeer();
  }

  @NotNull
  @Override
  protected File workingDir(Settings settings, @NotNull VirtualFile baseDir) {
    return VfsUtilCore.virtualToIoFile(baseDir).getParentFile();
  }


  @NotNull
  @Override
  protected Runnable postInstall(@NotNull Project project,
                                 @NotNull VirtualFile baseDir,
                                 File workingDir) {
    return () -> ApplicationManager.getApplication().executeOnPooledThread(() -> {
      super.postInstall(project, baseDir, workingDir).run();
      createRunConfigurations(project, baseDir);
    });
  }

  @Nullable
  private static String validateFolderName(String path, String label) {
    String fileName = PathUtil.getFileName(path);
    if (!VALID_NG_APP_NAME.matcher(fileName).matches()) {
      return XmlStringUtil.wrapInHtml(
        label + " name '" + fileName + "' is not valid. " + label + " name must " +
        "start with a letter, and must contain only alphanumeric characters or dashes. " +
        "When adding a dash the segment after the dash must also start with a letter."
      );
    }
    return null;
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
    VirtualFile pkg = baseDir.findChild("package.json");
    if (pkg != null && !pkg.isDirectory()) {
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

  private class AngularCLIProjectGeneratorPeer extends NpmPackageGeneratorPeer {

    private TextAccessor myContentRoot;

    @Override
    public void buildUI(@NotNull SettingsStep settingsStep) {
      super.buildUI(settingsStep);
      final ModuleNameLocationSettings field = settingsStep.getModuleNameLocationSettings();
      if (field != null) {
        myContentRoot = new TextAccessor() {
          @Override
          public void setText(@NotNull String text) {
            field.setModuleContentRoot(text);
          }

          @Override
          @NotNull
          public String getText() {
            return field.getModuleContentRoot();
          }
        };
      }
    }

    @Nullable
    @Override
    public ValidationInfo validate() {
      final ValidationInfo info = super.validate();
      if (info != null) {
        return info;
      }
      if (myContentRoot != null) {
        String message = validateFolderName(myContentRoot.getText(), "Content root folder");
        if (message != null) {
          return new ValidationInfo(message);
        }
      }
      return null;
    }
  }
}
