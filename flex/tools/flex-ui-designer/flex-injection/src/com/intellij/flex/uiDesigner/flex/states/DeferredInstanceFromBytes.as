package com.intellij.flex.uiDesigner.flex.states {
import flash.utils.ByteArray;

import mx.core.ITransientDeferredInstance;

public final class DeferredInstanceFromBytes extends DeferredInstanceFromBytesBase implements ITransientDeferredInstance {
  public function DeferredInstanceFromBytes(bytes:ByteArray) {
    super(bytes);
  }
}
}
