package com.intellij.flex.uiDesigner.css {
public interface CssDeclaration {
  function get fromAs():Boolean;

  function get name():String;
  function get presentableName():String;

  function get type():int;

  function get value():*;

  function get colorName():String;

  function get textOffset():int;
}
}