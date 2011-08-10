package com.intellij.flex.uiDesigner {
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.errors.IOError;
import flash.events.AsyncErrorEvent;
import flash.events.ErrorEvent;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;
import flash.geom.Rectangle;
import flash.utils.ByteArray;
import flash.utils.Dictionary;

public final class SpriteAssetInitializer {
  private static var pendingClients:Dictionary;

  public static function addPendingClient(clazz:Class, instance:Object):void {
    var loader:MyLoader = MyLoader(pendingClients[clazz]);
    if (loader.pendingClients == null) {
      loader.pendingClients = new Vector.<Object>();
    }

    loader.pendingClients.push(instance);
  }

  public static function init(spriteAssetClass:Class, bounds:Rectangle, data:ByteArray):void {
    if (pendingClients == null) {
      pendingClients = new Dictionary();
    }

    spriteAssetClass["bounds"] = bounds;

    var loader:Loader = new MyLoader(spriteAssetClass);
    pendingClients[spriteAssetClass] = loader;
    loader.contentLoaderInfo.addEventListener(Event.INIT, loadInitHandler);
    loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
    loader.contentLoaderInfo.addEventListener(AsyncErrorEvent.ASYNC_ERROR, loadErrorHandler);
    loader.contentLoaderInfo.addEventListener(SecurityErrorEvent.SECURITY_ERROR, loadErrorHandler);
    loader.loadBytes(data, LoaderContentParentAdobePleaseDoNextStep.createEntirelySeparated());
  }

  private static function loadInitHandler(event:Event):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    var loader:MyLoader = MyLoader(loaderInfo.loader);
    var symbolClass:Class = Class(loaderInfo.applicationDomain.getDefinition("_SymbolOwnClass"));
    loader.assetClass["symbolClass"] = symbolClass;
    if (loader.pendingClients != null) {
      for each (var client:Object in loader.pendingClients) {
        client.symbolClassAvailable(symbolClass);
      }
    }

    removeLoaderListeners(loaderInfo, loader.assetClass);
    loader.pendingClients.length = 0;
    loader.assetClass = null;
    loader.unload();
  }

  private static function loadErrorHandler(event:ErrorEvent):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    var loader:MyLoader = MyLoader(loaderInfo.loader);
    removeLoaderListeners(loaderInfo, loader.assetClass);

    throw new IOError(event.text);
  }

  private static function removeLoaderListeners(loaderInfo:LoaderInfo, spriteAssetClass:Class):void {
    delete pendingClients[spriteAssetClass];

    loaderInfo.removeEventListener(Event.INIT, loadInitHandler);
    loaderInfo.removeEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
    loaderInfo.removeEventListener(AsyncErrorEvent.ASYNC_ERROR, loadErrorHandler);
    loaderInfo.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, loadErrorHandler);
  }
}
}

import flash.display.Loader;

final class MyLoader extends Loader {
  public var assetClass:Class;
  public var pendingClients:Vector.<Object>;

  public function MyLoader(assetClass:Class) {
    this.assetClass = assetClass;
  }
}
