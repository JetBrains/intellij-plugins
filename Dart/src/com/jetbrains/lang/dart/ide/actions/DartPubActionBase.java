package com.jetbrains.lang.dart.ide.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.sdk.DartSdk;
import icons.DartIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

abstract public class DartPubActionBase extends AnAction implements DumbAware {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.actions.DartPubActionBase");
  private static final String GROUP_DISPLAY_ID = "Dart Pub Tool";

  public DartPubActionBase() {
    super(DartIcons.Dart_16);
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setText(getPresentableText());
    final boolean enabled = getModuleAndPubspecYamlFile(e) != null;
    e.getPresentation().setVisible(enabled);
    e.getPresentation().setEnabled(enabled);
  }

  @Nullable
  private static Pair<Module, VirtualFile> getModuleAndPubspecYamlFile(final AnActionEvent e) {
    final Module module = LangDataKeys.MODULE.getData(e.getDataContext());
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(e.getDataContext());

    if (module != null && psiFile != null && psiFile.getName().equalsIgnoreCase(PUBSPEC_YAML)) {
      final VirtualFile file = psiFile.getOriginalFile().getVirtualFile();
      return file != null ? Pair.create(module, file) : null;
    }
    return null;
  }

  @Nls
  protected abstract String getPresentableText();

  protected abstract String getPubCommand();

  protected abstract String getSuccessMessage();

  @Override
  public void actionPerformed(final AnActionEvent e) {
    final Pair<Module, VirtualFile> moduleAndPubspecYamlFile = getModuleAndPubspecYamlFile(e);
    if (moduleAndPubspecYamlFile == null) return;

    DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) {
      final int answer = Messages.showDialog(moduleAndPubspecYamlFile.first.getProject(), "Dart SDK is not configured",
                                             getPresentableText(), new String[]{"Configure SDK", "Cancel"}, 0, Messages.getErrorIcon());
      if (answer != 0) return;

      ShowSettingsUtil.getInstance().showSettingsDialog(moduleAndPubspecYamlFile.first.getProject(), DartBundle.message("dart.title"));

      sdk = DartSdk.getGlobalDartSdk();
      if (sdk == null) return;
    }

    File pubFile = new File(sdk.getHomePath() + (SystemInfo.isWindows ? "/bin/pub.bat" : "/bin/pub"));
    if (!pubFile.isFile()) {
      final int answer =
        Messages.showDialog(moduleAndPubspecYamlFile.first.getProject(), DartBundle.message("dart.sdk.bad.dartpub.path", pubFile.getPath()),
                            getPresentableText(), new String[]{"Configure SDK", "Cancel"}, 0, Messages.getErrorIcon());
      if (answer != 0) return;

      ShowSettingsUtil.getInstance().showSettingsDialog(moduleAndPubspecYamlFile.first.getProject(), DartBundle.message("dart.title"));

      sdk = DartSdk.getGlobalDartSdk();
      if (sdk == null) return;

      pubFile = new File(sdk.getHomePath() + (SystemInfo.isWindows ? "/bin/pub.bat" : "/bin/pub"));
      if (!pubFile.isFile()) return;
    }

    doExecute(moduleAndPubspecYamlFile.first, moduleAndPubspecYamlFile.second, sdk.getHomePath(), pubFile.getPath());
  }

  private void doExecute(final Module module, final VirtualFile pubspecYamlFile, final String sdkPath, final String pubPath) {
    final Task.Backgroundable task = new Task.Backgroundable(module.getProject(), getPresentableText(), true) {
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setText(DartBundle.message("dart.pub.0.in.progress", getPubCommand()));
        indicator.setIndeterminate(true);
        final GeneralCommandLine command = new GeneralCommandLine();
        command.setExePath(pubPath);
        command.setWorkDirectory(pubspecYamlFile.getParent().getPath());
        command.addParameter(getPubCommand());
        command.getEnvironment().put("DART_SDK", sdkPath);

        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
          @Override
          public void run() {
            FileDocumentManager.getInstance().saveAllDocuments();
          }
        }, ModalityState.defaultModalityState());


        try {
          final ProcessOutput processOutput = new CapturingProcessHandler(command).runProcess();
          final String err = processOutput.getStderr().trim();

          LOG.debug("pub " + getPubCommand() + ", exit code: " + processOutput.getExitCode() + ", err:\n" +
                    err + "\nout:\n" + processOutput.getStdout());

          if (err.isEmpty()) {
            Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, getPresentableText(), getSuccessMessage(),
                                                      NotificationType.INFORMATION));
          }
          else {
            Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, getPresentableText(), err, NotificationType.ERROR));
          }

          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              DartProjectComponent.excludePackagesFolders(module, pubspecYamlFile);
            }
          });
        }
        catch (ExecutionException ex) {
          LOG.error(ex);
          Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, getPresentableText(),
                                                    DartBundle.message("dart.pub.exception", ex.getMessage()),
                                                    NotificationType.ERROR));
        }
      }
    };

    task.queue();
  }
}
