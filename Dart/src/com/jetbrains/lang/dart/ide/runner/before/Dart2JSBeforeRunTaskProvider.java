package com.jetbrains.lang.dart.ide.runner.before;

import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.process.ScriptRunnerUtil;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.BooleanValueHolder;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.actions.Dart2JSAction;
import com.jetbrains.lang.dart.ide.actions.ui.Dart2JSSettingsDialog;
import com.jetbrains.lang.dart.ide.settings.DartSettingsUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by fedorkorotkov.
 */
public class Dart2JSBeforeRunTaskProvider extends BeforeRunTaskProvider<Dart2JSBeforeRunTask> {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.runner.before.Dart2JSBeforeRunTaskProvider");
  public static final Key<Dart2JSBeforeRunTask> ID = Key.create("Dart2JSBeforeRunTask");

  @Override
  public Key<Dart2JSBeforeRunTask> getId() {
    return ID;
  }

  @Override
  public String getName() {
    return DartBundle.message("dart2js.title");
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return DartIcons.Dart_16;
  }

  @Override
  public String getDescription(Dart2JSBeforeRunTask task) {
    return DartBundle.message("dart2js.task.description", VfsUtil.extractFileName(task.getInputFilePath()));
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Nullable
  @Override
  public Dart2JSBeforeRunTask createTask(RunConfiguration runConfiguration) {
    return new Dart2JSBeforeRunTask(ID);
  }

  @Override
  public boolean configureTask(RunConfiguration runConfiguration, Dart2JSBeforeRunTask task) {
    final Dart2JSSettingsDialog dialog = new Dart2JSSettingsDialog(
      runConfiguration.getProject(),
      task.getInputFilePath(),
      task.getOutputFilePath()
    );
    dialog.show();
    if (!dialog.isOK()) {
      return false;
    }

    task.setInputFilePath(dialog.getInputPath());
    task.setOutputFilePath(dialog.getOutputPath());
    task.setCheckedMode(dialog.isCheckedMode());
    task.setMinifyMode(dialog.isMinify());

    return true;
  }

  @Override
  public boolean canExecuteTask(RunConfiguration configuration, Dart2JSBeforeRunTask task) {
    final String scriptFilePath = task.getInputFilePath();
    return !StringUtil.isEmpty(scriptFilePath) &&
           VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(scriptFilePath)) != null;
  }

  @Override
  public boolean executeTask(DataContext context,
                             final RunConfiguration configuration,
                             ExecutionEnvironment env,
                             final Dart2JSBeforeRunTask task) {
    final VirtualFile dart2js = DartSettingsUtil.getSettings().getDart2JS();
    if (dart2js == null) {
      return false;
    }
    final BooleanValueHolder result = new BooleanValueHolder(true);
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      public void run() {
        new Task.Modal(configuration.getProject(), DartBundle.message("dart2js.title"), false) {
          public void run(@NotNull ProgressIndicator indicator) {
            indicator.setIndeterminate(true);
            indicator.setText(DartBundle.message("dart2js.task.description", VfsUtil.extractFileName(task.getInputFilePath())));
            final GeneralCommandLine command = Dart2JSAction.getCommandLine(
              dart2js,
              task.getInputFilePath(),
              task.getOutputFilePath(),
              task.isCheckedMode(),
              task.isMinifyMode()
            );
            try {
              final String output = ScriptRunnerUtil.getProcessOutput(command);
              final boolean isOK = StringUtil.isEmpty(output);
              result.setValue(isOK);
              if (!isOK) {
                LOG.info(output);
              }

              final VirtualFile outputFile =
                VirtualFileManager.getInstance().refreshAndFindFileByUrl(VfsUtilCore.pathToUrl(task.getOutputFilePath()));
              if (outputFile != null) {
                outputFile.getParent().refresh(true, true);
              }
            }
            catch (ExecutionException e) {
              LOG.warn(e);
            }
          }
        }.queue();
      }
    });
    return result.getValue();
  }
}
