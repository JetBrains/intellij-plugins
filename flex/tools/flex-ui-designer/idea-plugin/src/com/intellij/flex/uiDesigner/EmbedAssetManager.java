package com.intellij.flex.uiDesigner;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.io.IdPool;

import java.util.ArrayList;

abstract class EmbedAssetManager<I extends EmbedAssetInfo> {
  protected final IdPool idPool = new IdPool();
  protected final ArrayList<I> assets = new ArrayList<>();

  protected void add(@NotNull I info) {
    if (info.id == assets.size()) {
      assets.add(info);
    }
    else {
      assets.set(info.id, info);
    }
  }

  protected void remove(int id) {
    assets.set(id, null);
    idPool.dispose(id);
  }

  public I getInfo(int id) {
    return assets.get(id);
  }
}

abstract class EmbedAssetInfo {
  public final VirtualFile file;
  public final int id;

  EmbedAssetInfo(@NotNull VirtualFile file, int id) {
    this.file = file;
    this.id = id;
  }
}