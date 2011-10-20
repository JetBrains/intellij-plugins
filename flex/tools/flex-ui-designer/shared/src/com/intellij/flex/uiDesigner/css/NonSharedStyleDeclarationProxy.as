package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.util.ImmutableFakeObjectProxy;

import flash.utils.flash_proxy;

use namespace flash_proxy;

public dynamic final class NonSharedStyleDeclarationProxy extends StyleDeclarationProxy {
  public function NonSharedStyleDeclarationProxy(owner:Object, parent:ImmutableFakeObjectProxy, declaration:CssStyleDeclaration) {
    _owner = owner;
    _parent = parent;

    super(declaration);
  }

  private var _owner:Object;
  public function get owner():Object {
    return _owner;
  }

  private var _parent:ImmutableFakeObjectProxy;
  public function get parent():ImmutableFakeObjectProxy {
    return _parent;
  }

  //noinspection JSMethodCanBeStatic,JSUnusedGlobalSymbols
  public function get effects():Object {
    return null;
  }

  override flash_proxy function getProperty(name:*):* {
    var v:* = userMap == null ? undefined : userMap[name];
    if (v !== undefined) {
      return v;
    }

    for (var i:int = _declarations.length - 1; i > -1; i--) {
      v = _declarations[i].getStyle(name);
      if (v !== undefined) {
        return v;
      }
    }

    return _parent.getProperty(name);
  }
}
}