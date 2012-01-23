package com.intellij.flex.uiDesigner;

import org.jetbrains.annotations.Nullable;

public final class AssetCounter {
  public int swfCount;
  public int imageCount;

  public AssetCounter(@Nullable AssetCounter assetCounter) {
    append(assetCounter);
  }

  public AssetCounter() {
  }

  public void append(@Nullable AssetCounter otherCounter) {
    if (otherCounter != null) {
      swfCount += otherCounter.swfCount;
      imageCount += otherCounter.imageCount;
    }
  }

  public void clear() {
    swfCount = 0;
    imageCount = 0;
  }
}
