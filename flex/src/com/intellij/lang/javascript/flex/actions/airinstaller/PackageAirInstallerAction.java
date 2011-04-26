package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.LocalFileSystem;

import java.util.List;

public class PackageAirInstallerAction extends DumbAwareAction {

  public void actionPerformed(AnActionEvent e) {
    new PackageAirInstallerDialog(e.getData(PlatformDataKeys.PROJECT)).show();
  }

  public void update(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    e.getPresentation().setEnabled(project != null && ModuleManager.getInstance(project).getModules().length > 0);
  }

  public static boolean createCertificate(final Project project, final Sdk flexSdk, final CertificateParameters parameters) {
    final boolean ok = ExternalTask.runWithProgress(createCertificateTask(project, flexSdk, parameters), "Creating certificate",
                                                    CreateCertificateDialog.TITLE);
    if (ok) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          LocalFileSystem.getInstance().refreshAndFindFileByPath(parameters.getKeystoreFilePath());
        }
      });
    }
    return ok;
  }

  private static ExternalTask createCertificateTask(final Project project, final Sdk flexSdk, final CertificateParameters parameters) {
    return new AdtTask(project, flexSdk) {
      protected void appendAdtOptions(List<String> command) {
        command.add("-certificate");
        command.add("-cn");
        command.add(parameters.getCertificateName());

        if (parameters.getOrgUnit().length() > 0) {
          command.add("-ou");
          command.add(parameters.getOrgUnit());
        }
        if (parameters.getOrgName().length() > 0) {
          command.add("-o");
          command.add(parameters.getOrgName());
        }
        if (parameters.getCountryCode().length() > 0) {
          command.add("-c");
          command.add(parameters.getCountryCode());
        }

        command.add(parameters.getKeyType());
        command.add(parameters.getKeystoreFilePath());
        command.add(parameters.getKeystorePassword());
      }
    };
  }
}
