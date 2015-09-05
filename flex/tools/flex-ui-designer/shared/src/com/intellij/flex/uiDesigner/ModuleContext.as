package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.StyleManagerEx;

import flash.system.ApplicationDomain;

public interface ModuleContext {
  function get applicationDomain():ApplicationDomain;

  function get inlineCssStyleDeclarationClass():Class;

  function get styleManager():StyleManagerEx;
  function set styleManager(value:StyleManagerEx):void;

  function getClass(fqn:String):Class;
  function getVectorClass(fqn:String):Class;

  function getDefinition(name:String):Object;

  function get deferredInstanceFromBytesClass():Class;

  function get deferredInstanceFromBytesVectorClass():Class;

  function getClassIfExists(fqn:String):Class;
}
}