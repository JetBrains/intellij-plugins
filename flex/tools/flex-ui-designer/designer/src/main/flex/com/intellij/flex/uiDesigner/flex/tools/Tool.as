package com.intellij.flex.uiDesigner.flex.tools {
public interface Tool {
  function attach(element:Object, toolContainer:ElementToolContainer):void;
  function detach():void;
}
}
