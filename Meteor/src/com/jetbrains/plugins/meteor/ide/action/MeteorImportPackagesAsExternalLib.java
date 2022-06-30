package com.jetbrains.plugins.meteor.ide.action;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.util.ExecUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class MeteorImportPackagesAsExternalLib {

  public static final String METEOR_PACKAGES_LIB_NAME = "meteor-packages-auto-import";
  public static final String PACKAGES_FILE = "packages";

  public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.balloonGroup("MeteorProject");

  public MeteorImportPackagesAsExternalLib() {
  }

  public enum CodeType {
    SERVER {
      @Override
      public String getFolder() {
        return "os";
      }

      @Override
      public String getNameEnd() {
        return "os";
      }
    },
    CLIENT {
      @Override
      public String getFolder() {
        return "web.browser";
      }

      @Override
      public String getNameEnd() {
        return "browser";
      }
    },
    CORDOVA {
      @Override
      public String getFolder() {
        return "web.cordova";
      }

      @Override
      public String getNameEnd() {
        return "cordova";
      }
    },
    NPM {
      @Override
      public String getFolder() {
        return "npm";
      }

      @Override
      public String getNameEnd() {
        return "npm";
      }
    },
    EMPTY {
      @Override
      public String getFolder() {
        return "";
      }

      @Override
      public String getNameEnd() {
        return "";
      }
    };

    public abstract String getFolder();

    public abstract String getNameEnd();
  }

  public void run(@NotNull VirtualFile virtualFile, Project project) {
    ApplicationManager.getApplication().assertIsDispatchThread();

    PsiFile baseFile = PsiManager.getInstance(project).findFile(virtualFile);
    if (baseFile == null) return;
    final VirtualFile dotMeteorVirtualFile = MeteorPackagesUtil.getDotMeteorVirtualFile(project, baseFile);

    updateVersionList(dotMeteorVirtualFile, project);
  }

  private static void updateInfo(Project project, VirtualFile dotMeteorVirtualFile) {
    VirtualFile versionsFile = MeteorPackagesUtil.getVersionsFile(project, dotMeteorVirtualFile);
    if (versionsFile == null) {
      Notification notification = NOTIFICATION_GROUP
        .createNotification(MeteorBundle.message("cannot.find.file.versions.check.your.meteor.has.version.0.9.0.or.higher"), MessageType.WARNING);
      notification.notify(project);
      return;
    }
    Collection<CodeType> oldCodes = MeteorPackagesUtil.getCodes(project);
    String oldPath = MeteorPackagesUtil.getPathToGlobalMeteorRoot(project);
    final MeteorImportPackagesDialog dialog = new MeteorImportPackagesDialog(project, oldPath, oldCodes);
    dialog.show();

    if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;

    Collection<CodeType> dialogCodeTypes = dialog.getCodeTypes();
    String pathToMeteorGlobal = dialog.getPath();

    MeteorPackagesUtil.setPathToGlobalMeteorRoot(project, pathToMeteorGlobal);
    MeteorPackagesUtil.setCodes(project, dialogCodeTypes);

    if (!oldCodes.equals(dialogCodeTypes) || !oldPath.equals(pathToMeteorGlobal)) {
      MeteorLibraryUpdater.refreshLibraries(project, false);
    }
    else {
      MeteorLibraryUpdater.updateLibraryIfRequired(project);
    }
  }


  private static void updateVersionList(@Nullable final VirtualFile dotMeteorVirtualFile, final Project project) {
    if (dotMeteorVirtualFile == null) return;

    final VirtualFile folderWithDotMeteor = dotMeteorVirtualFile.getParent();

    final String executablePath = MeteorSettings.getInstance().getExecutablePath();
    if (StringUtil.isEmpty(executablePath)) {
      Notification notification = NOTIFICATION_GROUP
        .createNotification(MeteorBundle.message("cannot.find.meteor.executable.please.check.meteor.settings"), MessageType.WARNING);
      notification.notify(project);

      return;
    }

    //execute for updating 'versions' file
    boolean result = ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      try {
        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        indicator.setText(MeteorBundle.message("updating.meteor.packages.info"));
        ExecUtil.execAndGetOutput(new GeneralCommandLine(executablePath, "list").withWorkDirectory(folderWithDotMeteor.getPath()));
      }
      catch (ExecutionException ignored) {
      }
    }, MeteorBundle.message("meteor.packages"), true, project);

    if (!result) return;

    WriteAction.run(() -> dotMeteorVirtualFile.refresh(false, true));

    updateInfo(project, dotMeteorVirtualFile);
  }

  @NotNull
  public static String getLibraryName(CodeType type) {
    return METEOR_PACKAGES_LIB_NAME + "-" + type.getNameEnd();
  }
}