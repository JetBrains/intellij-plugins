package com.jetbrains.plugins.meteor.ide.action;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.MeteorFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.plugins.meteor.ide.action.MeteorImportPackagesAsExternalLibAction.PACKAGES_FILE;
import static com.jetbrains.plugins.meteor.ide.action.MeteorPackagesUtil.VERSIONS_FILE_NAME;

public final class MeteorPackagesEditNotificationProvider extends EditorNotifications.Provider<EditorNotificationPanel> {
  private static final Key<EditorNotificationPanel> KEY = Key.create("MeteorPackagesEditorNotificationsProvider");

  @Override
  @NotNull
  public Key<EditorNotificationPanel> getKey() {
    return KEY;
  }

  @Override
  @Nullable
  public EditorNotificationPanel createNotificationPanel(@NotNull final VirtualFile file, @NotNull final FileEditor fileEditor, @NotNull Project project) {
    if (file.isInLocalFileSystem() && (file.getName().equals(VERSIONS_FILE_NAME) || file.getName().equals(PACKAGES_FILE))) {
      if (MeteorFacade.getInstance().isMeteorProject(project)) {
        return new ImportPackagesPanel();
      }
    }

    return null;
  }

  private static final class ImportPackagesPanel extends EditorNotificationPanel {
    private ImportPackagesPanel() {
      super(EditorColors.GUTTER_BACKGROUND);
      createActionLabel(MeteorBundle.message("link.label.import.packages.as.library"), "Meteor.action.packages");
    }
  }
}
