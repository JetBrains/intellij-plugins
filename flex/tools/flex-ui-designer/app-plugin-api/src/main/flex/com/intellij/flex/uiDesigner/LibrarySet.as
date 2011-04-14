package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.css.CssDeclarationImpl;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.css.ClassReferenceImpl;
import com.intellij.flex.uiDesigner.css.EmbedImage;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.net.registerClassAlias;
import flash.system.ApplicationDomain;
import flash.utils.IDataInput;

registerClassAlias("d", CssRuleset);
registerClassAlias("p", CssDeclarationImpl);
registerClassAlias("pi", CssDeclaration);
registerClassAlias("f", VirtualFileImpl);
registerClassAlias("c", ClassReferenceImpl);
registerClassAlias("ei", EmbedImage);

public final class LibrarySet {
  /**
   * Application Domain contains all class definitions
   * If ApplicationDomainCreationPolicy.MULTIPLE, then applicationDomain equals application domain of last library in set
   */
  public var applicationDomain:ApplicationDomain;

  private static const originalLibraries:Vector.<OriginalLibrary> = new Vector.<OriginalLibrary>();

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

  private var _libraries:Vector.<Library>;
  public function get libraries():Vector.<Library> {
    return _libraries;
  }

  public function readExternal(input:IDataInput):void {
    _applicationDomainCreationPolicy = ApplicationDomainCreationPolicy.enumSet[input.readByte()];
    var n:int = input.readUnsignedShort();
    _libraries = new Vector.<Library>(n, true);
    var originalLibrary:OriginalLibrary;
    for (var i:int = 0; i < n; i++) {
      const marker:int = input.readByte();
      if (marker == 0) {
        originalLibrary = new OriginalLibrary();
        _libraries[i] = originalLibrary;
        originalLibrary.readExternal(input);
        originalLibraries.push(originalLibrary);
      }
      else if (marker == 1) {
        _libraries[i] = originalLibraries[input.readShort()];
      }
      else if (marker < 4) {
        var filteredLibrary:FilteredLibrary = new FilteredLibrary();
        _libraries[i] = filteredLibrary;
        if (marker == 2) {
          filteredLibrary.origin = originalLibrary = new OriginalLibrary();
          originalLibrary.readExternal(input);
          originalLibraries.push(originalLibrary);
        }
        else {
          filteredLibrary.origin = originalLibraries[input.readShort()];
        }
      }
      else {
        assert(marker == 4);
        _libraries[i] = new EmbedLibrary(input.readUTFBytes(AmfUtil.readUInt29(input)));
      }
    }
  }
}
}