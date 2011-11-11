package com.intellij.flex.uiDesigner;

import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

abstract class EmbedAssetManager<I extends EmbedAssetInfo> {
  private final TIntArrayList freeIndices = new TIntArrayList();
  protected final ArrayList<I> assets = new ArrayList<I>();

  protected int allocateId() {
    return freeIndices.isEmpty() ? assets.size() : freeIndices.remove(freeIndices.size() - 1);
  }

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
    freeIndices.add(id);
  }

  public I getInfo(int id) {
    return assets.get(id);
  }
}

abstract class EmbedAssetInfo {
  public final VirtualFile file;
  public final int id;

  public EmbedAssetInfo(@NotNull VirtualFile file, int id) {
    this.file = file;
    this.id = id;
  }
}
