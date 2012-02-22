package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.VirtualFile;

import flash.utils.Dictionary;

public class InlineCssRuleset extends CssRuleset {
  public function InlineCssRuleset() {
    declarations = new Vector.<CssDeclaration>();
  }
  
  public static function createInline(line:int, offset:int, file:VirtualFile = null):InlineCssRuleset {
    var ruleset:InlineCssRuleset = new InlineCssRuleset();
    ruleset._line = line;
    ruleset._textOffset = offset;
    ruleset._file = file;
    return ruleset;
  }
  
  public static function createRuntime(name:String, value:*):InlineCssRuleset {
    var ruleset:InlineCssRuleset = new InlineCssRuleset();
    ruleset.put(name, value);
    return ruleset;
  }
  
  public static function createExternalInlineWithFactory(defaultFactory:Function, fromAs:Boolean):InlineCssRuleset {
    var ruleset:InlineCssRuleset = new InlineCssRuleset();
    defaultFactory.prototype = {};
    var data:Object = new defaultFactory();
    var map:Dictionary = new Dictionary();
    var list:Vector.<CssDeclaration> = ruleset.declarations;
    ruleset._declarationMap = map;
    var declaration:CssDeclarationImpl;
    var i:int = list.length;
    for (var key:String in data) {
      declaration = CssDeclarationImpl.createRuntime(key, data[key], fromAs);
      map[key] = declaration;
      list[i++] = declaration;
    }

    list.fixed = true;
    return ruleset;
  }

  public static function fillFromFactory(defaultFactory:Function, ruleset:InlineCssRuleset):void {
    defaultFactory.prototype = {};
    var data:Object = new defaultFactory();
    var map:Dictionary = ruleset._declarationMap;
    var list:Vector.<CssDeclaration> = ruleset.declarations;
    list.fixed = false;
    var declaration:CssDeclarationImpl;
    var i:int = list.length;
    for (var key:String in data) {
      declaration = CssDeclarationImpl.createRuntime(key, data[key], false);
      map[key] = declaration;

      list[i++] = declaration;
    }

    list.fixed = true;
  }

  override public function get inline():Boolean {
    return true;
  }
}
}