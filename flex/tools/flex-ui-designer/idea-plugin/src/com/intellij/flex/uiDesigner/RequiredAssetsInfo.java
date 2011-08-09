package com.intellij.flex.uiDesigner;

public final class RequiredAssetsInfo {
  public int swfCount;
  public int imageCount;

  public void append(RequiredAssetsInfo otherInfo) {
    swfCount += otherInfo.swfCount;
    imageCount += otherInfo.imageCount;
  }
}
