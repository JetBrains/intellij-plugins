package com.intellij.flex.uiDesigner.css {
import flash.display.DisplayObject;

import mx.core.mx_internal;
import mx.styles.CSSStyleDeclaration;
import mx.styles.IAdvancedStyleClient;

use namespace mx_internal;

[Abstract]
public class AbstractCssStyleDeclaration extends CSSStyleDeclaration implements CssStyleDeclaration {
  protected var styleValueResolver:StyleValueResolver;

  public function AbstractCssStyleDeclaration(styleValueResolver:StyleValueResolver) {
    this.styleValueResolver = styleValueResolver;
    super(null, null, false);
  }

  override mx_internal function addStyleToProtoChain(chain:Object, target:DisplayObject, filterMap:Object = null):Object {
    if (filterMap != null) {
      for (var k:Object in filterMap) {
        if (k != filterMap[k]) {
          throw new ArgumentError("filterMap with unequal key and name is not supported");
        }
      }
      
      var s:StyleDeclarationProxy = StyleDeclarationProxy(chain);
      // if target has mx.styles.StyleProxy — chain is StyleDeclarationProxy (i.e. global, nonInheritingChain),
      // otherwise NonSharedStyleDeclarationProxy
      if (!(s is NonSharedStyleDeclarationProxy) || NonSharedStyleDeclarationProxy(s).owner == target) {
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

  override public function matchesStyleClient(object:IAdvancedStyleClient):Boolean {
    return false;
  }

  public function get _selector():CssSelector {
    return null;
  }
}
}
