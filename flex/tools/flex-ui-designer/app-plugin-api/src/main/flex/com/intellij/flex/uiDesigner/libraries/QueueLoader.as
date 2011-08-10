package com.intellij.flex.uiDesigner.libraries {
import cocoa.util.FileUtil;

import com.intellij.flex.uiDesigner.LoaderContentParentAdobePleaseDoNextStep;

import flash.display.LoaderInfo;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.filesystem.File;
import flash.net.URLRequest;
import flash.system.ApplicationDomain;
import flash.system.LoaderContext;
import flash.utils.ByteArray;

/**
 * todo handle load error
 */
public class QueueLoader {
  private var librarySet:LibrarySet;
  private var loadedCount:int;

  //noinspection JSUnusedLocalSymbols
  [Embed(source="/complement-flex4.1.swf", mimeType="application/octet-stream")]
  private static const flex41ComplementClass:Class;
  //noinspection JSUnusedLocalSymbols
  private static var flex41ComplementBytes:ByteArray;
  
  //noinspection JSUnusedLocalSymbols
  [Embed(source="/complement-flex4.5.swf", mimeType="application/octet-stream")]
  private static const flex45ComplementClass:Class;
  //noinspection JSUnusedLocalSymbols
  private static var flex45ComplementBytes:ByteArray;

  [Embed(source="/complement-air4.swf", mimeType="application/octet-stream")]
  //noinspection JSUnusedLocalSymbols
  private static const air4ComplementClass:Class;
  //noinspection JSUnusedLocalSymbols
  private static var air4ComplementBytes:ByteArray;

  private static var _rootDomain:ApplicationDomain;
  //noinspection JSUnusedGlobalSymbols
  public static function set rootDomain(value:ApplicationDomain):void {
    if (_rootDomain == null) {
      _rootDomain = value;
    }
  }

  private static var _complementDevDir:String;
  public static function set complementDevDir(value:String):void {
    if (_complementDevDir == null) {
      _complementDevDir = value;
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
  private function createLoadder(library:LibrarySetItem):MyLoader {
    var loader:MyLoader;
    if (freeLoaders.length == 0) {
      loader = new MyLoader();
      loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadCompleteHandler);
      loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
    }
    else {
      loader = freeLoaders.pop();
    }

    loader.library = library;
    return loader;
  }

  private static function loadErrorHandler(event:IOErrorEvent):void {
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
    loadedCount = librarySet.loadSize;
    assert(_rootDomain != null);
    loaderContext.applicationDomain = new ApplicationDomain(librarySet.parent == null ? _rootDomain : librarySet.parent.applicationDomain);
    for each (var item:LibrarySetItem in librarySet.items) {
      if (item.parents == null) {
        loadLibrary(item);
      }
    }
  }

  private function loadLibrary(item:LibrarySetItem):void {
    item.loadState = LoadState.LOADING;
    var loader:MyLoader = createLoadder(item);
    // *** Adobe http://juick.com/develar/896344  http://juick.com/develar/896278
    if (item is LibrarySetEmbedItem) {
      //trace("load: @" + library.path);
      loader.loadBytes(getFlexComplementSwfBytes(item.path), loaderContext);
    }
    else {
      //trace("load: " + urlRequest.url);
      loader.load(new URLRequest("app:/" + item.path + (item.filtered ? "_" + librarySet.id + ".swf" : ".swf")), loaderContext);
    }
  }

  private static function getFlexComplementSwfBytes(path:String):ByteArray {
    var propertyNameBase:String = path.replace(".", "");
    var propertyByteName:String = propertyNameBase + "ComplementBytes";
    if (QueueLoader[propertyByteName] == null) {
      if (_complementDevDir == null) {
        QueueLoader[propertyByteName] = new QueueLoader[propertyNameBase + "ComplementClass"]();
      }
      else {
        QueueLoader[propertyByteName] = FileUtil.readBytesByFile(new File(_complementDevDir + "/complement-" + path + ".swf"));
      }
    }

    return QueueLoader[propertyByteName];
  }

  private function loadCompleteHandler(event:Event):void {
    if (librarySet == null) {
      return; // stopped;
    }

    if (--loadedCount > 0) {
      if (librarySet.applicationDomainCreationPolicy == ApplicationDomainCreationPolicy.MULTIPLE) {
        loaderContext.applicationDomain = new ApplicationDomain(loaderContext.applicationDomain);
      }

      var loader:MyLoader = MyLoader(LoaderInfo(event.currentTarget).loader);
      var loadedLibrary:LibrarySetItem = loader.library;
      loadedLibrary.loadState = LoadState.READY;
      loader.library = null;
      freeLoaders.push(loader);
      if (loadedLibrary.successors == null) {
        return;
      }

      ol: for each (var library:LibrarySetItem in loadedLibrary.successors) {
        if (library.loadState == LoadState.UNINITIALIZED) {
          for each (var parentLibrary:LibrarySetItem in library.parents) {
            if (parentLibrary.loadState != LoadState.READY) {
              continue ol;
            }
          }

          loadLibrary(library);
        }
      }
    }
    else {
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
  }

  private function resetLoadState():void {
    for each (var l:LibrarySetItem in librarySet.items) {
      // reset load state (original library shared between library sets)
      l.loadState = LoadState.UNINITIALIZED;
    }

    for each (var loader:MyLoader in freeLoaders) {
      loader.unload();
      loader.library = null;
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

import com.intellij.flex.uiDesigner.libraries.LibrarySetItem;

import flash.display.Loader;

final class MyLoader extends Loader {
  public var library:LibrarySetItem;
}