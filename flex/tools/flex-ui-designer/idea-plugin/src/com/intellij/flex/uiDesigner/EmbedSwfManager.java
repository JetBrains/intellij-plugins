package com.intellij.flex.uiDesigner;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmbedSwfManager extends EmbedAssetManager<SwfAssetInfo> {
  public static final String FXG_MARKER = "\u200B";

  public static EmbedSwfManager getInstance() {
    return DesignerApplicationManager.getService(EmbedSwfManager.class);
  }

  public int add(@NotNull VirtualFile file, @Nullable String symbolName, @NotNull AssetCounter assetCounter) {
    for (SwfAssetInfo asset : assets) {
      if (asset.file.equals(file) && (symbolName == null ? asset.symbolName == null : symbolName.equals(asset.symbolName))) {
        return asset.id;
      }
    }

    assetCounter.swfCount++;
    int id = idPool.allocate();
    add(new SwfAssetInfo(file, symbolName, id));
    return id;
  }
}

class SwfAssetInfo extends EmbedAssetInfo {
  @Nullable
  public final String symbolName;

  SwfAssetInfo(VirtualFile file, @Nullable String symbolName, int id) {
    super(file, id);
    this.symbolName = symbolName;
  }
}