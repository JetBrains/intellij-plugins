package com.jetbrains.actionscript.profiler.ui.node;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.actionscript.profiler.base.NavigatableDataProducer;
import com.jetbrains.actionscript.profiler.calltree.CallTree;
import com.jetbrains.actionscript.profiler.file.CpuSnapshotFileType;
import com.jetbrains.actionscript.profiler.model.ProfileData;

import javax.swing.tree.DefaultMutableTreeNode;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author: Fedor.Korotkov
 */
public class CPUSnapshotNode extends DefaultMutableTreeNode implements NavigatableDataProducer {
  private final String runConfigurationName;
  private final Module module;
  private final Date date;
  private final CallTree callTree;

  private Navigatable navigatableCache;


  public CPUSnapshotNode(String name, Module module, Date date, CallTree tree) {
    this.runConfigurationName = name;
    this.module = module;
    this.date = date;
    this.callTree = tree;
    setUserObject(getTitle());
  }

  private String getTitle() {
    return "CPU " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
  }


  @Override
  public Navigatable getNavigatable() {
    if (navigatableCache == null) {
      final String name = runConfigurationName + " " + getTitle();
      VirtualFile virtualFile = new LightVirtualFile(name, new CpuSnapshotFileType(), ""){
        @Override
        public String getPath() {
          return getName();
        }
      };
      virtualFile.putUserData(ProfileData.CALL_TREE_KEY, callTree);
      navigatableCache = new OpenFileDescriptor(module.getProject(), virtualFile);
    }
    return navigatableCache;
  }
}
