package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.UncaughtErrorManager;

import flash.utils.Dictionary;

public class LibraryManager implements LibrarySetLoadProgressListener {
  private const idMap:Dictionary = new Dictionary();

  private var loader:QueueLoader;

  private const resolveQueue:Dictionary = new Dictionary();

  public function LibraryManager() {
    loader = new QueueLoader(this);
  }

  public function complete(librarySet:LibrarySet):void {
    var queueList:Vector.<LoaderQueue> = resolveQueue[librarySet];
    if (queueList == null) {
      return;
    }

    for each (var item:LoaderQueue in queueList) {
      if (--item.count == 0) {
        try {
          item.apply();
        }
        catch (e:Error) {
          UncaughtErrorManager.instance.handleError(e);
        }
      }
    }

    delete resolveQueue[librarySet];
  }

  public function register(librarySet:LibrarySet):void {
    assert(!(librarySet.id in idMap));
    idMap[librarySet.id] = librarySet;

    loader.load(librarySet);
  }

  public function getById(id:int):LibrarySet {
    return idMap[id];
  }

  public function idsToInstancesAndMarkAsUsed(ids:Vector.<int>):Vector.<LibrarySet> {
    var librarySets:Vector.<LibrarySet> = new Vector.<LibrarySet>(ids.length, true);
    for (var i:int = 0, n:int = ids.length; i < n; i++) {
      librarySets[i] = idMap[ids[i]];
    }
    return librarySets;
  }
  
  public function remove(librarySets:Vector.<LibrarySet>):void {
    for each (var librarySet:LibrarySet in librarySets) {
      do {
        librarySet.usageCounter--;
        if (librarySet.usageCounter < 1) {
          removeLibrarySet(librarySet);
        }
      }
      while ((librarySet = librarySet.parent) != null);
    }
  }

  protected function removeLibrarySet(librarySet:LibrarySet):void {
    loader.stop(librarySet);
    librarySet.applicationDomain = null;
    delete idMap[librarySet.id];
  }

  public function resolve(librarySets:Vector.<LibrarySet>, readyHandler:Function, ...readyHandlerArguments):void {
    var queue:LoaderQueue;
    for each (var librarySet:LibrarySet in librarySets) {
      do {
        if (!librarySet.isLoaded) {
          if (queue == null) {
            queue = new LoaderQueue(readyHandler, readyHandlerArguments);
          }

          queue.count++;

          var queueList:Vector.<LoaderQueue> = resolveQueue[librarySet];
          if (queueList == null) {
            queueList = new Vector.<LoaderQueue>(1);
            queueList[0] = queue;
            resolveQueue[librarySet] = queueList;
          }
          else {
            queueList[queueList.length] = queue;
          }
        }
      }
      while ((librarySet = librarySet.parent) != null);
    }

    if (queue == null) {
      readyHandler.apply(null, readyHandlerArguments);
    }
  }
}
}

final class LoaderQueue {
  public var count:int;

  private var handler:Function;
  private var handlerArguments:Array;

  public function LoaderQueue(handler:Function, handlerArguments:Array) {
    this.handler = handler;
    this.handlerArguments = handlerArguments;
  }

  public function apply():void {
    handler.apply(null, handlerArguments);
  }
}