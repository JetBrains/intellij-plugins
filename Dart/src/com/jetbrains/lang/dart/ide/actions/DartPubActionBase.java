package com.jetbrains.lang.dart.ide.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import icons.DartIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;

/**
 * @author: Fedor.Korotkov
 */
abstract public class DartPubActionBase extends AnAction {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.actions.DartPubActionBase");

  public DartPubActionBase() {
    super(DartIcons.Dart_16);
  }

  @Override
  public void update(AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Presentation presentation = e.getPresentation();

    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);
    final boolean enabled = psiFile instanceof YAMLFile && isEnabled((YAMLFile)psiFile);

    presentation.setVisible(enabled);
    presentation.setEnabled(enabled);

    if (enabled) {
      presentation.setText(getPresentationText());
    }
  }

  @Nls
  protected abstract String getPresentationText();

  protected abstract boolean isEnabled(YAMLFile file);

  protected abstract String getPubCommandArgument();

  protected abstract boolean isOK(@NotNull String output);

  @Override
  public void actionPerformed(final AnActionEvent e) {
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
    Module module = LangDataKeys.MODULE.getData(e.getDataContext());
    if (!(psiFile instanceof YAMLFile) || module == null) {
      Messages.showOkCancelDialog(e.getProject(), DartBundle.message("dart.sdk.bad.dartpub.file"),
                                  DartBundle.message("dart.warning"),
                                  DartIcons.Dart_16);
      return;
    }
    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(psiFile);

    if (virtualFile == null) {
      return;
    }

    final DartSettings settings = DartSettings.getSettingsForModule(module);
    final VirtualFile dartPub = settings == null ? null : settings.getPub();
    if (dartPub == null) {
      Messages.showOkCancelDialog(e.getProject(),
                                  DartBundle.message("dart.sdk.bad.dartpub.path", settings != null ? settings.getPubUrl() : ""),
                                  DartBundle.message("dart.warning"),
                                  DartIcons.Dart_16);
      return;
    }

    new Task.Backgroundable(psiFile.getProject(), "Dart Pub", true) {
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setText("Running pub manager...");
        indicator.setFraction(0.0);
        final GeneralCommandLine command = new GeneralCommandLine();
        command.setExePath(dartPub.getPath());
        command.setWorkDirectory(virtualFile.getParent().getPath());
        command.addParameter(getPubCommandArgument());
        command.getEnvironment().put("DART_SDK", settings.getSdkPath());

        // save on disk
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
          @Override
          public void run() {
            FileDocumentManager.getInstance().saveAllDocuments();
          }
        }, ModalityState.defaultModalityState());


        try {
          final ProcessOutput processOutput = new CapturingProcessHandler(command).runProcess();

          LOG.debug("pub terminated with exit code: " + processOutput.getExitCode());
          LOG.debug(processOutput.getStdout());
          LOG.debug(processOutput.getStderr());

          final String output = processOutput.getStdout().trim();
          final boolean ok = isOK(output);

          if (!ok) {
            Notifications.Bus.notify(new Notification(e.getPresentation().getText(),
                                                      DartBundle.message("dart.pub.title"),
                                                      DartBundle.message("dart.pub.error", output, processOutput.getStderr()),
                                                      NotificationType.WARNING));
          }
          else {
            Notifications.Bus.notify(new Notification(e.getPresentation().getText(),
                                                      DartBundle.message("dart.pub.title"),
                                                      output,
                                                      NotificationType.INFORMATION));
            virtualFile.getParent().refresh(true, false);
          }
        }
        catch (ExecutionException ex) {
          LOG.error(ex);
          Notifications.Bus.notify(new Notification(e.getPresentation().getText(),
                                                    DartBundle.message("dart.pub.title"),
                                                    DartBundle.message("dart.pub.exception", ex.getMessage()),
                                                    NotificationType.ERROR));
        }
        indicator.setFraction(1.0);
      }
    }.setCancelText("Stop").queue();
  }
}
