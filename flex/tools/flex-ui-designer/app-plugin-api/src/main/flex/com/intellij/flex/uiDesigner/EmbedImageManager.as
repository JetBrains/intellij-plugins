package com.intellij.flex.uiDesigner {
import flash.system.ApplicationDomain;

public class EmbedImageManager extends AbstractEmbedAssetManager implements EmbedAssetManager {
  public function EmbedImageManager(server:Server) {
    super(server);
  }

  public function get(id:int, applicationDomain:ApplicationDomain):Class {
    var result:Class = getCachedClass(id);
    if (result != null) {
      return result;
    }

    var clazz:Class = getClass("_b", applicationDomain);
    clazz["data"] = server.getBitmapData(id);
    classes[id] = clazz;

    return clazz;
  }

  public function fillClassPool():void {

  }
}
}