package com.intellij.lang.javascript.linter.eslint.importer;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.psi.JsonFile;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonCommonUtil;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.util.function.Function;

public class EslintImportCodeStyleEditorNotificationProvider implements EditorNotificationProvider {
  private static final String NOTIFICATION_DISMISSED_PROPERTY = "eslint.code.style.apply.dismiss";

  private final @NotNull Project myProject;

  public EslintImportCodeStyleEditorNotificationProvider(@NotNull Project project) {
    myProject = project;
  }

  private boolean isNotificationDismissed(@NotNull VirtualFile file) {
    return PropertiesComponent.getInstance(myProject).getBoolean(NOTIFICATION_DISMISSED_PROPERTY) ||
           !EslintUtil.isFlatOrLegacyConfigFile(file) && !PackageJsonCommonUtil.isPackageJsonFile(file);
  }

  private void dismissNotification() {
    PropertiesComponent.getInstance(myProject).setValue(NOTIFICATION_DISMISSED_PROPERTY, true);
    EditorNotifications.getInstance(myProject).updateAllNotifications();
  }

  @Override
  public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project,
                                                                                                                 @NotNull VirtualFile file) {
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    if (!(psiFile instanceof JsonFile)) return null;
    if (!psiFile.isWritable() || JSProjectUtil.isInLibrary(psiFile) || JSLibraryUtil.isProbableLibraryFile(file)) return null;
    if (isNotificationDismissed(file)) return null;

    EslintConfigWrapper rulesWrapper = EslintConfigWrapper.getForFile(psiFile);
    if (rulesWrapper == null || !rulesWrapper.hasDataToImport(project)) return null;

    return fileEditor -> {
      if (!(fileEditor instanceof TextEditor)) return null;

      EditorNotificationPanel panel =
        new EditorNotificationPanel(fileEditor, null, EditorColors.GUTTER_BACKGROUND, EditorNotificationPanel.Status.Info);
      panel.setText(EslintBundle.message("eslint.code.style.apply.message"));
      panel.createActionLabel(EslintBundle.message("eslint.code.style.apply.text"), EslintImportCodeStyleAction.ACTION_ID, false);
      panel.createActionLabel(EslintBundle.message("eslint.code.style.dismiss.text"), () -> dismissNotification(), false);
      return panel;
    };
  }
}
