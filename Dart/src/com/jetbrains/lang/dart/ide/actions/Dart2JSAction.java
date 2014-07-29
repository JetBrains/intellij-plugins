package com.jetbrains.lang.dart.ide.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ScriptRunnerUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.actions.ui.Dart2JSSettingsDialog;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

public class Dart2JSAction extends AnAction {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.actions.Dart2JSAction");

  public Dart2JSAction() {
    super(DartIcons.Dart_16);
  }

  @Override
  public void update(AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Presentation presentation = e.getPresentation();

    final boolean enabled = CommonDataKeys.PSI_FILE.getData(dataContext) instanceof DartFile &&
                            DartSdk.getGlobalDartSdk() != null;

    presentation.setVisible(enabled);
    presentation.setEnabled(enabled);
  }

  @Override
  public void actionPerformed(final AnActionEvent e) {
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
    if (!(psiFile instanceof DartFile)) {
      return;
    }
    VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(psiFile);
    if (virtualFile == null) {
      return;
    }
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) return;

    final String jsFilePath = virtualFile.getPath() + ".js";
    final Dart2JSSettingsDialog dialog = new Dart2JSSettingsDialog(psiFile.getProject(), virtualFile.getPath(), jsFilePath);
    dialog.disableInput();
    dialog.show();
    if (!dialog.isOK()) {
      return;
    }

    new Task.Backgroundable(psiFile.getProject(), "dart2js", false) {
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setText("Running dart2js...");
        indicator.setFraction(0.0);
        final GeneralCommandLine command = getCommandLine(
          DartSdkUtil.getDart2jsPath(sdk),
          dialog.getInputPath(),
          dialog.getOutputPath(),
          dialog.isCheckedMode(),
          dialog.isMinify()
        );

        // save on disk
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
          @Override
          public void run() {
            FileDocumentManager.getInstance().saveAllDocuments();
          }
        }, ModalityState.defaultModalityState());

        try {
          final String output = ScriptRunnerUtil.getProcessOutput(command);

          final VirtualFile folder = LocalFileSystem.getInstance().findFileByPath(PathUtil.getParentPath(dialog.getInputPath()));
          if (folder != null) {
            folder.refresh(true, false);
          }

          ProgressManager.progress("");
          LOG.debug(output);
          boolean error = !output.isEmpty();
          if (error) {
            Notifications.Bus.notify(new Notification(e.getPresentation().getText(),
                                                      DartBundle.message("dart2js.title"),
                                                      DartBundle.message("dart2js.js.file.creation.error", output),
                                                      NotificationType.ERROR));
            return;
          }
          Notifications.Bus.notify(new Notification(e.getPresentation().getText(),
                                                    DartBundle.message("dart2js.title"),
                                                    DartBundle.message("dart2js.js.file.created", jsFilePath),
                                                    NotificationType.INFORMATION));
        }
        catch (ExecutionException ex) {
          LOG.error(ex);
          Notifications.Bus.notify(new Notification(e.getPresentation().getText(),
                                                    DartBundle.message("dart2js.title"),
                                                    DartBundle.message("dart2js.js.file.creation.error", ex.getMessage()),
                                                    NotificationType.ERROR));
        }
        indicator.setFraction(1.0);
      }
    }.queue();
  }

  public static GeneralCommandLine getCommandLine(final String dart2jsPath,
                                                  final String inputPath,
                                                  final String outputPath,
                                                  final boolean checkedMode,
                                                  final boolean minifyMode) {
    final GeneralCommandLine command = new GeneralCommandLine();
    command.setExePath(dart2jsPath);
    if (checkedMode) {
      command.addParameter(DartiumUtil.CHECKED_MODE_OPTION);
    }
    if (minifyMode) {
      command.addParameter("--minify");
    }
    command.addParameter("--out=" + outputPath);
    command.addParameter(inputPath);
    return command;
  }
}
