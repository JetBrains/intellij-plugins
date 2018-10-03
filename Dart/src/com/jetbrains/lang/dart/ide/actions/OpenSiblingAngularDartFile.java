package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Run the Open Sibling Angular Dart File action. This action opens the next AngularDart file where
 * "next" is defined by foo.dart -> foo.html -> foo.scss -> foo.css
 */
public class OpenSiblingAngularDartFile extends AnAction {

  private static final String DART = "dart";
  private static final String HTML = "html";
  private static final String SCSS = "scss";
  private static final String CSS = "css";

  public OpenSiblingAngularDartFile() {
    super(null, null, DartIcons.Dart_16);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    final VirtualFile currentVirtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
    final Project project = event.getProject();

    // Verify that the current VirtualFile and Project are not null.
    if (currentVirtualFile == null || project == null) {
      return;
    }

    // Verify that the extension is not null and one of "dart", "html", "scss" or "css".
    final String extension = currentVirtualFile.getExtension();
    if (extension == null
        || (!extension.equals(DART) && !extension.equals(HTML) && !extension.equals(SCSS) && !extension.equals(CSS))) {
      return;
    }

    VirtualFile nextVirtualFile = getNextVirtualFile(extension, extension, currentVirtualFile.getUrl());
    if (nextVirtualFile != null) {
      FileEditorManager.getInstance(project).openFile(nextVirtualFile, true, true);
    }
  }

  @Nullable
  private static VirtualFile getNextVirtualFile(@NotNull final String origExtension,
                                                @NotNull String extension,
                                                @NotNull final String currentOpenFileUrl) {
    String nextExtension = getNextExtension(extension);

    // If the original extension is the same as the next extension, then a loop has been made, no sibling exists, return null.
    if (origExtension.equals(nextExtension)) {
      return null;
    }

    String nextFileUrl = replaceExtension(currentOpenFileUrl, nextExtension);
    VirtualFile nextVirtualFile = VirtualFileManager.getInstance().findFileByUrl(nextFileUrl);
    if (nextVirtualFile != null) {
      return nextVirtualFile;
    }
    else {
      return getNextVirtualFile(origExtension, nextExtension, nextFileUrl);
    }
  }

  private static String getNextExtension(@NotNull final String extension) {
    if (extension.equals(DART)) {
      return HTML;
    }
    else if (extension.equals(HTML)) {
      return SCSS;
    }
    else if (extension.equals(SCSS)) {
      return CSS;
    }
    else {
      return DART;
    }
  }

  /**
   * Given some {@link String} and a replacement extension, this method returns the string,
   * with the characters after the last '.' replaced with extension.
   */
  private static String replaceExtension(@NotNull String string, @NotNull String newSuffix) {
    int periodOffset = string.lastIndexOf('.');
    if (periodOffset == -1) {
      return string;
    }
    return string.substring(0, periodOffset + 1) + newSuffix;
  }
}