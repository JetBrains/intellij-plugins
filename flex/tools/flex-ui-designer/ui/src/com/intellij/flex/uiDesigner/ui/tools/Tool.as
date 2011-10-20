package com.intellij.flex.uiDesigner.ui.tools {
public interface Tool {
  function attach(element:Object, toolContainer:ElementToolContainer):void;
  function detach():void;
}
}
