package com.intellij.flex.uiDesigner {
import flash.desktop.NativeApplication;
import flash.display.Bitmap;
import flash.display.DisplayObject;
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.system.ApplicationDomain;
import flash.system.LoaderContext;
import flash.utils.setInterval;

import org.jetbrains.roboflest.roboflest;

public class MainLoader extends Sprite {
  [Embed(source="/main-1.0-SNAPSHOT.swf", mimeType="application/octet-stream")]
  private static var appClass:Class;

  [Embed(source="/icon256x256.png")]
  private static var icon256:Class;

  [Embed(source="/icon128x128.png")]
  private static var icon128:Class;

  [Embed(source="/icon32x32.png")]
  private static var icon32:Class;

  public static var displayDispatcher:DisplayObject;

  public function MainLoader() {
    var application:NativeApplication = NativeApplication.nativeApplication;
    application.autoExit = false;
    if (NativeApplication.supportsDockIcon) {
      application.icon.bitmaps = [Bitmap(new icon128()).bitmapData, Bitmap(new icon256()).bitmapData, Bitmap(new icon32()).bitmapData];
    }

    icon128 = null;
    icon256 = null;
    icon32 = null;

    config::useRoboflest {
      roboflest();
      //setInterval(debugTickler, 10000);
    }

    var loader:Loader = new Loader();
    loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadCompleteHandler);
    loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
    var loaderContext:LoaderContext = new LoaderContext();
    loaderContext.allowCodeImport = true;
    loaderContext.requestedContentParent = this;
    loader.loadBytes(new appClass(), loaderContext);
    appClass = null;
  }

  private static function loadErrorHandler(event:IOErrorEvent):void {
    trace(event);
  }

  private static function loadCompleteHandler(event:Event):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    displayDispatcher = loaderInfo.content;
    loaderInfo.applicationDomain.getDefinition("com.intellij.flex.uiDesigner.libraries.QueueLoader")["rootDomain"] = ApplicationDomain.currentDomain;
  }

  //noinspection JSMethodCanBeStatic
  config::useRoboflest
  private function debugTickler():void {
    //noinspection JSUnusedLocalSymbols
    var i:int = 0;
  }
}
}