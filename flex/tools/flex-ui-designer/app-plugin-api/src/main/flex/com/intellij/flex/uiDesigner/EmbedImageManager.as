package com.intellij.flex.uiDesigner {
import flash.display.LoaderInfo;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.system.ApplicationDomain;
import flash.system.LoaderContext;
import flash.utils.ByteArray;

public class EmbedImageManager extends AbstractEmbedAssetManager implements EmbedAssetManager {
  private var data:Vector.<Class>;

  public function load(id:int, bytes:ByteArray):void {
    if (data == null) {
      data = new Vector.<Class>(id + 8);
    }
    else if (id >= data.length) {
      data.length += 8;
    }
    else {
      assert(data[id] == null);
    }

    var loader:MyLoader = new MyLoader(id);
    addLoaderListeners(loader);
    var loaderContext:LoaderContext = new LoaderContext(false, new ApplicationDomain());
    configureLoaderContext(loaderContext);
    loader.loadBytes(bytes, loaderContext);
  }

  public function get(id:int):Class {
    return data[id];
  }

  override protected function loadCompleteHandler(event:Event):void {
    super.loadCompleteHandler(event);
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    var loader:MyLoader = MyLoader(loaderInfo.loader);
    data[loader.id] = Class(loaderInfo.applicationDomain.getDefinition("B"));
  }

  override protected function loadErrorHandler(event:IOErrorEvent):void {
    super.loadErrorHandler(event);
    var loader:MyLoader = MyLoader(LoaderInfo(event.currentTarget).loader);
    data[loader.id] = null;
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