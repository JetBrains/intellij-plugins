package com.intellij.javascript.flex.compiled;

import com.intellij.lang.javascript.flex.importer.FlexImporter;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Maxim.Mossienko
 */
public class SwfFileDecompiler implements BinaryFileDecompiler {
  @Override
  @NotNull
  public CharSequence decompile(@NotNull final VirtualFile file) {
    final Project project = findProject();
    if (project == null) return "";
    try {
      return FlexImporter.buildInterfaceFromStream(file.getInputStream());
    }
    catch (IOException ex) {
      return ArrayUtil.EMPTY_CHAR_SEQUENCE;
    }
  }

  private static Project findProject() {
    final Project[] projects = ProjectManager.getInstance().getOpenProjects();
    if (projects.length == 0) return null;
    final Project project = projects[0];

    return project;
  }
}