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

import org.jetbrains.util.ActionCallback;
import org.jetbrains.util.ActionCallbackRef;

public class ClassPool {
  //noinspection JSFieldCanBeLocal
  private var totalClassCount:int;
  private var names:Vector.<int>;
  private const classes:Dictionary = new Dictionary();
  
  private var namePrefix:String;
  private var librarySet:FlexLibrarySet;

  public function ClassPool(namePrefix:String, librarySet:FlexLibrarySet) {
    this.namePrefix = namePrefix;
    this.librarySet = librarySet;
  }

  public function getCachedClass(id:int):Class {
    return classes[id];
  }

  public function removeCachedClass(id:int):void {
    delete classes[id];
  }

  public function getClass(id:int):Class {
    var containerClass:Class = Class(librarySet.applicationDomain.getDefinition(generateClassName()));
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

  public function fill(classCount:int, swfData:ByteArray, libraryManager:LibraryManager):void {
    var loader:MyLoader = new MyLoader(classCount, librarySet.currentFillCallbackRef);
    if (librarySet.isLoaded) {
      doFill(swfData, loader);
    }
    else {
      libraryManager.resolve(librarySet, doFill, swfData, loader);
    }
  }

  private function doFill(swfData:ByteArray, loader:MyLoader):void {
    loader.contentLoaderInfo.addEventListener(Event.INIT, loadInitHandler);
    loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
    loader.contentLoaderInfo.addEventListener(AsyncErrorEvent.ASYNC_ERROR, loadErrorHandler);
    loader.contentLoaderInfo.addEventListener(SecurityErrorEvent.SECURITY_ERROR, loadErrorHandler);
    loader.loadBytes(swfData, LoaderContentParentAdobePleaseDoNextStep.create(librarySet.applicationDomain));
  }

  private function loadInitHandler(event:Event):void {
    var loaderInfo:LoaderInfo = LoaderInfo(event.currentTarget);
    var loader:MyLoader = MyLoader(loaderInfo.loader);

    removeLoaderListeners(loaderInfo);
    loader.unload();

    adjustCache(loader.classCount);
    var callbackRef:ActionCallbackRef = loader.callbackRef;
    callbackRef.usageCount--;
    if (callbackRef.value != null) {
      callbackRef.value.setDone();
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
    var callback:ActionCallback = MyLoader(loaderInfo.loader).callbackRef.value;
    removeLoaderListeners(loaderInfo);

    if (callback != null) {
      callback.setRejected();
    }

    throw new IOError(event.text);
  }

  private function removeLoaderListeners(loaderInfo:LoaderInfo):void {
    loaderInfo.removeEventListener(Event.INIT, loadInitHandler);
    loaderInfo.removeEventListener(IOErrorEvent.IO_ERROR, loadErrorHandler);
    loaderInfo.removeEventListener(AsyncErrorEvent.ASYNC_ERROR, loadErrorHandler);
    loaderInfo.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, loadErrorHandler);
  }
}
}

import flash.display.Loader;

import org.jetbrains.util.ActionCallbackRef;

final class MyLoader extends Loader {
  internal var classCount:int;
  internal var callbackRef:ActionCallbackRef;

  public function MyLoader(classCount:int, callbackRef:ActionCallbackRef) {
    this.classCount = classCount;
    this.callbackRef = callbackRef;
    callbackRef.usageCount++;
  }
}
