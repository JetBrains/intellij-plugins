package com.jetbrains.plugins.meteor.ide.action;


import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.MeteorFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Function;

/**
 * If file type for '*.html' isn't HTML we cannot consider the file as Spacebars
 * but some users changed association for '*.html' to Handlebars (there was no other way before).
 * So let's offer user to change association on correct (HTML)
 */
public final class MeteorHtmlFileTypeEditNotificationProvider implements EditorNotificationProvider {
  private static final String METEOR_CHANGE_ASSOCIATION_DISMISSED = "js.meteor.notification.html.filetype.dismissed";
  private static final String HTML = "html";

  @Override
  public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project,
                                                                                                                 @NotNull VirtualFile file) {
    if (!MeteorFacade.getInstance().isMeteorProject(project)) return null;
    FileType html = FileTypeManager.getInstance().getFileTypeByExtension("html");
    if (html == HtmlFileType.INSTANCE) return null;

    return fileEditor -> {
      if (!PropertiesComponent.getInstance(project).getBoolean(METEOR_CHANGE_ASSOCIATION_DISMISSED)) {
        return new IncorrectHtmlAssociationPanel(project);
      }

      return null;
    };
  }

  private static final class IncorrectHtmlAssociationPanel extends EditorNotificationPanel {
    private IncorrectHtmlAssociationPanel(@NotNull Project project) {
      super(EditorColors.GUTTER_BACKGROUND, Status.Warning);
      setText(MeteorBundle.message("action.meteor.html.file.type.warning"));

      createActionLabel(MeteorBundle.message("action.meteor.html.file.type.change"), () -> ApplicationManager.getApplication().runWriteAction(() -> {
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        FileType oldType = fileTypeManager.getFileTypeByExtension(HTML);
        fileTypeManager.removeAssociatedExtension(oldType, HTML);
        fileTypeManager.associateExtension(HtmlFileType.INSTANCE, HTML);
      }));
      createActionLabel(MeteorBundle.message("action.meteor.html.file.type.dismiss"), () -> {
        PropertiesComponent.getInstance(project).setValue(METEOR_CHANGE_ASSOCIATION_DISMISSED, true);
        EditorNotifications.getInstance(project).updateAllNotifications();
      });
    }
  }
}
