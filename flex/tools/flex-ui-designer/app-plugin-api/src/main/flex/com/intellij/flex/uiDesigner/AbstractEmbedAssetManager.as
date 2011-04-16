package com.intellij.flex.uiDesigner {
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.system.LoaderContext;

public class AbstractEmbedAssetManager {
  private static var contentParent:ContentParent;

  protected function configureLoaderContext(loaderContext:LoaderContext):void {
    // AIR 2.6
    if (!("imageDecodingPolicy" in loaderContext)) {
      return;
    }

    loaderContext["imageDecodingPolicy"] = "onLoad";
    if (contentParent == null) {
      contentParent = new ContentParent();
      loaderContext["requestedContentParent"] = contentParent;
    }
  }

  protected function loadErrorHandler(event:IOErrorEvent):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
		removeLoaderListeners(loaderInfo);
  }

  protected function loadCompleteHandler(event:Event):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    removeLoaderListeners(loaderInfo);
  }

  protected function addLoaderListeners(loader:Loader):void {
    loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadCompleteHandler);
    loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
  }

  protected function removeLoaderListeners(loaderInfo:LoaderInfo):void {
    loaderInfo.removeEventListener(Event.COMPLETE, loadCompleteHandler);
    loaderInfo.removeEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
  }
}
}

import flash.display.DisplayObject;
import flash.display.Sprite;

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
