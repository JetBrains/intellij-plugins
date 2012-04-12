package com.intellij.flex.uiDesigner.css {
public interface CssStyleDeclaration {
  function getStyle(styleProp:String):*;

  function get _selector():CssSelector;
}
}