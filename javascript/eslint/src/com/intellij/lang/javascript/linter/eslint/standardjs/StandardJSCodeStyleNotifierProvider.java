package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.ide.lightEdit.LightEdit;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonCommonUtil;
import com.intellij.lang.javascript.formatter.StandardJSCodeStyle;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.LightColors;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.util.function.Function;

public final class StandardJSCodeStyleNotifierProvider implements EditorNotificationProvider, DumbAware {
  private static final @NonNls String CLOSED = "standardjs.codestyle.accepted";

  @Override
  public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project,
                                                                                                                 @NotNull VirtualFile file) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return null;
    }
    if (LightEdit.owns(project)) {
      return null;
    }

    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    if (propertiesComponent.getBoolean(CLOSED) || StandardJSCodeStyle.isInstalled(project)) {
      return null;
    }
    if (!file.isWritable() || JSProjectUtil.isInLibrary(file, project) || JSLibraryUtil.isProbableLibraryFile(file)) return null;
    if (!isPackageJsonWithDependency(file)) {
      return null;
    }

    return fileEditor -> {
      if (ApplicationManager.getApplication().isUnitTestMode() || !(fileEditor instanceof TextEditor)) {
        return null;
      }

      final EditorNotificationPanel panel = new EditorNotificationPanel(LightColors.YELLOW, EditorNotificationPanel.Status.Info)
        .text(EslintBundle.message("standardjs.editor.notification.can.be.enabled.text"));
      panel.createActionLabel(EslintBundle.message("standardjs.editor.notification.action"), () -> {
        StandardJSCodeStyle.install(project);
        propertiesComponent.setValue(CLOSED, true);
        EditorNotifications.getInstance(project).updateAllNotifications();
      }, false);
      panel.createActionLabel(EslintBundle.message("standardjs.editor.notification.do.not.show.text"), () -> {
        propertiesComponent.setValue(CLOSED, true);
        EditorNotifications.getInstance(project).updateAllNotifications();
      }, false);

      return panel;
    };
  }

  private static boolean isPackageJsonWithDependency(@NotNull VirtualFile file) {
    return PackageJsonCommonUtil.isPackageJsonFile(file) &&
           PackageJsonData.getOrCreate(file).isDependencyOfAnyType(StandardJSUtil.PACKAGE_NAME);
  }
}
