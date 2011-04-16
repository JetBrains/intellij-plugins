package com.intellij.flex.uiDesigner {
import flash.utils.ByteArray;

public interface EmbedAssetManager {
  function load(id:int, bytes:ByteArray):void;
}
}
