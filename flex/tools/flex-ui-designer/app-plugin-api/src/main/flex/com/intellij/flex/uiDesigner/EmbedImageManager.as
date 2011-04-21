package com.intellij.flex.uiDesigner {
import cocoa.util.StringUtil;

import flash.display.LoaderInfo;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.system.ApplicationDomain;
import flash.system.LoaderContext;
import flash.utils.ByteArray;

public class EmbedImageManager extends AbstractEmbedAssetManager implements EmbedAssetManager {
  private var classes:Vector.<Class>;

  private var lastAllocatedClassIndex:int;

  private var server:Server;

  public function EmbedImageManager(server:Server) {
    this.server = server;
  }

  public function load(id:int, bytes:ByteArray):void {
    return;
    var loader:MyLoader = new MyLoader(id);
    addLoaderListeners(loader);
    var loaderContext:LoaderContext = new LoaderContext(false, new ApplicationDomain());
    configureLoaderContext(loaderContext);
    loader.loadBytes(bytes, loaderContext);
  }

  public function get(id:int, applicationDomain:ApplicationDomain):Class {
    var result:Class;
    if (classes == null) {
      classes = new Vector.<Class>(id + 16);
    }
    else if (id >= classes.length) {
      classes.length = Math.max(classes.length, id) + 16;
    }
    else {
      result = classes[id];
      if (result != null) {
        return result;
      }
    }

    var classIndex:String = lastAllocatedClassIndex.toString(16);
    var className:String = "_b";
    var padding:int = 3 - classIndex.length;
    if (padding > 0) {
      className += StringUtil.repeat("0", padding);
    }
    className += classIndex;

    lastAllocatedClassIndex++;
    var clazz:Class = Class(applicationDomain.getDefinition(className));
    clazz["data"] = server.getBitmapData(id);
    classes[id] = clazz;

    return clazz;
  }

  override protected function loadCompleteHandler(event:Event):void {
    super.loadCompleteHandler(event);
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    var loader:MyLoader = MyLoader(loaderInfo.loader);
    //data[loader.id] = Class(loaderInfo.applicationDomain.getDefinition("B"));
  }

  override protected function loadErrorHandler(event:IOErrorEvent):void {
    super.loadErrorHandler(event);
    var loader:MyLoader = MyLoader(LoaderInfo(event.currentTarget).loader);
    //data[loader.id] = null;
  }
}
}

import flash.display.Loader;

final class MyLoader extends Loader {
  public var id:int;

  public function MyLoader(id:int) {
    this.id = id;
  }
}