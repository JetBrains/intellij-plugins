package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.SerializedDocumentDataProvider;

import mx.core.IFactory;

public final class FlexDocumentFactory implements IFactory, ClassReference {
  private var source:SerializedDocumentDataProvider;
  private var context:DeferredInstanceFromBytesContext;
  
  public function FlexDocumentFactory(source:SerializedDocumentDataProvider, context:DeferredInstanceFromBytesContext) {
    this.source = source;
    this.context = context;
  }
  
  public function get className():String {
    return source.className;
  }

  public function newInstance():* {
    return context.reader.read(source.data, context.readerContext);
  }
}
}
