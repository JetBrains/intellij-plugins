package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.flex.states.DeferredInstanceFromBytesBase;
import com.intellij.flex.uiDesigner.flex.states.StaticInstanceReferenceInDeferredParentInstanceBase;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.errors.IllegalOperationError;

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
    const size:int = data.readUnsignedShort();
    if (size == 0) {
      return;
    }

    for (var i:int = 0; i < size; i++) {
      var o:Object = null;
      var target:Object = readMxmlObjectReference(data, reader);
      var propertyName:String = reader.readClassOrPropertyName();
      var type:int = data.readByte();
      var isStyle:Boolean = (type & 1) != 0;
      type >>= 1;

      var targetBinding:PropertyBindingTarget;
      if (target is StaticInstanceReferenceInDeferredParentInstanceBase) {
        targetBinding = new PropertyBindingTarget(target, propertyName, isStyle);
      }
      else if (target is DeferredInstanceFromBytesBase) {
        throw new IllegalOperationError("unsupported");
      }

      switch (type) {
        case BindingType.MXML_OBJECT:
        case BindingType.MXML_OBJECT_WRAP_TO_ARRAY:
          o = readMxmlObjectReference(data, reader);
          // if target can be get only via binding, execute it
          // 1) on deferredParentInstance creation if value is static
          // 2) on value creation if value is dynamic
          if (o is DeferredInstanceFromBytesBase) {
            DeferredInstanceFromBytesBase(o).registerBinding(targetBinding == null ? new PropertyBindingTarget(target, propertyName, isStyle) : targetBinding);
            continue;
          }

          if (type == BindingType.MXML_OBJECT_WRAP_TO_ARRAY) {
            o = [o];
          }

          if (targetBinding != null) {
            targetBinding.staticValue = o;
          }
          break;

        case BindingType.VARIABLE:
          o = readVariableReference(data, reader);
          break;

        case BindingType.EXPRESSION:
          o = reader.readExpression();
          break;

        default:
          throw new ArgumentError("unknown binding type " + type);
      }

      if (targetBinding == null) {
        if (isStyle) {
          target.setStyle(propertyName, o);
        }
        else {
          target[propertyName] = o;
        }
      }
      else if (targetBinding.staticValue != null) {
        StaticInstanceReferenceInDeferredParentInstanceBase(target).deferredParentInstance.registerBinding(targetBinding);
      }
    }
    
    deferredReferenceClass = null;
  }

  //noinspection JSMethodCanBeStatic
  internal function readVariableReference(input:IDataInput, reader:MxmlReader):Object {
    var o:Object;
    var id:int = AmfUtil.readUInt29(input);
    if ((id & 1) == 0) {
      o = reader.readExpression();
    }

    id = id >> 1;
    if (id != 0) {
      if (o == null) {
        o = reader.objectTable[id - 1];
      }
      else {
        reader.saveReferredObject(id - 1, o);
      }
    }
    
    return o;
  }

  // todo trace("unsupported DeferredInstanceFromBytesBase in readExpression");
  internal function readMxmlObjectReference(input:IDataInput, reader:MxmlReader):Object {
    var id:int = AmfUtil.readUInt29(input);
    // is object reference or StaticInstanceReferenceInDeferredParentInstance input
    if ((id & 1) == 0) {
      return reader.objectTable[id >> 1];
    }
    else {
      if (deferredReferenceClass == null) {
        deferredReferenceClass = reader.context.moduleContext.getClass("com.intellij.flex.uiDesigner.flex.states.StaticInstanceReferenceInDeferredParentInstance");
      }
      var o:StaticInstanceReferenceInDeferredParentInstanceBase = new deferredReferenceClass();
      o.reference = id >> 1;
      o.deferredParentInstance = DeferredInstanceFromBytesBase(reader.readObjectReference());
      reader.saveReferredObject(AmfUtil.readUInt29(input), o);
      return o;
    }
  }
}
}

final class BindingType {
  public static const MXML_OBJECT:int = 0;
  public static const MXML_OBJECT_WRAP_TO_ARRAY:int = 1;
  public static const VARIABLE:int = 2;
  public static const EXPRESSION:int = 3;
}