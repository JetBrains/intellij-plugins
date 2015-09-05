package com.intellij.flex.uiDesigner {
import flash.utils.Dictionary;

public class EmbedSwfManager extends AbstractEmbedAssetManager implements EmbedAssetManager {
  private const symbolClasses:Dictionary = new Dictionary();

  public function EmbedSwfManager(server:Server) {
    super(server);

    SpriteAssetInitializer.embedSwfManager = this;
  }

  public function get(id:int, pool:ClassPool, project:Project):Class {
    var result:Class = pool.getCachedClass(id);
    if (result != null) {
      return result;
    }

    var containerClass:Class = pool.getClass(id);
    var cacheItem:SwfAssetCacheItem = symbolClasses[id];
    if (cacheItem == null) {
      cacheItem = new SwfAssetCacheItem(id);
      symbolClasses[id] = cacheItem;
      SpriteAssetInitializer.init(containerClass, cacheItem, server.getSwfData(id, cacheItem, project));
    }
    else {
      containerClass["bounds"] = cacheItem.bounds;
      if (cacheItem.symbolClass != null) {
        containerClass["symbolClass"] = cacheItem.symbolClass;
      }
    }

    return containerClass;
  }

  //noinspection JSUnusedGlobalSymbols
  public function loadErrorHandler(cacheItem:SwfAssetCacheItem):void {
    // todo what about returned container classes? draw error skin or anything else?
    delete symbolClasses[cacheItem.id];
  }
}
}