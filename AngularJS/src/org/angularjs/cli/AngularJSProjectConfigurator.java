package org.angularjs.cli;

import com.intellij.ide.projectView.actions.MarkRootActionBase;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
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
public class AngularJSProjectConfigurator implements DirectoryProjectConfigurator {

  @Override
  public void configureProject(Project project, @NotNull VirtualFile baseDir, Ref<Module> moduleRef) {
    final ModuleManager moduleManager = ModuleManager.getInstance(project);
    final Module[] modules = moduleManager.getModules();
    if (modules.length == 1) {
      final VirtualFile cliJson = AngularCliUtil.findCliJson(baseDir);
      final ModifiableRootModel model = ModuleRootManager.getInstance(modules[0]).getModifiableModel();
      final ContentEntry entry = MarkRootActionBase.findContentEntry(model, baseDir);
      if (entry != null && cliJson != null) {
        excludeDefault(baseDir, entry);
        ApplicationManager.getApplication().runWriteAction(() -> {
          model.commit();
          project.save();
        });
        AngularCliUtil.createRunConfigurations(project, baseDir);
      } else {
        model.dispose();
      }
    }
  }

  public static void excludeDefault(@NotNull VirtualFile baseDir, ContentEntry entry) {
    entry.addExcludeFolder(baseDir.getUrl() + "/dist");
    entry.addExcludeFolder(baseDir.getUrl() + "/tmp");
  }

}
