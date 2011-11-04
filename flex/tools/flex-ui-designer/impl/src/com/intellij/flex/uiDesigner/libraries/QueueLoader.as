package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.LoaderContentParentAdobePleaseDoNextStep;

import flash.events.AsyncErrorEvent;
import flash.events.ErrorEvent;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;
import flash.filesystem.File;
import flash.net.URLRequest;
import flash.system.ApplicationDomain;
import flash.system.LoaderContext;

/**
 * todo handle load error
 */
public class QueueLoader {
  private var librarySet:LibrarySet;

  private static var _rootDomain:ApplicationDomain;
  //noinspection JSUnusedGlobalSymbols
  public static function set rootDomain(value:ApplicationDomain):void {
    if (_rootDomain == null) {
      _rootDomain = value;
    }
  }

  private const queue:Vector.<LibrarySet> = new Vector.<LibrarySet>();

  private var progressListener:LibrarySetLoadProgressListener;

  private const loaderContext:LoaderContext = new LoaderContext();

  public function QueueLoader(librarySetLoadProgressListener:LibrarySetLoadProgressListener) {
    this.progressListener = librarySetLoadProgressListener;

    loaderContext.allowCodeImport = true;
    LoaderContentParentAdobePleaseDoNextStep.configureContext(loaderContext);
  }

  private const freeLoaders:Vector.<MyLoader> = new Vector.<MyLoader>();
  private function createLoadder(librarySet:LibrarySet):MyLoader {
    var loader:MyLoader;
    if (freeLoaders.length == 0) {
      loader = new MyLoader();
      loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadCompleteHandler);
      loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
      loader.contentLoaderInfo.addEventListener(AsyncErrorEvent.ASYNC_ERROR, loadErrorHandler);
      loader.contentLoaderInfo.addEventListener(SecurityErrorEvent.SECURITY_ERROR, loadErrorHandler);
    }
    else {
      loader = freeLoaders.pop();
    }

    loader.librarySet = librarySet;
    return loader;
  }

  private static function loadErrorHandler(event:ErrorEvent):void {
    var f:File = File.applicationDirectory;
    trace(event.text, f.nativePath);
  }

  public function load(librarySet:LibrarySet):void {
    if (this.librarySet == null) {
      doLoadLibrarySet(librarySet);
    }
    else {
      queue[queue.length] = librarySet;
    }
  }

  private function doLoadLibrarySet(librarySet:LibrarySet):void {
    this.librarySet = librarySet;
    assert(_rootDomain != null);
    loaderContext.applicationDomain = new ApplicationDomain(librarySet.parent == null ? _rootDomain : librarySet.parent.applicationDomain);
    loadLibrary(librarySet);
  }

  private function loadLibrary(librarySet:LibrarySet):void {
    var loader:MyLoader = createLoadder(librarySet);
    // *** Adobe http://juick.com/develar/896344  http://juick.com/develar/896278
    //trace("load: " + urlRequest.url);
    loader.load(new URLRequest("app:/" + librarySet.id + ".swf"), loaderContext);
  }

  private function loadCompleteHandler(event:Event):void {
    if (librarySet == null) {
      return; // stopped;
    }

    librarySet.applicationDomain = loaderContext.applicationDomain;
    resetLoadState();

    var lS:LibrarySet = librarySet;
    librarySet = null;
    trace("library set loaded");
    progressListener.complete(lS);

    if (queue.length > 0) {
      doLoadLibrarySet(queue.shift());
    }
  }

  private function resetLoadState():void {
    for each (var loader:MyLoader in freeLoaders) {
      loader.unload();
      loader.librarySet = null;
    }
  }

  public function stop(librarySet:LibrarySet):void {
    if (this.librarySet == librarySet) {
      resetLoadState();
      this.librarySet = null;
      queue.length = 0;
    }
  }
}
}

import com.intellij.flex.uiDesigner.libraries.LibrarySet;

import flash.display.Loader;

final class MyLoader extends Loader {
  public var librarySet:LibrarySet;
}