package com.intellij.flex.uiDesigner {
import flash.desktop.NativeApplication;
import flash.display.Bitmap;
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.KeyboardEvent;
import flash.system.ApplicationDomain;
import flash.system.LoaderContext;

import org.flyti.roboflest.roboflest;

public class MainLoader extends Sprite {
  [Embed(source="/designer-1.0-SNAPSHOT.swf", mimeType="application/octet-stream")]
  private static var appClass:Class;

  [Embed(source="/icon256x256.png")]
  private static const icon256:Class;

  [Embed(source="/icon128x128.png")]
  private static const icon128:Class;

  [Embed(source="/icon32x32.png")]
  private static const icon32:Class;

  public function MainLoader() {
    var application:NativeApplication = NativeApplication.nativeApplication;
    application.autoExit = false;
    if (NativeApplication.supportsDockIcon) {
      application.icon.bitmaps = [Bitmap(new icon128()).bitmapData, Bitmap(new icon256()).bitmapData, Bitmap(new icon32()).bitmapData];
    }
    
    config::useRoboflest {
      roboflest();
    }

    var loader:Loader = new Loader();
    loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadCompleteHandler);
    loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
    var loaderContext:LoaderContext = new LoaderContext();
    loaderContext.allowCodeImport = true;
    loader.loadBytes(new appClass(), loaderContext);
    appClass = null;

    addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
  }

  private static function loadErrorHandler(event:IOErrorEvent):void {
    trace(event);
  }

  private function loadCompleteHandler(event:Event):void {
    var loader:Loader = LoaderInfo(event.currentTarget).loader;
    addChild(loader.getChildAt(0));
    loader.contentLoaderInfo.applicationDomain.getDefinition("com.intellij.flex.uiDesigner.libraries.QueueLoader")["rootDomain"] = ApplicationDomain.currentDomain;
  }

  private function keyDownHandler(event:KeyboardEvent):void {
    trace(stage.focus, event);
  }
}
}