package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.SerializedDocumentDataProvider;

public final class SparkViewInitializer extends FlexComponentCreator {
  public function SparkViewInitializer(source:SerializedDocumentDataProvider, context:DeferredInstanceFromBytesContext) {
    super(source, context)
  }

  public function initialize(component:Object):void {
    _instanceForRead = component;
    try {
      context.createReader().read(source.data, this, context.styleManager);
    }
    finally {
      _instanceForRead = null;
    }
  }

  private var _instanceForRead:Object;
  override public function get instanceForRead():Object {
    return _instanceForRead;
  }
}
}
