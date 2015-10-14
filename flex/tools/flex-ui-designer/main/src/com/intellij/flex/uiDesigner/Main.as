package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;
import cocoa.util.Files;

import com.intellij.flex.uiDesigner.plaf.aqua.IdeaAquaLookAndFeel;

import flash.desktop.NativeApplication;
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.InvokeEvent;
import flash.events.UncaughtErrorEvent;
import flash.filesystem.File;

import net.miginfocom.layout.Grid;
import net.miginfocom.layout.UnitValue;

import org.flyti.plexus.PlexusContainer;
import org.flyti.plexus.PlexusManager;
import org.jetbrains.ApplicationManager;
import org.jetbrains.EntityLists;

public class Main extends Sprite {
  private var invoked:Boolean;
  // cannot load until all plugins are loaded — plugin can override a component used to.
  private var loadedPluginCounter:int;
 
  private var port:int;

  // Burn in Hell, Adobe
  DocumentWindow;
  Grid;
  UnitValue;
  EntityLists;

  public function Main() {
    init();
  }

  private function init():void {
    loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorHandler);
 
    new ComponentSet();
    NativeApplication.nativeApplication.addEventListener(InvokeEvent.INVOKE, invokeHandler);
  }
 
  private function uncaughtErrorHandler(event:UncaughtErrorEvent):void {
    var stackTrace:String = event.error.getStackTrace();
    trace(stackTrace);
    Files.writeString(File.applicationDirectory.nativePath + "/startup-error.txt", stackTrace);
 
    event.preventDefault();
    event.stopImmediatePropagation();

    NativeApplication.nativeApplication.removeEventListener(InvokeEvent.INVOKE, invokeHandler);
  }
 
  private function invokeHandler(event:InvokeEvent):void {
    if (invoked) {
      return;
    }
 
    invoked = true;
    NativeApplication.nativeApplication.removeEventListener(InvokeEvent.INVOKE, invokeHandler);

    ApplicationManager.instance.laf = new IdeaAquaLookAndFeel();
 
    var deferredConnect:Boolean;
    var arguments:Array = event.arguments;
    if (arguments.length > 1) {
      for (var i:int = 1; i < arguments.length; i += 2) {
        var key:String = arguments[i];
        var value:String = arguments[i + 1];
        switch (key) {
          case "-p":
            deferredConnect = true;
            loadPlugin(value);
            break;
 
          default:
            throw new ArgumentError("unknown app parameter: " + key);
        }
      }
    }
 
    port = arguments[0];
    if (!deferredConnect) {
      connect();
    }
  }
 
  private function connect():void {
    var container:PlexusContainer = PlexusManager.instance.container;
    // cache StringRegistry instance
    StringRegistry(container.lookup(StringRegistry));
 
    var socketManager:SocketManager = SocketManager(container.lookup(SocketManager));
    socketManager.addSocketDataHandler(0, SocketDataHandler(container.lookup(DefaultSocketDataHandler)));
    socketManager.connect("localhost", port, 0);
 
    UncaughtErrorManager.instance.listen(parent.loaderInfo);
    // hello, it is flash. we must listen our loaderInfo.uncaughtErrorEvents for errors in plugin swf (as example, our test plugin)
    UncaughtErrorManager.instance.listen(loaderInfo);
    loaderInfo.uncaughtErrorEvents.removeEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorHandler);
  }
 
  private function loadPlugin(path:String):void {
    loadedPluginCounter++;
    var loader:Loader = new Loader();
    loader.contentLoaderInfo.addEventListener(Event.INIT, loadInitHandler);
    loader.loadBytes(Files.readBytes(path), LoaderContentParentAdobePleaseDoNextStep.create());
  }
 
  private function loadInitHandler(event:Event):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    loaderInfo.removeEventListener(event.type, loadInitHandler);
    loaderInfo.loader.unload();
    
    loadedPluginCounter--;
    if (loadedPluginCounter == 0) {
      connect();
    }
  }
}
}
