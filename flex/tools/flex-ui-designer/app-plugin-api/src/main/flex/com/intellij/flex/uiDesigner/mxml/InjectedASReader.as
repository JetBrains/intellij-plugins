package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.flex.states.DeferredInstanceFromBytesBase;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.errors.IllegalOperationError;
import flash.utils.IDataInput;

public class InjectedASReader {
  private static const OBJECT:int = 0;
  private static const ARRAY:int = 1;
  
  private var deferredReferenceClass:Class;
  
  public function read(data:IDataInput, reader:MxmlReader):void {
    readDeclarations(reader);
    readBinding(data, reader);
  }

  //noinspection JSMethodCanBeStatic
  private function readDeclarations(reader:MxmlReader):void {
    reader.readArray([]); // result array is ignored
  }
  
  private function readBinding(data:IDataInput, reader:MxmlReader):void {
    const size:int = data.readShort();
    if (size == 0) {
      return;
    }
    
    var o:Object;
    for (var i:int = 0; i < size; i++) {
      var target:Object = reader.readObjectReference();
      var propertyName:String = reader.readClassOrPropertyName();
      var type:int = data.readByte();
      var isStyle:Boolean = data.readBoolean();

      switch (type) {
        case OBJECT:
          o = readObjectReference(data, reader);
          break;

        case ARRAY:
          const length:int = data.readByte();
          var array:Array = new Array(length);
          for (var j:int = 0; j < length; j++) {
            array[j] = readObjectReference(data, reader);
          }

          o = array;
          break;

        default:
          throw new ArgumentError("unknown binding type");
      }

      if (isStyle) {
        target.setStyle(propertyName, o);
      }
      else {
        target[propertyName] = o;
      }
    }
    
    deferredReferenceClass = null;
  }

  private function readObjectReference(data:IDataInput, reader:MxmlReader):Object {
    var id:int = AmfUtil.readUInt29(data);
    var o:Object;
    // is object reference or StaticInstanceReferenceInDeferredParentInstance data
    if ((id & 1) == 0) {
      o = reader.objectTable[id >> 1];
      // todo deferred instance from bytes
      if (o is DeferredInstanceFromBytesBase) {
        throw new IllegalOperationError();
      }
    }
    else {
      if (deferredReferenceClass == null) {
        deferredReferenceClass = reader.context.moduleContext.getClass("com.intellij.flex.uiDesigner.mxml.StaticInstanceReferenceInDeferredParentInstance");
      }
      o = new deferredReferenceClass();
      o.reference = id >> 1;
      o.deferredParentInstance = reader.readObjectReference();

      id = AmfUtil.readUInt29(data);
      if (reader.objectTable[id] != null) {
        throw new ArgumentError("must be null");
      }
      reader.objectTable[id] = o;
    }
    
    return o;
  }
}
}
