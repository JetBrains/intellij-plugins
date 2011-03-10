package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.VirtualFile;

import flash.utils.Dictionary;

public class InlineCssRuleset extends CssRuleset {
  public function InlineCssRuleset() {
    _declarations = new Vector.<CssDeclaration>();
  }
  
  public static function createInline(line:int, offset:int, file:VirtualFile = null):InlineCssRuleset {
    var ruleset:InlineCssRuleset = new InlineCssRuleset();
    ruleset._line = line;
    ruleset._textOffset = offset;
    ruleset._file = file;
    return ruleset;
  }
  
  public static function createExternalInline(name:String, value:*):InlineCssRuleset {
    var ruleset:InlineCssRuleset = new InlineCssRuleset();
    ruleset.put(name, value);
    return ruleset;
  }
  
  public static function createExternalInlineWithFactory(defaultFactory:Function):InlineCssRuleset {
    var ruleset:InlineCssRuleset = new InlineCssRuleset();
    defaultFactory.prototype = {};
    var data:Object = new defaultFactory();
    var map:Dictionary = new Dictionary();
    var list:Vector.<CssDeclaration> = ruleset.declarations;
    ruleset._declarationMap = map;
    var declaration:CssDeclaration;
    for (var key:String in data) {
      declaration = new CssDeclaration();
      declaration.name = key;
      declaration.value = data[key];
      map[key] = declaration;

      list[list.length] = declaration;
    }

    list.fixed = true;
    return ruleset;
  }
  
  
  override public function get inline():Boolean {
    return true;
  }
}
}
