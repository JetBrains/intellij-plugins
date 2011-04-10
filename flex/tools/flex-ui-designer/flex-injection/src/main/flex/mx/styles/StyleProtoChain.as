package mx.styles {
import com.intellij.flex.uiDesigner.css.AbstractCssStyleDeclaration;
import com.intellij.flex.uiDesigner.css.InlineCssRuleset;
import com.intellij.flex.uiDesigner.css.InlineCssStyleDeclaration;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;

import mx.core.IFlexModule;
import mx.core.IInvalidating;
import mx.core.IUITextField;
import mx.core.mx_internal;
import mx.effects.EffectManager;

use namespace mx_internal;

public class StyleProtoChain {
  public static const STYLE_UNINITIALIZED:Object = {};

  public static function getClassStyleDeclarations(object:IStyleClient):Array {
    return FtyleProtoChain.getClassStyleDeclarations(object);
  }

  public static function initProtoChain(object:IStyleClient):void {
    if (object.styleDeclaration != null && !(object.styleDeclaration is AbstractCssStyleDeclaration)) {
      object.styleDeclaration = new InlineCssStyleDeclaration(InlineCssRuleset.createExternalInlineWithFactory(object.styleDeclaration.defaultFactory), StyleManagerEx(getStyleManager(object)));
    }
    return FtyleProtoChain.initProtoChain(object);
  }

  public static function initProtoChainForUIComponentStyleName(obj:IStyleClient):void {
    FtyleProtoChain.initProtoChainForUIComponentStyleName(obj);
  }

  public static function initTextField(obj:IUITextField):void {
    FtyleProtoChain.initTextField(obj);
  }

  public static function styleChanged(object:IInvalidating, styleProp:String):void {
    FtyleProtoChain.styleChanged(object, styleProp);
  }

  public static function matchesCSSType(object:IAdvancedStyleClient, cssType:String):Boolean {
    return FtyleProtoChain.matchesCSSType(object, cssType);
  }

  public static function getMatchingStyleDeclarations(object:IAdvancedStyleClient, styleDeclarations:Array = null):Array {
    return FtyleProtoChain.getMatchingStyleDeclarations(object, styleDeclarations);
  }

  public static function setStyle(object:IStyleClient, styleProp:String, newValue:*):void {
    if (styleProp == "styleName") {
      object.styleName = newValue;
      return;
    }
    
    EffectManager.setStyle(styleProp, object);
    
    var styleManager:IStyleManager2 = getStyleManager(object);
    var isProtoChainInitialized:Boolean = object.inheritingStyles != STYLE_UNINITIALIZED;
    if (object.styleDeclaration == null) {
      object.styleDeclaration = new InlineCssStyleDeclaration(InlineCssRuleset.createExternalInline(styleProp, newValue), StyleManagerEx(styleManager));
      if (isProtoChainInitialized) {
        object.regenerateStyleCache(styleManager.isInheritingStyle(styleProp));
      }
    }
    else {
      InlineCssStyleDeclaration(object.styleDeclaration).ruleset.put(styleProp, newValue);
    }

    if (isProtoChainInitialized && object.getStyle(styleProp) != newValue) {
      object.styleChanged(styleProp);
      object.notifyStyleChangeInChildren(styleProp, styleManager.isInheritingStyle(styleProp));
    }
  }

  private static function getStyleManager(object:Object):IStyleManager2 {
    if (object is IFlexModule) {
      return StyleManager.getStyleManager(IFlexModule(object).moduleFactory);
    }
    else if (object is StyleProxy) {
      return getStyleManagerFromStyleProxy(StyleProxy(object));
    }
    else {
      throw new ArgumentError("what is " + object);
    }
  }

  private static function getStyleManagerFromStyleProxy(object:StyleProxy):IStyleManager2 {
    var curObj:IStyleClient = object;
    while (curObj is StyleProxy) {
      curObj = StyleProxy(curObj).source;
    }

    return getStyleManager(curObj);
  }
}
}