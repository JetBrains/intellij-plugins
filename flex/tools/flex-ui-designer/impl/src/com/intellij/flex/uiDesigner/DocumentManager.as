package com.intellij.flex.uiDesigner {
import org.osflash.signals.ISignal;

public interface DocumentManager {
  [Bindable(event="documentChanged")]
  function get document():Document;

  function set document(value:Document):void;

  function open(documentFactory:DocumentFactory, activateAndFocus:Boolean):void;

  function get documentUpdated():ISignal;

  function get documentRendered():ISignal;

  function get documentChanged():ISignal;
}
}