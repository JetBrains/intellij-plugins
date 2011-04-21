package com.intellij.flex.uiDesigner {
import flash.utils.Dictionary;

public class LibraryManager {
  private const idMap:Dictionary = new Dictionary();

  private var loader:QueueLoader;

  private const resolveQueue:Dictionary = new Dictionary();

  public function LibraryManager() {
    loader = new QueueLoader(completeHandler);
  }

  private function completeHandler(librarySet:LibrarySet):void {
    var currentQueue:LoaderQueue = resolveQueue[librarySet];
    if (currentQueue != null) {
      var leftBrotherQueue:LoaderQueue;
      var leftmostBrother:LoaderQueue;
      do {
        if (currentQueue.size == 1) {
          currentQueue.apply();
          if (leftBrotherQueue == null) {
            if (currentQueue.tail == null) {
              delete resolveQueue[librarySet];
              break;
            }
            else {
              // optimization, set local var instead of put in map
              leftmostBrother = currentQueue.tail;
            }
          }
          else {
            leftBrotherQueue.tail = currentQueue.tail;
          }
        }
        else {
          currentQueue.size--;
          leftBrotherQueue = currentQueue;
        }
      }
      while ((currentQueue = currentQueue.tail) != null);

      if (leftmostBrother != null) {
        resolveQueue[librarySet] = leftmostBrother;
      }
    }
  }

  public function register(librarySet:LibrarySet):void {
    assert(!(librarySet.id in idMap));
    idMap[librarySet.id] = librarySet;

    loader.load(librarySet);
  }

  public function getById(id:int):LibrarySet {
    return idMap[id];
  }

  public function idsToInstancesAndMarkAsUsed(ids:Vector.<String>):Vector.<LibrarySet> {
    var librarySets:Vector.<LibrarySet> = new Vector.<LibrarySet>(ids.length, true);
    for (var i:int = 0, n:int = ids.length; i < n; i++) {
      librarySets[i] = idMap[ids[i]];
    }
    return librarySets;
  }
  
  public function remove(librartSets:Vector.<LibrarySet>):void {
    for each (var librarySet:LibrarySet in librartSets) {
      delete idMap[librarySet.id];
    }
  }

  public function resolve(librarySets:Vector.<LibrarySet>, readyHandler:Function, ...readyHandlerArguments):void {
    var queue:LoaderQueue;
    for each (var librarySet:LibrarySet in librarySets) {
      if (librarySet.applicationDomain == null) {
        if (queue == null) {
          queue = new LoaderQueue(readyHandler, readyHandlerArguments);
        }

        var leftBrotherQueue:LoaderQueue = resolveQueue[librarySet];
        if (leftBrotherQueue == null) {
          resolveQueue[librarySet] = queue;
        }
        else {
          queue.tail = leftBrotherQueue.tail;
          leftBrotherQueue.tail = queue;
        }
        queue.size++;
      }
    }

    if (queue == null) {
      readyHandler.apply(null, readyHandlerArguments);
    }
  }
}
}

class LoaderQueue {
  private var handler:Function;
  private var handlerArguments:Array;
  public var size:int;

  public var tail:LoaderQueue;

  public function LoaderQueue(handler:Function, handlerArguments:Array) {
    this.handler = handler;
    this.handlerArguments = handlerArguments;
  }

  public function apply():void {
    handler.apply(null, handlerArguments);
  }
}