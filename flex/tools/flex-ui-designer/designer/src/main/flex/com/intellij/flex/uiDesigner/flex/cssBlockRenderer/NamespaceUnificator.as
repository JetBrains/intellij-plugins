package com.intellij.flex.uiDesigner.flex.cssBlockRenderer {
import com.intellij.flex.uiDesigner.VirtualFileImpl;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.css.CssSelector;

import flash.utils.Dictionary;

public class NamespaceUnificator {
  private var uriToPrefix:Dictionary;
  
  public function reset():void {
    uriToPrefix = new Dictionary();
  }
  
  public function getNamespacePrefix(ruleset:CssRuleset, selector:CssSelector):String {
    return uriToPrefix[VirtualFileImpl(ruleset.file).stylesheet.namespaces[selector.namespacePrefix]];
  }
  
  public function process(ruleset:CssRuleset):void {
    var namespaceMap:Dictionary = VirtualFileImpl(ruleset.file).stylesheet.namespaces;
    for each (var selector:CssSelector in ruleset.selectors) {
      var prefix:String = selector.namespacePrefix;
      if (prefix != null) {
        var uri:String = namespaceMap[prefix];
        if (uri == "library://ns.adobe.com/flex/spark") {
          uriToPrefix[uri] = null;
        }
        else {
          uriToPrefix[uri] = prefix != null ? prefix : (uri == "library://ns.adobe.com/flex/mx" ? "mx" : generatePrefixForEmptyPrefix(uri));
        }
      }
    }
  }

  private function generatePrefixForEmptyPrefix(uri:String):String {
    var si:int = uri.lastIndexOf("/");
    if (si == -1) {
      return uri;
    }
    else {
      return uri.substr(si + 1);
    }
  }
}
}