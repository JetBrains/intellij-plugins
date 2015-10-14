package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.DocumentReaderContext;
import com.intellij.flex.uiDesigner.ModuleContext;
import com.intellij.flex.uiDesigner.SerializedDocumentDataProvider;
import com.intellij.flex.uiDesigner.VirtualFile;

internal class FlexComponentCreator implements ClassReference, DocumentReaderContext {
  protected var source:SerializedDocumentDataProvider;
  protected var context:DeferredInstanceFromBytesContext;

  public function FlexComponentCreator(source:SerializedDocumentDataProvider, context:DeferredInstanceFromBytesContext) {
    this.source = source;
    this.context = context;
  }

  public function get className():String {
    return source.className;
  }

  public function get file():VirtualFile {
    return context.readerContext.file;
  }

  public function get moduleContext():ModuleContext {
    return context.readerContext.moduleContext;
  }

  public function registerComponentDeclarationRangeMarkerId(component:Object, id:int):void {
    context.readerContext.registerComponentDeclarationRangeMarkerId(component, id);
  }

  public function registerObjectWithId(id:String, object:Object):void {
    // actual only for FlexDocumentFactory
  }

  public function get instanceForRead():Object {
    return null;
  }
}
}
