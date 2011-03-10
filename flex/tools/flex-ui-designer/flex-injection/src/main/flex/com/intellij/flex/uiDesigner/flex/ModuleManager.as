package com.intellij.flex.uiDesigner.flex {
import flash.system.ApplicationDomain;
import flash.utils.getQualifiedClassName;

import mx.core.IFlexModuleFactory;

/**
 * @see mx.core.StyleProtoChain#getTypeHierarchy
 */
public class ModuleManager {
  private var flexModuleFactory:IFlexModuleFactory;
  private var applicationDomain:ApplicationDomain;

  public function ModuleManager(flexModuleFactory:IFlexModuleFactory) {
    this.flexModuleFactory = flexModuleFactory;
    applicationDomain = flexModuleFactory.info().currentDomain;
  }

  public function getAssociatedFactory(object:Object):IFlexModuleFactory {
    if (applicationDomain.hasDefinition(getQualifiedClassName(object))) {
      return flexModuleFactory;
    }
    else {
      trace("getAssociatedFactory return null for " + object + " instance of " + getQualifiedClassName(object));
      return null;
    }
  }
}
}