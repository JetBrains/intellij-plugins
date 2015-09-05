package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.util.ImmutableFakeObjectProxy;

import flash.utils.Dictionary;
import flash.utils.flash_proxy;

use namespace flash_proxy;

public dynamic class StyleDeclarationProxy extends ImmutableFakeObjectProxy {
  protected var userMap:Dictionary;

  protected var _declarations:Vector.<CssStyleDeclaration>;
  public function get declarations():Vector.<CssStyleDeclaration> {
    return _declarations;
  }

  public function getMapList():Vector.<Dictionary> {
    return mapList;
  }

  public function getUserMap():Dictionary {
    return userMap;
  }

  public function StyleDeclarationProxy(declaration:CssStyleDeclaration) {
    _declarations = new <CssStyleDeclaration>[declaration];
    super(null);
  }
  
  /**
   * In difference from rulesets in MergedCssStyleDeclaration and mapList, declaration are added in reverse order — that is last has greater priority, than the first
   * (such difference and convention violation due to the fact: we can not change the behavior of StyleProtoChain)
   */
  public function addDeclaration(declaration:CssStyleDeclaration):void {
    _declarations.fixed = false;
    _declarations[_declarations.length] = declaration;
    _declarations.fixed = true;
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

    return super.getProperty(name);
  }

  override flash_proxy function setProperty(name:*, value:*):void {
    if (value === undefined) {
      if (userMap != null) {
        delete userMap[name];
      }
    }
    else {
      if (userMap == null) {
        userMap = new Dictionary();
      }
      userMap[name] = value;
    }
  }
}
}