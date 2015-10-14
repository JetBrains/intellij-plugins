package com.intellij.flex.uiDesigner.flex {
import flash.utils.ByteArray;

import mx.core.IFactory;

public final class FlexComponentFactory extends FlexComponentCreator implements IFactory {
  private var data:ByteArray;

  public function FlexComponentFactory(data:ByteArray, context:DeferredInstanceFromBytesContext) {
    super(null, context);
    this.data = data;
  }

  public function newInstance():* {
    return context.createReader().readDeferredInstanceFromBytes(data, context);
  }

  override public function get className():String {
    return "InnerClass";
  }
}
}