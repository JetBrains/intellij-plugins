package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.AssetContainerClassPool;

public class FlexLibrarySet extends LibrarySet {
  public function FlexLibrarySet(id:int, parent:LibrarySet) {
    super(id, parent);
  }

  private var _swfAssetContainerClassPool:AssetContainerClassPool;
  public function get swfAssetContainerClassPool():AssetContainerClassPool {
    if (_swfAssetContainerClassPool == null) {
      _swfAssetContainerClassPool = new AssetContainerClassPool("_s", this);
    }
    return _swfAssetContainerClassPool;
  }

  private var _imageAssetContainerClassPool:AssetContainerClassPool;
  public function get imageAssetContainerClassPool():AssetContainerClassPool {
    if (_imageAssetContainerClassPool == null) {
      _imageAssetContainerClassPool = new AssetContainerClassPool("_b", this);
    }
    return _imageAssetContainerClassPool;
  }
}
}
