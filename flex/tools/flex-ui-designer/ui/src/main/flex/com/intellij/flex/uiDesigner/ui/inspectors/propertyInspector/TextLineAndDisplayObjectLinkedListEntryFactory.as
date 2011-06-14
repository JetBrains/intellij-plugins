package com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector {
import flash.display.DisplayObjectContainer;
import flash.text.engine.TextLine;

public class TextLineAndDisplayObjectLinkedListEntryFactory {
  private const pool:Vector.<TextLineAndDisplayObjectLinkedListEntry> = new Vector.<TextLineAndDisplayObjectLinkedListEntry>(32, true);
  private var poolSize:int;
  private var oldPoolSize:int;

  private var displayObjectClass:Class;

  function TextLineAndDisplayObjectLinkedListEntryFactory(displayObjectClass:Class) {
    this.displayObjectClass = displayObjectClass;
  }

  public function create(line:TextLine):TextLineAndDisplayObjectLinkedListEntry {
    if (poolSize == 0) {
      return new TextLineAndDisplayObjectLinkedListEntry(line, new displayObjectClass(), this);
    }
    else {
      var entry:TextLineAndDisplayObjectLinkedListEntry = pool[--poolSize];
      entry.line = line;
      return entry;
    }
  }

  public function addToPool(entry:TextLineAndDisplayObjectLinkedListEntry):void {
    if (poolSize == pool.length) {
      pool.fixed = false;
      pool.length = poolSize << 1;
      pool.fixed = true;
    }
    pool[poolSize++] = entry;
  }

  public function finalizeReused(container:DisplayObjectContainer):void {
    for (var i:int = oldPoolSize, n:int = poolSize; i < n; i++) {
      container.removeChild(pool[i].displayObject);
    }
    oldPoolSize = poolSize;
  }

  public function preReuse():void {
    oldPoolSize = poolSize;
  }
}
}
