package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.SerializedDocumentDataProvider;

import mx.core.IFactory;
import mx.core.UIComponent;

public final class FlexDocumentFactory extends FlexComponentCreator implements IFactory, SkinPartFinder {
  private var objectsWithId:Vector.<UIComponent>;

  public function FlexDocumentFactory(source:SerializedDocumentDataProvider, context:DeferredInstanceFromBytesContext) {
    super(source, context);
  }

  public function newInstance():* {
    return context.createReader().read(source.data, this, context.styleManager);
  }

  public function findSkinParts(host:SkinHost, skinParts:Object):void {
    if (objectsWithId == null) {
      return;
    }

    for each (var object:UIComponent in objectsWithId) {
      var partName:String = object.id;
      if (partName in skinParts) {
        host.skinPartAdded(partName, object);
      }
    }
  }

  override public function registerObjectWithId(id:String, object:Object):void {
    if (object is UIComponent) {
      if (objectsWithId == null) {
        objectsWithId = new Vector.<UIComponent>();
      }
      objectsWithId[objectsWithId.length] = UIComponent(object);
    }
  }
}
}