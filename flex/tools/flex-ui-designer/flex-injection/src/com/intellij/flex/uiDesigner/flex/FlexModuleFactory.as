package com.intellij.flex.uiDesigner.flex {
import flash.errors.IllegalOperationError;
import flash.system.ApplicationDomain;
import flash.utils.Dictionary;

import mx.core.IFlexModule;
import mx.core.IFlexModuleFactory;
import mx.styles.IStyleManager2;

public class FlexModuleFactory extends BaseFlexModuleFactoryImpl implements IFlexModuleFactory {
  private var _styleManager:IStyleManager2;

  public function FlexModuleFactory(styleManager:IStyleManager2, applicationDomain:ApplicationDomain) {
    this._styleManager = styleManager;
    _info_FLEX_COMPILER_41_BUG.currentDomain = applicationDomain;
  }

  public function get preloadedRSLs():Dictionary {
    throw new IllegalOperationError("unsupported");
  }

  public function allowDomain(... rest):void {
  }

  public function allowInsecureDomain(... rest):void {
  }

  public function callInContext(fn:Function, thisArg:Object, argArray:Array, returns:Boolean = true):* {
    if (returns) {
      return fn.apply(thisArg, argArray);
    }
    else {
      fn.apply(thisArg, argArray);
    }
  }

  public function create(... params):Object {
    var mainClassName:String = String(params[0]);
    var mainClass:Class;
    var domain:ApplicationDomain = ApplicationDomain.currentDomain;
    if (domain.hasDefinition(mainClassName)) {
      mainClass = Class(domain.getDefinition(mainClassName));
    }
    else {
      throw new Error("Class '" + mainClassName + "' not found.");
    }

    var instance:Object = new mainClass();
    if (instance is IFlexModule) {
      IFlexModule(instance).moduleFactory = this;
    }
    return instance;
  }

  public function getImplementation(interfaceName:String):Object {
    if (interfaceName == "mx.styles::IStyleManager2") {
      return _styleManager;
    }
    else {
      return null;
    }
  }

  private const _info_FLEX_COMPILER_41_BUG:Info = new Info();
  public function info():Object {
    return _info_FLEX_COMPILER_41_BUG;
  }

  public function registerImplementation(interfaceName:String, impl:Object):void {
    throw new IllegalOperationError("unsupported");
  }

  public function get styleManager():IStyleManager2 {
    return _styleManager;
  }
}
}

import flash.system.ApplicationDomain;

class Info {
  public var currentDomain:ApplicationDomain;
}