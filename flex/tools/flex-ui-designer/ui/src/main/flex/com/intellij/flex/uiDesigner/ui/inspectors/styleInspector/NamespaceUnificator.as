package com.intellij.flex.uiDesigner.ui.inspectors.styleInspector {
import com.intellij.flex.uiDesigner.VirtualFileImpl;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.css.CssSelector;

import flash.utils.Dictionary;

public class NamespaceUnificator {
  private var uriToPrefix:Dictionary;

  private const _uriList:Vector.<String> = new Vector.<String>();
  public function get uriList():Vector.<String> {
    return _uriList;
  }
  
  public function reset():void {
    uriToPrefix = new Dictionary();
    _uriList.length = 0;
  }
  
  public function getNamespacePrefix(ruleset:CssRuleset, selector:CssSelector):String {
    return getNamespacePrefix2(VirtualFileImpl(ruleset.file).stylesheet.namespaces[selector.namespacePrefix]);
  }
  
  public function getNamespacePrefix2(uri:String):String {
    return uriToPrefix[uri];
  }
  
  public function process(ruleset:CssRuleset):void {
    var namespaceMap:Dictionary = VirtualFileImpl(ruleset.file).stylesheet.namespaces;
    for each (var selector:CssSelector in ruleset.selectors) {
      if (selector.presentableSubject == null) {
        continue;
      }
      
      var prefix:String = selector.namespacePrefix;
      var uri:String = namespaceMap[prefix];
      if (uri in uriToPrefix) {
        continue;
      }

      if (uri == "library://ns.adobe.com/flex/spark") {
        uriToPrefix[uri] = null;
      }
      else {
        uriToPrefix[uri] = prefix != null ? prefix : (uri == "library://ns.adobe.com/flex/mx" ? "mx" : generatePrefixForEmptyPrefix(uri));
      }

      _uriList.push(uri);
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