package com.intellij.flex.uiDesigner {
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.system.LoaderContext;

public class AbstractEmbedAssetManager {
  protected function configureLoaderContext(loaderContext:LoaderContext):void {
    loaderContext.allowCodeImport = true;
    // AIR 2.6
    if (!("requestedContentParent" in loaderContext)) {
      return;
    }

    LoaderContentParentAdobePleaseDoNextStep.configureContext(loaderContext);
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
    loader.contentLoaderInfo.addEventListener(Event.INIT, loadCompleteHandler);
    loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
  }

  protected function removeLoaderListeners(loaderInfo:LoaderInfo):void {
    loaderInfo.removeEventListener(Event.COMPLETE, loadCompleteHandler);
    loaderInfo.removeEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
  }
}
}