package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.Server;
import com.intellij.flex.uiDesigner.UncaughtErrorManager;

import flash.utils.Dictionary;

import org.jetbrains.ApplicationManager;

import org.jetbrains.EntityLists;

public class LibraryManager implements LibrarySetLoadProgressListener {
  private const librarySets:Vector.<LibrarySet> = new Vector.<LibrarySet>();

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
    EntityLists.add(librarySets, librarySet);
    loader.load(librarySet);
  }

  public function getById(id:int):LibrarySet {
    return librarySets[id];
  }

  public function idsToInstances(ids:Vector.<int>):Vector.<LibrarySet> {
    var result:Vector.<LibrarySet> = new Vector.<LibrarySet>(ids.length, true);
    for (var i:int = 0, n:int = ids.length; i < n; i++) {
      result[i] = librarySets[ids[i]];
    }
    return result;
  }
  
  public function unregister(librarySets:Vector.<LibrarySet>):void {
    var unregistered:Vector.<int>;
    for each (var librarySet:LibrarySet in librarySets) {
      do {
        librarySet.usageCounter--;
        if (librarySet.usageCounter == 0 && !ApplicationManager.instance.unitTestMode) {
          unregisterLibrarySet(librarySet);
          if (unregistered == null) {
            unregistered = new Vector.<int>();
          }
          unregistered[unregistered.length] = librarySet.id;
        }
      }
      while ((librarySet = librarySet.parent) != null);
    }

    if (unregistered != null) {
      Server.instance.unregisterLibrarySets(unregistered);
    }
  }

  protected function unregisterLibrarySet(librarySet:LibrarySet):void {
    loader.stop(librarySet);
    librarySet.applicationDomain = null;
    librarySets[librarySet.id] = null;
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