// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.actions.addAsLib;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddAsSwcLibAction extends AnAction {
  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = getEventProject(e);
    if (project == null) return;

    final List<VirtualFile> roots = getRoots(e);
    if (!roots.isEmpty()) {
      VirtualFile file = roots.get(0);
      if (!file.isInLocalFileSystem()) {
        file = JarFileSystem.getInstance().getLocalByEntry(file);
      }
      final Module module = file == null ? null : ModuleUtilCore.findModuleForFile(file, project);
      new AddAsSwcLibDialog(project, module, roots).show();
    }
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    final Project project = getEventProject(e);

    final boolean enabled = project != null && containsFlashModule(project) && !getRoots(e).isEmpty();

    e.getPresentation().setText(FlexBundle.messagePointer("add.as.library.title") + "...");
    e.getPresentation().setEnabledAndVisible(enabled);
  }

  @NotNull
  private static List<VirtualFile> getRoots(AnActionEvent e) {
    final Project project = getEventProject(e);
    final VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
    if (project == null || files == null || files.length == 0) return Collections.emptyList();

    //final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

    final List<VirtualFile> roots = new ArrayList<>();
    for (VirtualFile file : files) {
      if (file.isDirectory()) {
        for (VirtualFile child : file.getChildren()) {
          if (FileUtilRt.extensionEquals(child.getName(), "swc")) {
            final VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(child);
            if (jarRoot != null/* && !fileIndex.isInLibraryClasses(child)*/) {
              roots.add(file);
            }
          }
        }
      }
      else if (FileUtilRt.extensionEquals(file.getName(), "swc")) {
        final VirtualFile root = JarFileSystem.getInstance().getJarRootForLocalFile(file);
        if (root != null/* && !fileIndex.isInLibraryClasses(root)*/) {
          roots.add(root);
        }
      }
    }
    return roots;
  }

  private static boolean containsFlashModule(final Project project) {
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      if (ModuleType.get(module) == FlexModuleType.getInstance()) {
        return true;
      }
    }

    return false;
  }
}
