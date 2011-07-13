package com.intellij.flex.uiDesigner {
import cocoa.Container;
import cocoa.MainWindowedApplication;
import cocoa.SegmentedControl;
import cocoa.bar.SingleSelectionBar;
import cocoa.layout.ListHorizontalLayout;
import cocoa.renderer.InteractiveBorderRendererManager;
import cocoa.renderer.TextRendererManager;
import cocoa.util.FileUtil;

import com.intellij.flex.uiDesigner.libraries.QueueLoader;
import com.intellij.flex.uiDesigner.plaf.EditorTabBarRendererManager;
import com.intellij.flex.uiDesigner.plaf.ProjectViewSkin;
import com.intellij.flex.uiDesigner.plaf.aqua.IdeaAquaLookAndFeel;
import com.intellij.flex.uiDesigner.ui.ElementTreeBarManager;

import flash.desktop.NativeApplication;
import flash.display.Loader;
import flash.display.LoaderInfo;
import flash.events.Event;
import flash.events.InvokeEvent;
import flash.events.UncaughtErrorEvent;
import flash.system.LoaderContext;

import org.flyti.plexus.PlexusContainer;
import org.flyti.plexus.PlexusManager;

public class Main extends MainWindowedApplication {
  private var invoked:Boolean;
  // cannot load until all plugins are loaded — plugin can override a component used to.
  private var loadedPluginCounter:int;

  private var port:int;
  private var errorPort:int;

  config::fdbWorkaround {
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    ElementManager;
    ElementTreeBarManager;
    MainFocusManager;
    DocumentManagerImpl;
    SegmentedControl;
    InteractiveBorderRendererManager;
    TextRendererManager;
    ProjectViewSkin;
    EditorTabBarRendererManager;
    ListHorizontalLayout;
    SingleSelectionBar;
    Container;
    SocketManagerImpl;
    DefaultSocketDataHandler;
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
    // Burn in hell, Adobe
  }

  override protected function initializeMaps():void {
    loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorHandler);

    new ComponentSet();
    NativeApplication.nativeApplication.addEventListener(InvokeEvent.INVOKE, invokeHandler);

    laf = new IdeaAquaLookAndFeel();
  }

  private static function uncaughtErrorHandler(event:UncaughtErrorEvent):void {
    trace(event.error.getStackTrace());
    event.preventDefault();
    event.stopImmediatePropagation();
  }

  private function invokeHandler(event:InvokeEvent):void {
    if (invoked) {
      return;
    }

    invoked = true;
    NativeApplication.nativeApplication.removeEventListener(InvokeEvent.INVOKE, invokeHandler);

    var deferredConnect:Boolean;
    var arguments:Array = event.arguments;
    if (arguments.length > 1) {
      for (var i:int = 2; i < arguments.length; i += 2) {
        var key:String = arguments[i];
        var value:String = arguments[i + 1];
        switch (key) {
          case "-cdd":
            QueueLoader.complementDevDir = value;
            break;

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
    errorPort = arguments[1];
    if (!deferredConnect) {
      connect();
    }
  }

  private function connect():void {
    var container:PlexusContainer = PlexusManager.instance.container;

    //throw new Error("AAAAAAAAAA!!!!!!!!!!1111111");
    
    // cache StringRegistry instance
    StringRegistry(container.lookup(StringRegistry));
    
    var socketManager:SocketManager = SocketManager(container.lookup(SocketManager));
    socketManager.addSocketDataHandler(0, SocketDataHandler(container.lookup(DefaultSocketDataHandler)));
    socketManager.connect("localhost", port, errorPort);
    
    UncaughtErrorManager.instance.listen(this);
    loaderInfo.uncaughtErrorEvents.removeEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorHandler);
  }

  private function loadPlugin(path:String):void {
    loadedPluginCounter++;
    var loader:Loader = new Loader();
    var loaderContext:LoaderContext = new LoaderContext();
    loaderContext.allowCodeImport = true;
    loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadCompleteHandler);
    loader.loadBytes(FileUtil.readBytes(path), loaderContext);
  }

  private function loadCompleteHandler(event:Event):void {
    LoaderInfo(event.currentTarget).removeEventListener(event.type, loadCompleteHandler);
    
    loadedPluginCounter--;
    if (loadedPluginCounter == 0) {
      connect();
    }
  }
}
}