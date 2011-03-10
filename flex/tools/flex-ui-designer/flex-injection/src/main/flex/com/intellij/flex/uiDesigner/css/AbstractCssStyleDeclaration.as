package com.intellij.flex.uiDesigner.css {
import flash.display.DisplayObject;

import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.IStyleManager2;

use namespace mx_internal;

[Abstract]
public class AbstractCssStyleDeclaration extends CSSStyleDeclaration implements CssStyleDeclaration {
  protected var styleManager:StyleManagerEx;

  public function AbstractCssStyleDeclaration(styleManager:StyleManagerEx) {
    this.styleManager = styleManager;
    super(null, IStyleManager2(styleManager), false);
  }

  override mx_internal function addStyleToProtoChain(chain:Object, target:DisplayObject, filterMap:Object = null):Object {
    if (filterMap != null) {
      for (var n:Object in filterMap) {
        if (n != filterMap[n]) {
          throw new ArgumentError("filterMap with unequal key and name is not supported");
        }
      }
      
      var s:NonSharedStyleDeclarationProxy = NonSharedStyleDeclarationProxy(chain);
      if (s.owner == target) {
        return new NonSharedStyleDeclarationProxy(target, s, this);
      }
      else {
        s.addDeclaration(this);
        return s;
      }
    }

    var source:StyleDeclarationProxy = StyleDeclarationProxy(chain);
    if (source is NonSharedStyleDeclarationProxy && NonSharedStyleDeclarationProxy(source).owner == target) {
      source.addDeclaration(this);
      return source;
    }
    else {
      return new NonSharedStyleDeclarationProxy(target, source, this);
    }
  }
}
}
