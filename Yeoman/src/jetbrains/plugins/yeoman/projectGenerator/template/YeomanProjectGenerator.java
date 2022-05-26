package jetbrains.plugins.yeoman.projectGenerator.template;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator;
import com.intellij.lang.javascript.ui.NodeModuleNamesUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.platform.DirectoryProjectGeneratorBase;
import icons.YeomanIcons;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorInfo;
import jetbrains.plugins.yeoman.projectGenerator.step.YeomanProjectSettingsStep;
import jetbrains.plugins.yeoman.projectGenerator.ui.run.YeomanRunGeneratorForm;
import jetbrains.plugins.yeoman.projectGenerator.util.YeomanCommandLineUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;


public class YeomanProjectGenerator extends DirectoryProjectGeneratorBase<YeomanProjectGenerator.Settings>
  implements CustomStepProjectGenerator {
  public static final Logger LOGGER = Logger.getInstance(YeomanRunGeneratorForm.class);

  public static final String BOWER_JSON = "bower.json";

  public static class Settings {
    public String tempPath;
    public String appPath;
    public boolean runNpmAndBowerInstall = true;
    public String options;
    public YeomanInstalledGeneratorInfo info;
  }

  @Nls
  @NotNull
  @Override
  public String getName() {
    return YeomanBundle.message("settings.yeoman.name");
  }

  @Nullable
  @Override
  public Icon getLogo() {
    return YeomanIcons.Yeoman;
  }

  @Override
  public void generateProject(@NotNull Project project,
                              @NotNull final VirtualFile baseDir,
                              @NotNull final Settings settings,
                              @NotNull Module module) {
    generateProject(project, baseDir, settings);
  }

  public static void generateProject(@NotNull Project project, @NotNull VirtualFile baseDir, @Nullable final Settings settings) {
    assert settings != null;
    final File baseDirFile = VfsUtilCore.virtualToIoFile(baseDir);
    if (ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {

      try {
        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        indicator.setText(YeomanBundle.message("yeoman.generator.running.process"));

        moveFiles(new File(settings.appPath), baseDirFile);
      }
      catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }, YeomanBundle.message("yeoman.welcome.dialog.after.initialize"), false, project)) {

      if (settings.runNpmAndBowerInstall &&
          new File(baseDirFile, NodeModuleNamesUtil.PACKAGE_JSON).exists()) {
        //run npm install
        installAllDepends(project, baseDirFile);
      }
    }
    
    baseDir.refresh(true, true);
  }


  private static void moveFiles(@NotNull File fromDir, @NotNull File toDir) {
    try {
      File[] files = fromDir.listFiles();
      if (files == null) return;

      boolean success = true;

      ////fast path: rename files
      for (File fromFile : files) {
        File toFile = new File(toDir, fromFile.getName());
        success = fromFile.renameTo(toFile);
        if (!success) {
          break;
        }
      }

      if (success) {

        return;
      }

      //fast pass is failed (problem can be with fs)
      FileUtil.copyDir(fromDir, toDir);
    }
    catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    finally {
      //noinspection ResultOfMethodCallIgnored
      fromDir.delete();
    }
  }

  private static void installAllDepends(@NotNull final Project project, @NotNull final File dir) {
    final GeneralCommandLine npmInstallCommandLine = YeomanCommandLineUtil.createNpmInstallCommandLine(project, dir);
    installDepends(project,
                   npmInstallCommandLine,
                   YeomanBundle.message("yeoman.generator.running.npm"),
                   () -> {
                     if (new File(dir, BOWER_JSON).exists()) {
                       final GeneralCommandLine line = YeomanCommandLineUtil.createBowerInstallCommandLine(project, dir);
                       installDepends(project, line, YeomanBundle.message("yeoman.generator.running.bower"), null);
                     }
                   });
  }

  private static void installDepends(@NotNull final Project project,
                                     @Nullable final GeneralCommandLine line,
                                     @NotNull @Nls String title,
                                     final Runnable success) {
    if (line == null) return;

    ApplicationManager.getApplication().invokeLater(() -> ProgressManager.getInstance().run(new Task.Backgroundable(project, title, true,
                                                                                                                    PerformInBackgroundOption.DEAF) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        YeomanCommandLineUtil.createExecuteCommandLineAction(project, line, new Ref<>(null), indicator).run();
      }

      @Override
      public void onSuccess() {
        if (success != null) {
          success.run();
        }
      }
    }));
  }

  @NotNull
  @Override
  public ValidationResult validate(@NotNull String baseDirPath) {
    return ValidationResult.OK;
  }

  public YeomanProjectGeneratorPanel createPanel() {

    final Settings settings = new Settings();
    settings.tempPath = createTemp().getAbsolutePath();

    return new YeomanProjectGeneratorPanel(settings);
  }

  public static File createTemp() {
    try {
      return FileUtil.createTempDirectory("yeoman-project-generator", null, false);
    }
    catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }


  @Override
  public AbstractActionWithPanel createStep(DirectoryProjectGenerator projectGenerator,
                                            AbstractNewProjectStep.AbstractCallback callback) {
    return new YeomanProjectSettingsStep(projectGenerator, callback);
  }
}
