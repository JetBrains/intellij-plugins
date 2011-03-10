package com.intellij.flex.uiDesigner.css {
import mx.styles.CSSStyleDeclaration;
import mx.styles.IStyleManager2;

public class ChildStyleManager extends AbstractStyleManager implements IStyleManager2, StyleManagerEx {
  public function ChildStyleManager(parent:RootStyleManager) {
    _parent = parent;
  }
  
  private var _parent:RootStyleManager;
  override public function get parent():IStyleManager2 {
    return _parent;
  }
  
  public function get styleValueResolver():StyleValueResolver {
    return _parent.styleValueResolver;
  }

  public function isColorName(colorName:String):Boolean {
    return false;
  }

  public function get inheritingStyles():Object {
    return _parent.inheritingStyles;
  }

  public function isInheritingStyle(styleName:String):Boolean {
    return _parent.isInheritingStyle(styleName);
  }

  public function registerInheritingStyle(styleName:String):void {
    // если что-то в документе регистрируется как InheritingStyle – значит оно для всего модуля (в реальном-то приложении на документ нет отдельного StyleManager)
    _parent.registerInheritingStyle(styleName);
  }
  
  override public function get selectors():Array {
    return super.selectors.concat(_parent.selectors);
  }
  
  override public function hasAdvancedSelectors():Boolean {
    return super.hasAdvancedSelectors() || _parent.hasAdvancedSelectors();
  }

  public function getMergedStyleDeclaration(selector:String):CSSStyleDeclaration {
    var style:MergedCssStyleDeclaration = MergedCssStyleDeclaration(getStyleDeclaration(selector));
    var parentStyle:MergedCssStyleDeclaration = MergedCssStyleDeclaration(_parent.getMergedStyleDeclaration(selector));
    if (style == null) {
      return parentStyle;
    }
    else if (parentStyle == null) {
      return style;
    }
    
    return MergedCssStyleDeclaration.mergeDeclarations(selector, style, parentStyle, this);
  }
  
  override public function get stylesRoot():Object {
    return rootStyleDeclarationProxy == null ? parent.stylesRoot : rootStyleDeclarationProxy;
  }

  public function setRootDeclaration(rootDeclaration:CssStyleDeclaration):void {
    rootStyleDeclarationProxy = new NonSharedStyleDeclarationProxy(null, rootStyleDeclarationProxy, rootDeclaration);
  }
}
}
