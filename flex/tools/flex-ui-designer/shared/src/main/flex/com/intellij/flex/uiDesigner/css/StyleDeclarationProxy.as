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
   * в отличии от rulesets в MergedCssStyleDeclaration и mapList, declaration добавляются в обратном порядке — то есть последний имеет больший приоритет, чем первый
   * (такая разница и нарушение конвенции связано с тем, что мы не можем изменить поведение StyleProtoChain)
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