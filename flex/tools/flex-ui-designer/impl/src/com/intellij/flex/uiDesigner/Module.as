package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.flex.ResourceBundle;
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;

import flash.system.ApplicationDomain;

import org.jetbrains.Identifiable;

public final class Module implements Identifiable, ModuleContext {
  public var resourceBundles:Vector.<ResourceBundle>;

  public function Module(id:int, project:Project, librarySet:LibrarySet, isApp:Boolean, localStyleHolders:Vector.<LocalStyleHolder>) {
    _isApp = isApp;
    _localStyleHolders = localStyleHolders;
    _librarySet = librarySet;
    _project = project;

    _id = id;
  }

  private var _id:int;
  public function get id():int {
    return _id;
  }

  private var _isApp:Boolean;
  public function get isApp():Boolean {
    return _isApp;
  }

  private var _styleManager:StyleManagerEx;
  /**
   * Don't forget — styleManager may be per document, so, in general case, you must use document.styleManager
   */
  public function get styleManager():StyleManagerEx {
    return _styleManager == null ? _librarySet.styleManager : _styleManager;
  }

  public function set styleManager(styleManager:StyleManagerEx):void {
    _styleManager = styleManager;
  }

  public function get hasOwnStyleManager():Boolean {
    return _styleManager != null;
  }

  private var _localStyleHolders:Vector.<LocalStyleHolder>;
  public function get localStyleHolders():Vector.<LocalStyleHolder> {
    return _localStyleHolders;
  }

  public function get flexLibrarySet():FlexLibrarySet {
    return _librarySet is FlexLibrarySet ? FlexLibrarySet(_librarySet) : _librarySet.parent as FlexLibrarySet;
  }

  public function getClassPool(id:String):ClassPool {
    return flexLibrarySet.getClassPool(id);
  }

  private var _project:Project;
  public function get project():Project {
    return _project;
  }

  private var _librarySet:LibrarySet;
  public function get librarySet():LibrarySet {
    return _librarySet;
  }

  public function get applicationDomain():ApplicationDomain {
    return _librarySet.applicationDomain;
  }

  private var _inlineCssStyleDeclarationClass:Class;
  public function get inlineCssStyleDeclarationClass():Class {
    if (_inlineCssStyleDeclarationClass == null) {
      _inlineCssStyleDeclarationClass = getClass("com.intellij.flex.uiDesigner.css.InlineCssStyleDeclaration");
    }

    return _inlineCssStyleDeclarationClass;
  }

  private var _deferredInstanceFromBytesClass:Class;
  public function get deferredInstanceFromBytesClass():Class {
    if (_deferredInstanceFromBytesClass == null) {
      _deferredInstanceFromBytesClass = getClass("com.intellij.flex.uiDesigner.flex.states.DeferredInstanceFromBytes");
    }

    return _deferredInstanceFromBytesClass;
  }

  private var _deferredInstanceFromBytesVectorClass:Class;
  public function get deferredInstanceFromBytesVectorClass():Class {
    if (_deferredInstanceFromBytesVectorClass == null) {
      _deferredInstanceFromBytesVectorClass = getVectorClass("com.intellij.flex.uiDesigner.flex.states.DeferredInstanceFromBytes");
    }

    return _deferredInstanceFromBytesVectorClass;
  }

  public function getClassIfExists(fqn:String):Class {
    return applicationDomain.hasDefinition(fqn) ? applicationDomain.getDefinition(fqn) as Class : null;
  }

  public function getClass(fqn:String):Class {
    return Class(applicationDomain.getDefinition(fqn));
  }

  public function getVectorClass(fqn:String):Class {
    return Class(applicationDomain.getDefinition("__AS3__.vec::Vector.<" + fqn + ">"));
  }

  public function getDefinition(fqn:String):Object {
    return applicationDomain.getDefinition(fqn);
  }
}
}