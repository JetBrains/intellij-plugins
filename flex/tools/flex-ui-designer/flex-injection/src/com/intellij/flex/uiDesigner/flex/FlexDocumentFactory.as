package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.DocumentReaderContext;
import com.intellij.flex.uiDesigner.ModuleContext;
import com.intellij.flex.uiDesigner.SerializedDocumentDataProvider;
import com.intellij.flex.uiDesigner.VirtualFile;

import mx.core.IFactory;
import mx.core.UIComponent;

public final class FlexDocumentFactory implements IFactory, ClassReference, SkinPartFinder, DocumentReaderContext {
  private var source:SerializedDocumentDataProvider;
  private var context:DeferredInstanceFromBytesContext;

  private var objectsWithId:Vector.<UIComponent>;

  public function FlexDocumentFactory(source:SerializedDocumentDataProvider, context:DeferredInstanceFromBytesContext) {
    this.source = source;
    this.context = context;
  }

  public function get className():String {
    return source.className;
  }

  public function newInstance():* {
    // why restore oldInput/oldContext? See CustomMxmlComponentAsChild — we read child document factory before finish read initial document
    return context.reader.read(source.data, this, context.styleManager, true);
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

  public function get file():VirtualFile {
    return context.readerContext.file;
  }

  public function get moduleContext():ModuleContext {
    return context.readerContext.moduleContext;
  }

  public function registerObjectDeclarationRangeMarkerId(object:Object, textOffset:int):void {
    context.readerContext.registerObjectDeclarationRangeMarkerId(object, textOffset);
  }

  public function registerObjectWithId(id:String, object:Object):void {
    if (object is UIComponent) {
      if (objectsWithId == null) {
        objectsWithId = new Vector.<UIComponent>();
      }
      objectsWithId[objectsWithId.length] = UIComponent(object);
    }
  }
}
}