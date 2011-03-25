package com.intellij.flex.uiDesigner {
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.system.LoaderContext;
import flash.utils.ByteArray;

public class SwfDataManager {
  private var data:Vector.<SwfCache>;
  private var contentParent:ContentParent;
  
  public function get(id:int, symbol:String):Class {
    var swfCache:SwfCache = data[id];
    return symbol == null ? swfCache.rootClass : Class(swfCache.applicationDomain.getDefinition(symbol));
  }
  
  public function load(id:int, bytes:ByteArray, symbol:String, propertyHolder:Object, propertyName:String):void {
    if (data == null) {
      data = new Vector.<SwfCache>(id + 8);
    }
    else if (id >= data.length) {
      data.length += 8;
    }
    else {
      assert(data[id] == null);
    }
    
    var swfCache:SwfCache = new SwfCache(id);
    data[id] = swfCache;
    
    var loader:Loader = new MyLoader(swfCache, propertyHolder, propertyName, symbol);
    loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadCompleteHandler);
    loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
    var loaderContext:LoaderContext = new LoaderContext(false, swfCache.applicationDomain);
    // AIR 2.6
    if ("imageDecodingPolicy" in loaderContext) {
      loaderContext["imageDecodingPolicy"] = "onLoad";
      if (contentParent == null) {
        contentParent = new ContentParent();
        loaderContext["requestedContentParent"] = contentParent;
      }
    }
    loaderContext.allowCodeImport = true;
    loader.loadBytes(bytes, loaderContext);
  }

  protected function loadCompleteHandler(event:Event):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    removeLoaderListeners(loaderInfo);
    var loader:MyLoader = MyLoader(loaderInfo.loader);
    loader.assign();
  }

  private function loadErrorHandler(event:IOErrorEvent):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
		removeLoaderListeners(loaderInfo);
    
    data[MyLoader(loaderInfo.loader).swfCache.id] = null;
  }

  private function removeLoaderListeners(loaderInfo:LoaderInfo):void {
    loaderInfo.removeEventListener(Event.COMPLETE, loadCompleteHandler);
    loaderInfo.removeEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
  }
}
}

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Loader;
import flash.display.Sprite;
import flash.system.ApplicationDomain;

class SwfCache {
  public var applicationDomain:ApplicationDomain = new ApplicationDomain();
  public var rootClass:Class;
  
  public var id:int;

  public function SwfCache(id:int) {
    this.id = id;
  }
}

class MyLoader extends Loader {
  public var swfCache:SwfCache;
  
  private var propertyHolder:Object;
  private var propertyName:String;
  private var symbol:String;
  
  public function MyLoader(swfCache:SwfCache, propertyHolder:Object, propertyName:String, symbol:String) {
    this.swfCache = swfCache;
    this.propertyHolder = propertyHolder;
    this.propertyName = propertyName;
    this.symbol = symbol;
  }
  
  public function assign():void {
    swfCache.rootClass = DisplayObjectContainer(content).getChildAt(0)["constructor"];
    propertyHolder[propertyName] = symbol == null ? content : swfCache.applicationDomain.getDefinition(symbol);
    
    unload();
  }
}

final class ContentParent extends Sprite {
  override public function addChild(child:DisplayObject):DisplayObject {
    return child;
  }

  override public function addChildAt(child:DisplayObject, index:int):DisplayObject {
    return child;
  }

  override public function removeChildAt(index:int):DisplayObject {
    return null;
  }

  override public function removeChild(child:DisplayObject):DisplayObject {
    return child;
  }
}