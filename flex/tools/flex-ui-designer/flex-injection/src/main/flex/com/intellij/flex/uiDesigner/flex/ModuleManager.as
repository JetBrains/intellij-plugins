package com.intellij.flex.uiDesigner.flex {
import flash.system.ApplicationDomain;
import flash.utils.getQualifiedClassName;

import mx.core.IFlexModuleFactory;
import mx.modules.IModuleInfo;

/**
 * @see mx.core.StyleProtoChain#getTypeHierarchy
 * Implement our own version of StyleProtoChain.getTypeHierarchy — delete dependency from ApplicationDomain.
 * So, we can implement shared (i.e. fake) version of ModuleManager (stupid flex concept).
 * Currently, getAssociatedFactory() used only in StyleProtoChain. And we can return null in getModule()
 */
internal final class ModuleManager {
  public function getAssociatedFactory(object:Object):IFlexModuleFactory {
    return null;
  }

  public function getModule(url:String):IModuleInfo {
    return null;
  }
}
}