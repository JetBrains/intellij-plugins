package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.StyleManagerEx;

import flash.system.ApplicationDomain;

public final class ModuleContextImpl implements ModuleContextEx {
  public function ModuleContextImpl(librarySets:Vector.<LibrarySet>) {
    _librarySets = librarySets;
  }

  private var _styleManager:StyleManagerEx;
  public function get styleManager():StyleManagerEx {
    return _styleManager;
  }

  public function set styleManager(value:StyleManagerEx):void {
    _styleManager = value;
  }

  private var _librarySets:Vector.<LibrarySet>;
  public function get librarySets():Vector.<LibrarySet> {
    return _librarySets;
  }

  public function get applicationDomain():ApplicationDomain {
    return _librarySets[_librarySets.length - 1].applicationDomain;
  }
  
  private var _inlineCssStyleDeclarationClass:Class;
  public function get inlineCssStyleDeclarationClass():Class {
    if (_inlineCssStyleDeclarationClass == null) {
      _inlineCssStyleDeclarationClass = getClass("com.intellij.flex.uiDesigner.css.InlineCssStyleDeclaration");
    }
    
    return _inlineCssStyleDeclarationClass;
  }
  
  private var _effectManagerClass:Class;
  public function get effectManagerClass():Class {
    if (_effectManagerClass == null) {
      _effectManagerClass = getClass("mx.effects.EffectManager");
    }

    return _effectManagerClass;
  }

  public function getClass(fqn:String):Class {
    return Class(applicationDomain.getDefinition(fqn));
  }

  public function getDefinition(fqn:String):Object {
    return applicationDomain.getDefinition(fqn);
  }
}
}
