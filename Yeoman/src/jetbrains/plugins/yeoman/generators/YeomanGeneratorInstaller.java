package jetbrains.plugins.yeoman.generators;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DefaultProjectFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.projectGenerator.ui.run.YeomanRunGeneratorForm;
import jetbrains.plugins.yeoman.projectGenerator.util.YeomanCommandLineUtil;
import jetbrains.plugins.yeoman.settings.YeomanNodeFiles;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class YeomanGeneratorInstaller {

  public static final String LOCAL_GENERATORS_RELATIVE_PATH = "extLibs" +
                                                              File.separator +
                                                              "yeoman" +
                                                              File.separator +
                                                              "generators_local";


  public static YeomanGeneratorInstaller getInstance() {
    return new YeomanGeneratorInstaller();
  }

  public File install(YeomanGeneratorInfo info, YeomanNodeFiles files) {
    if (files.getInterpreter() == null) {
      throw new RuntimeException(YeomanBundle.message("yeoman.generator.node.error"));
    }

    final File directory = getModuleDirectory(info);
    FileUtil.createDirectory(new File(directory, "node_modules"));
    Project defaultProject = ProjectManager.getInstance().getDefaultProject();
    final GeneralCommandLine commandLine = YeomanCommandLineUtil.createNpmInstallCommandLine(defaultProject, directory);
    final String infoName = info.getName();
    assert infoName != null;
    commandLine.addParameter(infoName);
    YeomanRunGeneratorForm.LOGGER.debug(commandLine.getCommandLineString());

    final Ref<RuntimeException> exceptionRef = Ref.create(null);
    Runnable action = YeomanCommandLineUtil
      .createExecuteCommandLineAction(DefaultProjectFactory.getInstance().getDefaultProject(),
                                      commandLine, exceptionRef, null);
    if (!ProgressManager.getInstance()
      .runProcessWithProgressSynchronously(action,
                                           YeomanBundle.message("progress.title.yeoman.generator.installation"), true, null)) {
      FileUtil.delete(directory);
      return null;
    }
    final RuntimeException exception = exceptionRef.get();
    if (exception != null) {
      YeomanRunGeneratorForm.LOGGER.debug(exception.getMessage(), exception);
      FileUtil.delete(directory);
      throw exception;
    }

    return directory;
  }


  public boolean uninstall(YeomanInstalledGeneratorInfo installInfo) {
    final File file = new File(FileUtil.toSystemDependentName(installInfo.getFilePath()));
    if (file.exists()) {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
        if (file.exists()) {
          FileUtil.delete(file);
        }
      }, YeomanBundle.message("progress.title.uninstalling.yeoman.generator"), false, null);
    }

    return true;
  }

  private static @NotNull File getModuleDirectory(YeomanGeneratorInfo info) {
    final File file = new File(PathManager.getSystemPath(), LOCAL_GENERATORS_RELATIVE_PATH + File.separator + info.getName());

    FileUtil.createParentDirs(file);
    FileUtil.createDirectory(file);
    return file;
  }
}
