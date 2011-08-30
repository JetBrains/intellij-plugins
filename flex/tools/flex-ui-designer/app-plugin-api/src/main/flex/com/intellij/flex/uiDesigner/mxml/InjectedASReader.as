package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.flex.BindingTarget;
import com.intellij.flex.uiDesigner.flex.states.DeferredInstanceFromBytesBase;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.utils.IDataInput;

public class InjectedASReader {
  private var deferredReferenceClass:Class;
  
  public function read(input:IDataInput, reader:MxmlReader):void {
    readDeclarations(input, reader);
    readBinding(input, reader);
  }

  //noinspection JSMethodCanBeStatic
  private function readDeclarations(input:IDataInput, reader:MxmlReader):void {
    const length:int = input.readUnsignedShort();
    if (length == 0) {
      return;
    }
    
    var vector:Vector.<Object> = new Vector.<Object>(length, true);
    reader.readArrayOrVector(vector, length); // result vector is ignored
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
      var isStyle:Boolean = (type & 1) != 0;
      type >>= 1;

      switch (type) {
        case BindingType.MXML_OBJECT:
          o = readObjectReference(data, reader, new PropertyBindingTarget(target, propertyName));
          break;

        case BindingType.VARIABLE:
          var id:int = AmfUtil.readUInt29(data);
          if (id == 0) {
            o = reader.readExpression();
          }
          else {
            o = reader.objectTable[id - 1];
          }
          break;

        default:
          throw new ArgumentError("unknown binding type");
      }

      if (o != null /* binding */) {
        if (isStyle) {
          target.setStyle(propertyName, o);
        }
        else {
          target[propertyName] = o;
        }
      }
    }
    
    deferredReferenceClass = null;
  }

  private function readObjectReference(data:IDataInput, reader:MxmlReader, bindingTarget:BindingTarget):Object {
    var id:int = AmfUtil.readUInt29(data);
    var o:Object;
    // is object reference or StaticInstanceReferenceInDeferredParentInstance data
    if ((id & 1) == 0) {
      o = reader.objectTable[id >> 1];
      if (o is DeferredInstanceFromBytesBase) {
        DeferredInstanceFromBytesBase(o).getInstanceIfCreatedOrRegisterBinding(bindingTarget);
        return null;
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

final class BindingType {
  public static const MXML_OBJECT:int = 0;
  public static const VARIABLE:int = 1;
  public static const EXPRESSION:int = 2;
}