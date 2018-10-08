// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.mvc.projectView;

import com.intellij.ide.SelectInContext;
import com.intellij.ide.SelectInTarget;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.plugins.groovy.mvc.MvcFramework;

/**
 * @author Dmitry Krasilschikov
 */
public class MvcProjectViewSelectInTarget implements DumbAware, SelectInTarget {
  @Override
  public boolean canSelect(SelectInContext context) {
    final Project project = context.getProject();
    final VirtualFile file = context.getVirtualFile();
    final MvcFramework framework = MvcFramework.getInstance(ModuleUtilCore.findModuleForFile(file, project));
    if (framework == null) {
      return false;
    }

    return MvcProjectViewPane.canSelectFile(project, framework, file);
  }

  @Override
  public void selectIn(SelectInContext context, final boolean requestFocus) {
    final Project project = context.getProject();
    final VirtualFile file = context.getVirtualFile();

    final MvcFramework framework = MvcFramework.getInstance(ModuleUtilCore.findModuleForFile(file, project));
    if (framework == null) {
      return;
    }

    final Runnable select = () -> {
      final MvcProjectViewPane view = MvcProjectViewPane.getView(project, framework);
      if (view != null) {
        view.selectFile(file, requestFocus);
      }
    };

    if (requestFocus) {
      ToolWindowManager.getInstance(project).getToolWindow(MvcToolWindowDescriptor.getToolWindowId(framework)).activate(select, false);
    } else {
      select.run();
    }
  }

  public String toString() {
    return "Griffon View";
  }

  @Override
  public float getWeight() {
    return (float)5.239;
  }
}