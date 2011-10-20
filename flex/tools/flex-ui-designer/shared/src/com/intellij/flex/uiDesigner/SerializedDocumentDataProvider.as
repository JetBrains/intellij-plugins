package com.intellij.flex.uiDesigner {
import flash.utils.ByteArray;

public interface SerializedDocumentDataProvider {
  function get data():ByteArray;

  function get className():String;
}
}
