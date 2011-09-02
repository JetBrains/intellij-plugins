package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.libraries.Library;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AssetCounter {
  public int swfCount;
  public int imageCount;

  public void append(@Nullable AssetCounter otherCounter) {
    if (otherCounter != null) {
      swfCount += otherCounter.swfCount;
      imageCount += otherCounter.imageCount;
    }
  }

  public void append(List<Library> libraries) {
    for (Library originalLibrary : libraries) {
      append(originalLibrary.assetCounter);
    }
  }
}
