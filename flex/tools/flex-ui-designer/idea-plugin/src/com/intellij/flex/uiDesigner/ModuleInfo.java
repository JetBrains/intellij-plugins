package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.flex.uiDesigner.io.AmfOutputable;
import com.intellij.flex.uiDesigner.io.Info;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ModuleInfo extends Info<Module> implements Disposable {
  private List<LocalStyleHolder> localStyleHolders;

  public final AssetCounterInfo assetCounterInfo;

  public ModuleInfo(Module module, AssetCounterInfo assetCounterInfo) {
    super(module);
    this.assetCounterInfo = assetCounterInfo;
  }

  public Module getModule() {
    return element;
  }

  public @Nullable List<LocalStyleHolder> getLocalStyleHolders() {
    return localStyleHolders;
  }

  public void addLocalStyleHolder(LocalStyleHolder localStyleHolder) {
    if (localStyleHolders == null) {
      localStyleHolders = new ArrayList<LocalStyleHolder>(5);
    }

    localStyleHolders.add(localStyleHolder);
  }

  @Override
  public void dispose() {
    // need only for message bus connections
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
