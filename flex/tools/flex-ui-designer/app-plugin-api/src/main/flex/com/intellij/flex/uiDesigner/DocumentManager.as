package com.intellij.flex.uiDesigner {
public interface DocumentManager {
  [Bindable(event="documentChanged")]
  function get document():Document;

  function set document(value:Document):void;

  function open(documentFactory:DocumentFactory):void;
}
}