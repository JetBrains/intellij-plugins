package com.intellij.flex.uiDesigner {
public final class AssetInfo {
  public var file:VirtualFile;
  // valuable only for SWF asset
  public var symbol:String;

  public function AssetInfo(file:VirtualFile, symbol:String) {
    this.file = file;
    this.symbol = symbol;
  }
}
}
