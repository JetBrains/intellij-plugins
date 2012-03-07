package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.StringRegistry;
import com.intellij.flex.uiDesigner.VirtualFile;
import com.intellij.flex.uiDesigner.VirtualFileImpl;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.css.Stylesheet;
import com.intellij.flex.uiDesigner.flex.ResourceBundle;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.system.ApplicationDomain;
import flash.utils.Dictionary;
import flash.utils.IDataInput;

import org.jetbrains.Identifiable;

public class LibrarySet implements Identifiable {
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

  public var resourceBundles:Vector.<ResourceBundle>;

  private var _styleManager:StyleManagerEx;
  public function get styleManager():StyleManagerEx {
    return _styleManager;
  }

  public function set styleManager(value:StyleManagerEx):void {
    _styleManager = value;
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
      const registered:Boolean = input.readBoolean();
      var libraryId:int = AmfUtil.readUInt29(input);
      var library:Library;
      if (registered) {
        library = libraries[libraryId];
      }
      else {
        library = readLibrary(input);
        if (libraryId >= libraries.length) {
          libraries.length = Math.max(libraries.length, libraryId) + 8;
        }
        libraries[libraryId] = library;
      }

      _items[i] = library;
    }
  }

  private static function readLibrary(input:IDataInput):Library {
    var file:VirtualFile = VirtualFileImpl.create(input);

    var inheritingStyles:Dictionary = new Dictionary();
    var n:int = input.readUnsignedShort();
    if (n > 0) {
      var stringTable:Vector.<String> = StringRegistry.instance.getTable();
      inheritingStyles = new Dictionary();
      while (n-- > 0) {
        inheritingStyles[stringTable[AmfUtil.readUInt29(input)]] = true;
      }
    }

    var defaultStyle:Stylesheet;
    if (input.readBoolean()) {
      defaultStyle = new Stylesheet();
      defaultStyle.read(input);
    }

    return new Library(file, inheritingStyles, defaultStyle);
  }
}
}