package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.VirtualFile;

public interface CssReader {
  function read(rulesets:Vector.<CssRuleset>, file:VirtualFile):void;

  function finalizeRead():void;

  function set styleManager(value:StyleManagerEx):void;
}
}
