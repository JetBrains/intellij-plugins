package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.DocumentFactory;

import mx.core.IFactory;

public final class FlexDocumentFactory implements IFactory {
  private var source:DocumentFactory;
  private var context:DeferredInstanceFromBytesContext;
  
  public function FlexDocumentFactory(factory:DocumentFactory, context:DeferredInstanceFromBytesContext) {
    this.source = factory;
    this.context = context;
  }

  public function newInstance():* {
    var object:Object = context.documentReader.read2(source.bytes, context);
    source.bytes.position = 0;
    return object;
  }
}
}
