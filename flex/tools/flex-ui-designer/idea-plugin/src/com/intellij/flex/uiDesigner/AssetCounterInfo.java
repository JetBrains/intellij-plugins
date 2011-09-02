package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.libraries.Library;

import java.util.List;

public final class AssetCounterInfo {
  public final AssetCounter demanded = new AssetCounter();
  public final AssetCounter allocated = new AssetCounter();

  public AssetCounterInfo(List<Library> libraries) {
    demanded.append(libraries);
  }
}
