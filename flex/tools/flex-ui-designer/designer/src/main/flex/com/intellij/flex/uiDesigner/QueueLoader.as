package com.intellij.flex.uiDesigner {
import cocoa.util.FileUtil;

import flash.display.Loader;
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
  private var currentIndex:int;

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

  private var completeHandler:Function;
  private var errorHandler:Function;

  private const loader:Loader = new Loader();
  private const loaderContext:LoaderContext = new LoaderContext();
  private const urlRequest:URLRequest = new URLRequest();

  public function QueueLoader(completeHandler:Function, errorHandler:Function = null) {
    this.completeHandler = completeHandler;
    this.errorHandler = errorHandler;

    loaderContext.allowCodeImport = true;
    loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadCompleteHandler);
    loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
  }

  private function loadErrorHandler(event:IOErrorEvent):void {
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
    currentIndex = 0;
    assert(_rootDomain != null);
    loaderContext.applicationDomain = new ApplicationDomain(librarySet.parent == null ? _rootDomain : librarySet.parent.applicationDomain);
    loadNextLibrary();
  }

  private function loadNextLibrary():void {
    // *** Adobe http://juick.com/develar/896344  http://juick.com/develar/896278
    var library:Library = librarySet.libraries[currentIndex++];
    if (library is EmbedLibrary) {
      trace("load: @" + library.path);
      loader.loadBytes(getFlexComplementSwfBytes(library.path), loaderContext);
    }
    else {
      urlRequest.url = "app:/" + library.path + (library is OriginalLibrary ? ".swf" : "_" + librarySet.id + ".swf");
      trace("load: " + urlRequest.url);
      loader.load(urlRequest, loaderContext);
    }
  }

  private function getFlexComplementSwfBytes(path:String):ByteArray {
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
    if (currentIndex < librarySet.libraries.length) {
      if (librarySet.applicationDomainCreationPolicy == ApplicationDomainCreationPolicy.MULTIPLE) {
        loaderContext.applicationDomain = new ApplicationDomain(loaderContext.applicationDomain);
      }

      loadNextLibrary();
    }
    else {
      librarySet.applicationDomain = loaderContext.applicationDomain;
      var lS:LibrarySet = librarySet;
      librarySet = null;
      completeHandler(lS);

      if (queue.length > 0) {
        doLoadLibrarySet(queue.shift());
      }
    }
  }
}
}