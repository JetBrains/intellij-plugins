package com.jetbrains.plugins.meteor.ide.action;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.MeteorFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Function;

import static com.jetbrains.plugins.meteor.ide.action.MeteorImportPackagesAsExternalLib.PACKAGES_FILE;
import static com.jetbrains.plugins.meteor.ide.action.MeteorPackagesUtil.VERSIONS_FILE_NAME;

public final class MeteorPackagesEditNotificationProvider implements EditorNotificationProvider {
  @Override
  public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project,
                                                                                                                 @NotNull VirtualFile file) {
    if (!file.isInLocalFileSystem() || (!file.getName().equals(VERSIONS_FILE_NAME) && !file.getName().equals(PACKAGES_FILE))) return null;
    if (!MeteorFacade.getInstance().isMeteorProject(project)) return null;

    return fileEditor -> {
      return new ImportPackagesPanel(fileEditor, project);
    };
  }

  private static final class ImportPackagesPanel extends EditorNotificationPanel {
    private ImportPackagesPanel(@NotNull FileEditor editor, @NotNull Project project) {
      super(editor, null, EditorColors.GUTTER_BACKGROUND, Status.Info);
      createActionLabel(MeteorBundle.message("link.label.import.packages.as.library"), () -> {
        new MeteorImportPackagesAsExternalLib().run(editor.getFile(), project);
      });
    }
  }
}
