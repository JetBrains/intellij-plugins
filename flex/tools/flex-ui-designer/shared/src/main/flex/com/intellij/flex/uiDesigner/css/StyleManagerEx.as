package com.intellij.flex.uiDesigner.css {
public interface StyleManagerEx {
  function get styleValueResolver():StyleValueResolver;

  function setRootDeclaration(rootDeclaration:CssStyleDeclaration):void;
  
  function isInheritingStyle(styleName:String):Boolean;
}
}