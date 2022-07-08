// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.cli;

import com.intellij.ide.projectView.actions.MarkRootActionBase;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectConfigurator;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public final class AngularJSProjectConfigurator implements DirectoryProjectConfigurator {
  @Override
  public void configureProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Ref<Module> moduleRef, boolean isProjectCreatedWithWizard) {
    Module module = moduleRef.get();
    if (module == null) {
      return;
    }

    final VirtualFile cliJson = AngularCliUtil.findCliJson(baseDir);
    final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
    final ContentEntry entry = MarkRootActionBase.findContentEntry(model, baseDir);
    if (entry != null && cliJson != null) {
      excludeDefault(baseDir, entry);
      ApplicationManager.getApplication().runWriteAction(model::commit);
      project.save();
      AngularCliUtil.createRunConfigurations(project, baseDir);
    }
    else {
      model.dispose();
    }
  }

  @SuppressWarnings("HardCodedStringLiteral")
  public static void excludeDefault(@NotNull VirtualFile baseDir, ContentEntry entry) {
    entry.addExcludeFolder(baseDir.getUrl() + "/dist");
    entry.addExcludeFolder(baseDir.getUrl() + "/tmp");
  }
}
