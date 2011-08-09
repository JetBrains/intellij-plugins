package com.intellij.flex.uiDesigner;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmbedSwfManager extends EmbedAssetManager<SwfAssetInfo> {
  public static EmbedSwfManager getInstance() {
    return ServiceManager.getService(EmbedSwfManager.class);
  }

  public int add(@NotNull VirtualFile file, @Nullable String symbolName, @NotNull RequiredAssetsInfo requiredAssetsInfo) {
    for (SwfAssetInfo asset : assets) {
      if (asset.file == file && (symbolName == null ? asset.symbolName == null : symbolName.equals(asset.symbolName))) {
        return asset.id;
      }
    }

    requiredAssetsInfo.swfCount++;
    final int id = allocateId();
    add(new SwfAssetInfo(file, symbolName, id));
    return id;
  }
}

class SwfAssetInfo extends EmbedAssetInfo {
  public final String symbolName;

  public SwfAssetInfo(VirtualFile file, @Nullable String symbolName, int id) {
    super(file, id);
    this.symbolName = symbolName;
  }
}