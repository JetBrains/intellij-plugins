package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.VirtualFileImpl;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.net.registerClassAlias;
import flash.system.ApplicationDomain;
import flash.utils.IDataInput;

registerClassAlias("f", VirtualFileImpl);

public final class LibrarySet {
  /**
   * Application Domain contains all class definitions
   * If ApplicationDomainCreationPolicy.MULTIPLE, then applicationDomain equals application domain of last library in set
   */
  public var applicationDomain:ApplicationDomain;

  private static const libraries:Vector.<SwfLibrary> = new Vector.<SwfLibrary>(16);

  public function LibrarySet(id:String, parent:LibrarySet) {
    _id = id;
    _parent = parent;
  }

  private var _id:String;
  public function get id():String {
    return _id;
  }

  private var _parent:LibrarySet;
  public function get parent():LibrarySet {
    return _parent;
  }

  private var _applicationDomainCreationPolicy:ApplicationDomainCreationPolicy;
  public function get applicationDomainCreationPolicy():ApplicationDomainCreationPolicy {
    return _applicationDomainCreationPolicy;
  }

  private var _items:Vector.<LibrarySetItem>;
  public function get items():Vector.<LibrarySetItem> {
    return _items;
  }

  public function readExternal(input:IDataInput):void {
    _applicationDomainCreationPolicy = ApplicationDomainCreationPolicy.enumSet[input.readByte()];
    var n:int = input.readUnsignedByte();
    _items = new Vector.<LibrarySetItem>(n, true);
    for (var i:int = 0; i < n; i++) {
      const flags:int = input.readByte();
      var libraryId:int = input.readUnsignedShort();
      var library:SwfLibrary;
      if ((flags & 2) != 0) {
        library = libraries[libraryId];
      }
      else {
        library = new SwfLibrary();
        if (libraryId >= libraries.length) {
          libraries.length = Math.max(libraries.length, libraryId) + 8;
        }
        libraries[libraryId] = library;
        library.readExternal(input);
      }

      var parents:Vector.<LibrarySetItem> = readParents(input);
      var item:LibrarySetFileItem = new LibrarySetFileItem(library, parents, (flags & 1) != 0);
      if (parents != null) {
        for each (var parent:LibrarySetItem in parents) {
          parent.addSuccessor(item);
        }
      }

      _items[i] = item;
    }

    n = input.readUnsignedByte();
    while (n-- > 0) {
      new LibrarySetEmbedItem(_items[input.readUnsignedByte()], AmfUtil.readUtf(input));
    }
  }

  private function readParents(input:IDataInput):Vector.<LibrarySetItem> {
    var n:int = input.readUnsignedByte();
    if (n == 0) {
      return null;
    }

    var parents:Vector.<LibrarySetItem> = new Vector.<LibrarySetItem>(n, true);
    for (var i:int = 0; i < n; i++) {
      parents[i] = _items[input.readUnsignedByte()];
    }
    return parents;
  }
}
}