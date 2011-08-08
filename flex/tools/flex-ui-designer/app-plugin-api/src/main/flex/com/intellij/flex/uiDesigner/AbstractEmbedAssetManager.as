package com.intellij.flex.uiDesigner {
import cocoa.util.StringUtil;

import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.system.ApplicationDomain;
import flash.system.LoaderContext;

public class AbstractEmbedAssetManager {
  protected var classes:Vector.<Class>;
  protected var lastAllocatedClassIndex:int;

  protected var server:Server;

  public function AbstractEmbedAssetManager(server:Server) {
    this.server = server;
  }

  protected function getCachedClass(id:int):Class {
    if (classes == null) {
      classes = new Vector.<Class>(id + 16);
    }
    else if (id >= classes.length) {
      classes.length = Math.max(classes.length, id) + 16;
    }
    else {
      return classes[id];
    }

    return null;
  }

  protected function getClass(prefix:String, applicationDomain:ApplicationDomain):Class {
    return Class(applicationDomain.getDefinition(generateClassName(prefix)));
  }

  protected function generateClassName(prefix:String):String {
    var classIndex:String = lastAllocatedClassIndex.toString(16);
    lastAllocatedClassIndex++;
    var className:String = prefix;
    var padding:int = 3 - classIndex.length;
    if (padding > 0) {
      className += StringUtil.repeat("0", padding);
    }
    className += classIndex;
    return className;
  }

  protected function configureLoaderContext(loaderContext:LoaderContext):void {
    loaderContext.allowCodeImport = true;
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