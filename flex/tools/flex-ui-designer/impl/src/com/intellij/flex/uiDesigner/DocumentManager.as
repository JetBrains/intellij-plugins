package com.intellij.flex.uiDesigner {
import org.jetbrains.util.ActionCallback;
import org.osflash.signals.ISignal;

public interface DocumentManager {
  [Bindable(event="documentChanged")]
  function get document():Document;

  function set document(value:Document):void;

  function render(documentFactory:DocumentFactory):ActionCallback;

  function get documentUpdated():ISignal;

  function get documentChanged():ISignal;
}
}