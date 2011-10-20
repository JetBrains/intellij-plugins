package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.util.FakeBooleanSetProxy;

import flash.utils.Dictionary;

import mx.core.Singleton;

import mx.styles.CSSStyleDeclaration;
import mx.styles.IStyleManager2;

public class RootStyleManager extends AbstractStyleManager implements IStyleManager2, StyleManagerEx {
  private var inheritingStyleFakeProxyObject:FakeBooleanSetProxy;
  
  public function RootStyleManager(inheritingStyleMapList:Vector.<Dictionary>, styleValueResolver:StyleValueResolver):void {
    inheritingStyleFakeProxyObject = new FakeBooleanSetProxy(inheritingStyleMapList);

    _styleValueResolver = styleValueResolver;

    _instance = this;

    // todo IDEA-72345 temp hack see mx.styles.CSSStyleDeclaration
    Singleton.registerClass("mx.styles::IStyleManager2", RootStyleManager);
  }
  
  private static var _instance:RootStyleManager;
  /**
   * impl for mx.core.Singleton
   */
  public static function getInstance():RootStyleManager {
    return _instance;
  }
  
  private var _styleValueResolver:StyleValueResolver;
  public function get styleValueResolver():StyleValueResolver {
     return _styleValueResolver;
  }
  
  public function isColorName(colorName:String):Boolean {
    var normalizedColorName:String = colorName.toLowerCase();
    return StyleManagerPredefinedData.colorNames[normalizedColorName] !== undefined || (userColorNames != null && userColorNames[normalizedColorName] !== undefined);
  }
  
  /**
   * *** Adobe — http://juick.com/develar/977819
   */
  public function get inheritingStyles():Object {
    return inheritingStyleFakeProxyObject;
  }
  
  public function isInheritingStyle(styleName:String):Boolean {
    return styleName in inheritingStyleFakeProxyObject;
  }
  
  public function registerInheritingStyle(styleName:String):void {
    inheritingStyleFakeProxyObject.addUser(styleName);
  }
  
  public function getMergedStyleDeclaration(selector:String):CSSStyleDeclaration {
    return getStyleDeclaration(selector);
  }

  public function setRootDeclaration(rootDeclaration:CssStyleDeclaration):void {
    // logically, this is Immutable, but there is flex-application that adds on the fly a global anything - this is wrong, but it is a reality
    rootStyleDeclarationProxy = new StyleDeclarationProxy(rootDeclaration);
  }

  private const _typeHierarchyCache:Dictionary = new Dictionary();
  public function get typeHierarchyCache():Object {
    return _typeHierarchyCache;
  }
}
}
