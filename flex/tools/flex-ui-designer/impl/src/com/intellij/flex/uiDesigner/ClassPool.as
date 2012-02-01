package com.intellij.flex.uiDesigner {
import cocoa.util.Strings;

import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;

import flash.display.LoaderInfo;
import flash.errors.IOError;
import flash.events.AsyncErrorEvent;
import flash.events.ErrorEvent;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;
import flash.utils.ByteArray;
import flash.utils.Dictionary;

import org.osflash.signals.ISignal;
import org.osflash.signals.Signal;

public class ClassPool {
  //noinspection JSFieldCanBeLocal
  private var totalClassCount:int;
  private var names:Vector.<int>;
  private const classes:Dictionary = new Dictionary();
  
  private var namePrefix:String;
  private var flexLibrarySet:LibrarySet;

  public function ClassPool(namePrefix:String, librarySet:LibrarySet) {
    this.namePrefix = namePrefix;
    this.flexLibrarySet = librarySet;
  }

  private var _filled:ISignal;
  public function get filled():ISignal {
    if (_filled == null) {
      _filled = new Signal();
    }
    return _filled;
  }

  private var _filling:int;
  public function get filling():Boolean {
    return _filling != 0;
  }

  public function getCachedClass(id:int):Class {
    return classes[id];
  }

  public function removeCachedClass(id:int):void {
    delete classes[id];
  }

  public function getClass(id:int):Class {
    var containerClass:Class = Class(flexLibrarySet.applicationDomain.getDefinition(generateClassName()));
    classes[id] = containerClass;
    return containerClass;
  }

  private function generateClassName():String {
    var classIndex:String = names.pop().toString(16);
    var className:String = namePrefix;
    var padding:int = 3 - classIndex.length;
    if (padding > 0) {
      className += Strings.repeat("0", padding);
    }
    className += classIndex;
    return className;
  }

  public function fill(classCount:int, swfData:ByteArray, librarySet:FlexLibrarySet, libraryManager:LibraryManager):void {
    if (libraryManager != null && !librarySet.isLoaded) {
      _filling++;
      libraryManager.resolve(new <LibrarySet>[librarySet], fillAfterResolveLibraries, classCount, swfData, librarySet);
      return;
    }

    var loader:MyLoader = new MyLoader(classCount);
    loader.contentLoaderInfo.addEventListener(Event.INIT, loadInitHandler);
    loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
    loader.contentLoaderInfo.addEventListener(AsyncErrorEvent.ASYNC_ERROR, loadErrorHandler);
    loader.contentLoaderInfo.addEventListener(SecurityErrorEvent.SECURITY_ERROR, loadErrorHandler);
    // libraryManager will be null if called from our fillAfterResolveLibraries (we increment _filling,
    // because our client must be notified about filling — call filling before librariesResolved must return true)
    if (libraryManager != null) {
      _filling++;
    }
    loader.loadBytes(swfData, LoaderContentParentAdobePleaseDoNextStep.create(flexLibrarySet.applicationDomain));
  }

  private function fillAfterResolveLibraries(classCount:int, swfData:ByteArray, librarySet:FlexLibrarySet):void {
    fill(classCount, swfData, librarySet, null);
  }

  private function loadInitHandler(event:Event):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    var loader:MyLoader = MyLoader(loaderInfo.loader);

    removeLoaderListeners(loaderInfo);
    loader.unload();

    adjustCache(loader.classCount);
    if (_filling == 0) {
      if (_filled != null) {
        _filled.dispatch();
      }
    }
  }

  private function adjustCache(classCount:int):void {
    var i:int;
    var j:int;
    if (names == null) {
      totalClassCount = classCount;
      names = new Vector.<int>(classCount);
      i = 0;
      j = 0;
    }
    else {
      i = names.length;
      names.length = i + classCount;
      j = totalClassCount;
      totalClassCount += classCount;
    }

    while (j < totalClassCount) {
      names[i++] = j++;
    }
  }

  private function loadErrorHandler(event:ErrorEvent):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    removeLoaderListeners(loaderInfo);

    throw new IOError(event.text);
  }

  private function removeLoaderListeners(loaderInfo:LoaderInfo):void {
    _filling--;
    loaderInfo.removeEventListener(Event.INIT, loadInitHandler);
    loaderInfo.removeEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
    loaderInfo.removeEventListener(AsyncErrorEvent.ASYNC_ERROR, loadErrorHandler);
    loaderInfo.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, loadErrorHandler);
  }
}
}

import flash.display.Loader;

final class MyLoader extends Loader {
  public var classCount:int;

  public function MyLoader(classCount:int) {
    this.classCount = classCount;
  }
}
