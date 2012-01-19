package com.jetbrains.actionscript.profiler.ui.node;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.actionscript.profiler.base.NavigatableDataProducer;
import com.jetbrains.actionscript.profiler.file.LiveObjectsFileType;
import com.jetbrains.actionscript.profiler.livetable.LiveModelController;
import com.jetbrains.actionscript.profiler.model.ProfileData;
import com.jetbrains.actionscript.profiler.model.ProfilingManager;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author: Fedor.Korotkov
 */
public class LiveObjectsNode extends DefaultMutableTreeNode implements NavigatableDataProducer {
  private final String runConfigurationName;
  private final Module module;
  private final ProfilingManager profilingManager;
  private final LiveModelController liveModelController;

  private Navigatable navigatableCache;

  public LiveObjectsNode(String runConfigurationName, Module module, ProfilingManager profilingManager, LiveModelController liveModelController) {
    super("Live Objects");
    this.runConfigurationName = runConfigurationName;
    this.module = module;
    this.profilingManager = profilingManager;
    this.liveModelController = liveModelController;
  }

  @Override
  public Navigatable getNavigatable() {
    if (navigatableCache == null) {
      final VirtualFile virtualFile = new LightVirtualFile(runConfigurationName, new LiveObjectsFileType(), ""){
        @Override
        public String getPath() {
          return getName();
        }
      };
      virtualFile.putUserData(ProfileData.CONTROLLER, liveModelController);
      virtualFile.putUserData(ProfileData.PROFILING_MANAGER, profilingManager);
      navigatableCache = new OpenFileDescriptor(module.getProject(), virtualFile);
    }
    return navigatableCache;
  }
}
