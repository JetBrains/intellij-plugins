package com.intellij.flex.uiDesigner;

public final class RequiredAssetsInfo {
  public int swfCount;
  public int bitmapCount;

  public void append(RequiredAssetsInfo otherInfo) {
    swfCount += otherInfo.swfCount;
    bitmapCount += otherInfo.bitmapCount;
  }
}
