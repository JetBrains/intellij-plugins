package com.jetbrains.lang.dart.ide.editor;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.actions.DartStyleAction;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class DartfmtOnSave extends FileDocumentManagerAdapter {

  public static final String ENABLE_DARTFMT_ON_SAVE = "ENABLE_DARTFMT_ON_SAVE";
  public static final Boolean ENABLE_DARTFMT_ON_SAVE_DEFAULT = false;

  @Override
  public void beforeDocumentSaving(@NotNull final Document document) {
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    final boolean enabled = propertiesComponent.getBoolean(ENABLE_DARTFMT_ON_SAVE, ENABLE_DARTFMT_ON_SAVE_DEFAULT);
    if (enabled) {
      runDartfmt(document);
    }
  }

  private static void runDartfmt(@NotNull final Document document) {
    if (!document.isWritable()) return;
    VirtualFile file = FileDocumentManager.getInstance().getFile(document);
    if (file == null || !file.isValid() || file.getFileType() != DartFileType.INSTANCE) return;

    Project project = ProjectUtil.guessProjectForContentFile(file);
    if (project == null) return;

    DartStyleAction.runDartFmt(project, Collections.singletonList(file), false);
  }
}
