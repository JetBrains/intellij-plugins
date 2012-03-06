package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.Server;
import com.intellij.flex.uiDesigner.UncaughtErrorManager;

import flash.utils.Dictionary;

import org.jetbrains.ApplicationManager;
import org.jetbrains.EntityLists;

public class LibraryManager {
  private const librarySets:Dictionary = new Dictionary(); // <int, LibrarySet>;

  private var loader:QueueLoader;

  private const resolveQueue:Dictionary = new Dictionary();

  public function LibraryManager() {
    loader = new QueueLoader();
    loader.done.add(complete);
  }

  private function complete(librarySet:LibrarySet):void {
    var queueList:Vector.<LoaderQueue> = resolveQueue[librarySet];
    if (queueList == null) {
      return;
    }

    for each (var item:LoaderQueue in queueList) {
      if (--item.count == 0) {
        try {
          item.run();
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

  public function unregister(librarySet:LibrarySet):void {
    var unregistered:Vector.<int>;
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

    if (unregistered != null) {
      Server.instance.unregisterLibrarySets(unregistered);
    }
  }

  protected function unregisterLibrarySet(librarySet:LibrarySet):void {
    loader.stop(librarySet);
    librarySet.applicationDomain = null;
    librarySets[librarySet.id] = null;
  }

  public function resolve(librarySet:LibrarySet, readyHandler:Function, ...readyHandlerArguments):void {
    var queue:LoaderQueue;
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

    if (queue == null) {
      readyHandler.apply(null, readyHandlerArguments);
    }
  }
}
}

import org.jetbrains.util.Runnable;

final class LoaderQueue extends Runnable {
  internal var count:int;

  public function LoaderQueue(handler:Function, handlerArguments:Array) {
    super(handler, handlerArguments);
  }
}