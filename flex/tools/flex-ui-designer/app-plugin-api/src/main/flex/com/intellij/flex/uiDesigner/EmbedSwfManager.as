package com.intellij.flex.uiDesigner {
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.system.LoaderContext;
import flash.utils.ByteArray;

public class EmbedSwfManager extends AbstractEmbedAssetManager implements EmbedAssetManager {
  private var data:Vector.<SwfCache>;

  public function get(id:int, symbol:String):Object {
    return get2(data[id], symbol);
  }

  private static function get2(swfCache:SwfCache, symbol:String):Object {
    return symbol == null ? swfCache.rootClass : Class(swfCache.applicationDomain.getDefinition(symbol));
  }
  
  public function assign(id:int, symbol:String, propertyHolder:Object, propertyName:String):void {
    var swfCache:SwfCache = data[id];
    if (swfCache.rootClass == null) {
      var pendingClient:PendingClient = new PendingClient(propertyHolder, propertyName, symbol);
      if (swfCache.pendingClient == null) {
        swfCache.pendingClient = pendingClient;
      }
      else {
        swfCache.pendingClients = new Vector.<PendingClient>(2);
        swfCache.pendingClients[0] = swfCache.pendingClient;
        swfCache.pendingClient = null;
        swfCache.pendingClients[1] = pendingClient;
      }
    }
    else {
      propertyHolder[propertyName] = get2(data[id], symbol);
    }
  }
  
  public function load(id:int, bytes:ByteArray):void {
    if (data == null) {
      data = new Vector.<SwfCache>(id + 16);
    }
    else if (id >= data.length) {
      data.length = Math.max(data.length, id) + 16;
    }
    else {
      assert(data[id] == null);
    }

    var swfCache:SwfCache = new SwfCache(id);
    data[id] = swfCache;

    var loader:Loader = new MyLoader(swfCache);
    addLoaderListeners(loader);
    var loaderContext:LoaderContext = new LoaderContext(false, swfCache.applicationDomain);
    configureLoaderContext(loaderContext);
    loader.loadBytes(bytes, loaderContext);
  }

  override protected function loadCompleteHandler(event:Event):void {
    super.loadCompleteHandler(event);
    var loader:MyLoader = MyLoader(LoaderInfo(event.currentTarget).loader);
    loader.assign();
  }

  override protected function loadErrorHandler(event:IOErrorEvent):void {
    super.loadErrorHandler(event);
    data[MyLoader(LoaderInfo(event.currentTarget).loader).swfCache.id] = null;
  }
}
}

import flash.display.AVM1Movie;
import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Loader;
import flash.system.ApplicationDomain;

final class SwfCache {
  public var applicationDomain:ApplicationDomain = new ApplicationDomain();
  public var rootClass:Class;
  
  public var id:int;
  
  public var pendingClients:Vector.<PendingClient>;
  public var pendingClient:PendingClient;

  public function SwfCache(id:int) {
    this.id = id;
  }
}

final class PendingClient {
  private var propertyHolder:Object;
  private var propertyName:String;
  private var symbol:String;

  public function PendingClient(propertyHolder:Object, propertyName:String, symbol:String) {
    this.propertyHolder = propertyHolder;
    this.propertyName = propertyName;
    this.symbol = symbol;
  }
  
  public function assign(content:DisplayObject, swfCache:SwfCache):void {
    propertyHolder[propertyName] = symbol == null ? content : swfCache.applicationDomain.getDefinition(symbol);
  }
}

final class MyLoader extends Loader {
  public var swfCache:SwfCache;
  
  public function MyLoader(swfCache:SwfCache) {
    this.swfCache = swfCache;
  }
  
  public function assign():void {
    swfCache.rootClass = content is AVM1Movie ? content["constructor"] : DisplayObjectContainer(content).getChildAt(0)["constructor"];
    if (swfCache.pendingClient != null) {
      swfCache.pendingClient.assign(content, swfCache);
      swfCache.pendingClient = null;
    }
    else if (swfCache.pendingClients != null) {
      for each (var pendingClient:PendingClient in swfCache.pendingClients) {
        pendingClient.assign(content, swfCache);
      }
    }
    
    unload();
  }
}