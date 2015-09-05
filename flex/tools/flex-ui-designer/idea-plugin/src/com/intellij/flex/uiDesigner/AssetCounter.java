package com.intellij.flex.uiDesigner;

import org.jetbrains.annotations.Nullable;

public final class AssetCounter {
  public int swfCount;
  public int imageCount;
  public int viewCount;

  public AssetCounter(@Nullable AssetCounter assetCounter) {
    append(assetCounter);
  }

  public AssetCounter() {
  }

  public void append(@Nullable AssetCounter otherCounter) {
    if (otherCounter != null) {
      swfCount += otherCounter.swfCount;
      imageCount += otherCounter.imageCount;
      viewCount += otherCounter.viewCount;
    }
  }
}
