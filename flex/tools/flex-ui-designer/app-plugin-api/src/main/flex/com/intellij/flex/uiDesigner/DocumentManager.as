package com.intellij.flex.uiDesigner {
import org.osflash.signals.ISignal;

public interface DocumentManager {
  [Bindable(event="documentChanged")]
  function get document():Document;

  function set document(value:Document):void;

  function open(documentFactory:DocumentFactory, documentOpened:Function = null):void;

  function get documentUpdated():ISignal;
}
}