package com.intellij.flex.uiDesigner.css {
public interface CssCondition {
  function matches(object:Object):Boolean;

  function get value():String;

  function get specificity():int;

  function appendString(text:String):String;
}
}
