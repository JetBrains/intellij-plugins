package com.intellij.flex.uiDesigner {
import flash.display.BitmapData;
import flash.utils.Dictionary;

public class EmbedImageManager extends AbstractEmbedAssetManager implements EmbedAssetManager {
  private const bitmapsData:Dictionary = new Dictionary();

  public function EmbedImageManager(server:Server) {
    super(server);
  }

  public function get(id:int, pool:AssetContainerClassPool, project:Project):Class {
    var result:Class = pool.getCachedClass(id);
    if (result != null) {
      return result;
    }

    var containerClass:Class = pool.getClass(id);
    var bitmapData:BitmapData = bitmapsData[id];
    if (bitmapData == null) {
      bitmapData = server.getBitmapData(id, project);
      bitmapsData[id] = bitmapData;
    }
    containerClass["data"] = bitmapData;

    return containerClass;
  }
}
}