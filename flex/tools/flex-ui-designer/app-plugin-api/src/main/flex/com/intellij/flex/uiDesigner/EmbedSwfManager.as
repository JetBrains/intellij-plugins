package com.intellij.flex.uiDesigner {
import flash.geom.Rectangle;
import flash.system.ApplicationDomain;
import flash.utils.ByteArray;

public class EmbedSwfManager extends AbstractEmbedAssetManager implements EmbedAssetManager {
  public function EmbedSwfManager(server:Server) {
    super(server);
  }

  public function get(id:int, applicationDomain:ApplicationDomain):Class {
    var result:Class = getCachedClass(id);
    if (result != null) {
      return result;
    }

    var clazz:Class = getClass("_s", applicationDomain);
    var swfInfo:Vector.<Object> = server.getSwfData(id);
    SpriteAssetInitializer.init(clazz, Rectangle(swfInfo[0]), ByteArray(swfInfo[1]));
    classes[id] = clazz;

    return clazz;
  }
}
}