package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.util.FakeBooleanSetProxy;

import flash.utils.Dictionary;

import mx.styles.CSSStyleDeclaration;
import mx.styles.IStyleManager2;

public class RootStyleManager extends AbstractStyleManager implements IStyleManager2, StyleManagerEx {
  private var inheritingStyleMapList:Vector.<Dictionary>;
  private var inheritingStyleFakeProxyObject:FakeBooleanSetProxy;
  
  public function RootStyleManager(inheritingStyleMapList:Vector.<Dictionary>, styleValueResolver:StyleValueResolver):void {
    this.inheritingStyleMapList = inheritingStyleMapList;
    inheritingStyleFakeProxyObject = new FakeBooleanSetProxy(inheritingStyleMapList);

    _styleValueResolver = styleValueResolver;
    
    _instance = this;
  }
  
  private static var _instance:AbstractStyleManager;
  /**
   * impl for mx.core.Singleton
   */
  public static function getInstance():AbstractStyleManager {
    return _instance;
  }
  
  private var _styleValueResolver:StyleValueResolver;
  public function get styleValueResolver():StyleValueResolver {
     return _styleValueResolver;
  }
  
  public function isColorName(colorName:String):Boolean {
    var normalizedColorName:String = colorName.toLowerCase();
    return colorNames[normalizedColorName] !== undefined || (userColorNames != null && userColorNames[normalizedColorName] !== undefined);
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
    // по логике, это Immutable, но наверняка найдется flex-приложение, которое на лету добавляет в global что-то — это неправильно, но это реальность
    rootStyleDeclarationProxy = new StyleDeclarationProxy(rootDeclaration);
  }
}
}
