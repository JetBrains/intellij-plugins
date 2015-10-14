package com.intellij.flex.uiDesigner;

public final class AssetCounterInfo {
  public final AssetCounter demanded;
  public final AssetCounter allocated = new AssetCounter();

  public AssetCounterInfo(AssetCounter demanded) {
    this.demanded = demanded;
  }
}
