package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.StyleManagerEx;

import flash.system.ApplicationDomain;

public interface ModuleContext {
  function get applicationDomain():ApplicationDomain;

  function get inlineCssStyleDeclarationClass():Class;

  function get effectManagerClass():Class;

  function get styleManager():StyleManagerEx;
  function set styleManager(value:StyleManagerEx):void;

  function getClass(fqn:String):Class;

  function getDefinition(name:String):Object;

  function getDocumentFactory(id:int):Object;

  function putDocumentFactory(id:int, documentFactory:Object):void;

  function get documentFactoryClass():Class;
}
}
