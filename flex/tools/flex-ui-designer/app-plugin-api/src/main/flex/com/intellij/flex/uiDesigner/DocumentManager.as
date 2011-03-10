package com.intellij.flex.uiDesigner {
import flash.utils.IDataInput;

public interface DocumentManager {
  [Bindable(event="documentChanged")]
  function get document():Document;

  function set document(value:Document):void;

  function open(module:Module, file:IDataInput):void;
}
}