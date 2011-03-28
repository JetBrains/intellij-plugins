package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.flex.uiDesigner.io.AmfOutputable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ModuleInfo {
  private final Module module;
  private List<LocalStyleHolder> localStyleHolders;

  public ModuleInfo(Module module) {
    this.module = module;
  }

  public Module getModule() {
    return module;
  }

  public
  @Nullable
  List<LocalStyleHolder> getLocalStyleHolders() {
    return localStyleHolders;
  }

  public void addLocalStyleHolder(LocalStyleHolder localStyleHolder) {
    if (localStyleHolders == null) {
      localStyleHolders = new ArrayList<LocalStyleHolder>(5);
    }

    localStyleHolders.add(localStyleHolder);
  }
}

class LocalStyleHolder implements AmfOutputable {
  final VirtualFile file;
  final byte[] data;

  LocalStyleHolder(VirtualFile file, byte[] data) {
    this.file = file;
    this.data = data;
  }

  @Override
  public void writeExternal(AmfOutputStream out) {
    Client.writeVirtualFile(file, out);
    out.writeAmfByteArray(data);
  }
}
