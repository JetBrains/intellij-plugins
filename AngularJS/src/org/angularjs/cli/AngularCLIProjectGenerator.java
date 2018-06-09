package org.angularjs.cli;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.filters.Filter;
import com.intellij.javascript.debugger.execution.JavaScriptDebugConfiguration;
import com.intellij.javascript.debugger.execution.JavascriptDebugConfigurationType;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator;
import com.intellij.lang.javascript.boilerplate.NpxPackageDescriptor;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmCommand;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmConfigurationType;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunConfiguration;
import com.intellij.lang.javascript.buildTools.npm.rc.NpmRunSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.text.SemVer;
import com.intellij.xml.util.XmlStringUtil;
import icons.AngularJSIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularCLIProjectGenerator extends NpmPackageProjectGenerator {
  public static final String PACKAGE_NAME = "@angular/cli";

  @Nls
  @NotNull
  @Override
  public String getName() {
    return "Angular CLI";
  }

  @Override
  public String getDescription() {
    return "The Angular CLI makes it easy to create an application that already works, right out of the box. It already follows our best practices!";
  }

  @Override
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
    return new String[]{"new", baseDir.getName(), "--dir=."};
  }

  @NotNull
  @Override
  protected Filter[] filters(@NotNull Project project, @NotNull VirtualFile baseDir) {
    return new Filter[]{new AngularCLIFilter(project, baseDir.getPath())};
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
    String fileName = PathUtil.getFileName(path);
    for (String segment : fileName.split("-")) {
      if (!segment.matches("[a-zA-Z][.0-9a-zA-Z]*(-[.0-9a-zA-Z]*)*")) {
        return XmlStringUtil.wrapInHtml(
          "Project name " + fileName + " is not valid. New project names must " +
          "start with a letter, and must contain only alphanumeric characters or dashes. " +
          "When adding a dash the segment after the dash must also start with a letter."
        );
      }
    }
    return super.validateProjectPath(path);
  }

  @NotNull
  @Override
  protected File workingDir(Settings settings, @NotNull VirtualFile baseDir) {
    File workingDir = super.workingDir(settings, baseDir);
    SemVer version = settings.myPackage.getVersion();
    return version == null || version.isGreaterOrEqualThan(6, 0, 0) ?
           workingDir.getParentFile() :
           workingDir;
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

  private static void createRunConfigurations(@NotNull Project project, @NotNull VirtualFile baseDir) {

    if (!project.isDisposed()) {
      RunManager runManager = RunManager.getInstance(project);

      createJSDebugConfiguration(runManager, "Angular Application", "http://localhost:4200");

      createNpmConfiguration(baseDir, runManager, "Angular CLI Server", "start");
      createNpmConfiguration(baseDir, runManager, "Karma Tests", "test");
      createNpmConfiguration(baseDir, runManager, "Protractor E2E Tests", "e2e");
    }
  }

  private static void createJSDebugConfiguration(@NotNull RunManager runManager, @NotNull String label, @NotNull String url) {
    ConfigurationType configurationType = JavascriptDebugConfigurationType.getTypeInstance();
    RunnerAndConfigurationSettings settings =
      runManager.createRunConfiguration(label, configurationType.getConfigurationFactories()[0]);

    JavaScriptDebugConfiguration config = (JavaScriptDebugConfiguration)settings.getConfiguration();
    config.setUri(url);

    runManager.addConfiguration(settings);
  }

  private static void createNpmConfiguration(@NotNull VirtualFile baseDir,
                                             @NotNull RunManager runManager,
                                             @NotNull String label,
                                             @NotNull String scriptName) {
    ConfigurationType configurationType = NpmConfigurationType.getInstance();
    RunnerAndConfigurationSettings settings =
      runManager.createConfiguration(label, configurationType.getConfigurationFactories()[0]);

    NpmRunConfiguration configuration = (NpmRunConfiguration)settings.getConfiguration();
    configuration.setRunSettings(NpmRunSettings
                                   .builder()
                                   .setCommand(NpmCommand.RUN_SCRIPT)
                                   .setScriptNames(Collections.singletonList(scriptName))
                                   .setPackageJsonPath(baseDir.findChild("package.json").getPath())
                                   .build());

    runManager.addConfiguration(settings);
  }

}
