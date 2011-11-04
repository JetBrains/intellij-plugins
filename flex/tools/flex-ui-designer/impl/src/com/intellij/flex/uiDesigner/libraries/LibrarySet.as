package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.system.ApplicationDomain;
import flash.utils.IDataInput;

public class LibrarySet {
  /**
   * Application Domain contains all class definitions
   * If ApplicationDomainCreationPolicy.MULTIPLE, then applicationDomain equals application domain of last library in set
   */
  public var applicationDomain:ApplicationDomain;

  private static const libraries:Vector.<Library> = new Vector.<Library>(16);

  public function LibrarySet(id:int, parent:LibrarySet) {
    _id = id;
    _parent = parent;
  }

  internal var usageCounter:int;

  public function get isLoaded():Boolean {
    return applicationDomain != null;
  }

  public function registerUsage():void {
    usageCounter++;
    if (parent != null) {
      parent.registerUsage();
    }
  }

  private var _id:int;
  public function get id():int {
    return _id;
  }

  private var _parent:LibrarySet;
  public function get parent():LibrarySet {
    return _parent;
  }

  private var _items:Vector.<Library>;
  public function get items():Vector.<Library> {
    return _items;
  }

  public function readExternal(input:IDataInput):void {
    var n:int = input.readUnsignedByte();
    _items = new Vector.<Library>(n, true);
    for (var i:int = 0; i < n; i++) {
      const registered:Boolean = input.readBoolean()
      var libraryId:int = AmfUtil.readUInt29(input);
      var library:Library;
      if (registered) {
        library = libraries[libraryId];
      }
      else {
        library = new Library();
        if (libraryId >= libraries.length) {
          libraries.length = Math.max(libraries.length, libraryId) + 8;
        }
        libraries[libraryId] = library;
        library.readExternal(input);
      }

      _items[i] = library;
    }
  }
}
}