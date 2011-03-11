package com.intellij.flex.uiDesigner {
import flash.desktop.NativeApplication;
import flash.display.Bitmap;
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.display.Sprite;
import flash.events.Event;
import flash.system.ApplicationDomain;
import flash.system.LoaderContext;

import org.flyti.roboflest.roboflest;

public class MainLoader extends Sprite {
  [Embed(source="../../../../../../../../designer/target/designer-1.0-SNAPSHOT.swf", mimeType="application/octet-stream")]
  private static var appClass:Class;

  [Embed(source="/icon256x256.png")]
  private static var icon256:Class;

  [Embed(source="/icon128x128.png")]
  private static var icon128:Class;

  [Embed(source="/icon32x32.png")]
  private static var icon32:Class;

  public function MainLoader() {
    NativeApplication.nativeApplication.autoExit = false;
    NativeApplication.nativeApplication.icon.bitmaps = [Bitmap(new icon128()).bitmapData, Bitmap(new icon256()).bitmapData, Bitmap(new icon32()).bitmapData];
    
    roboflest();

    var loader:Loader = new Loader();
    loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadCompleteHandler);
    var loaderContext:LoaderContext = new LoaderContext();
    loaderContext.allowCodeImport = true;
    loader.loadBytes(new appClass(), loaderContext);
    appClass = null;
  }

  private function loadCompleteHandler(event:Event):void {
    var loader:Loader = LoaderInfo(event.currentTarget).loader;
    addChild(loader.getChildAt(0));
    loader.contentLoaderInfo.applicationDomain.getDefinition("com.intellij.flex.uiDesigner.QueueLoader")["rootDomain"] = ApplicationDomain.currentDomain;
  }
}
}